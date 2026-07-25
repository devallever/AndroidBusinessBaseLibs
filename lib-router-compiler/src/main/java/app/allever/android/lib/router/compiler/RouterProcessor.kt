package app.allever.android.lib.router.compiler

import app.allever.android.lib.router.annotation.Route
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

class RouterProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val codeGenerator = environment.codeGenerator
    private val logger = environment.logger
    private val routeMap = mutableMapOf<String, RouteInfo>()
    private val moduleRoutes = mutableMapOf<String, MutableList<RouteInfo>>()

    private val moduleName = environment.options["routerModuleName"]
        ?.replace(Regex("[^a-zA-Z0-9_]"), "_")
        ?.takeIf { it.isNotEmpty() }
        ?: "default"

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val routeSymbols = resolver.getSymbolsWithAnnotation(Route::class.qualifiedName!!)
        val invalidSymbols = mutableListOf<KSAnnotated>()

        logger.info("[Router] ========== KSP Process Start ==========")
        logger.info("[Router] Module: $moduleName")
        logger.info("[Router] Options: ${environment.options}")
        logger.info("[Router] Incremental: ${environment.options["ksp.incremental"] ?: "unknown"}")
        val allFiles = resolver.getAllFiles().toList()
        logger.info("[Router] Total source files in resolver: ${allFiles.size}")

        val routeCount = routeSymbols.count()
        logger.info("[Router] Found @Route symbols: $routeCount")

        routeSymbols.forEach { symbol ->
            if (!symbol.validate()) {
                invalidSymbols.add(symbol)
                return@forEach
            }

            if (symbol is KSClassDeclaration) {
                try {
                    val routeAnnotation = symbol.annotations.first {
                        it.shortName.asString() == "Route"
                    }

                    val pathArg = routeAnnotation.arguments.first { it.name?.asString() == "path" }
                    val path = pathArg.value as String

                    val nameArg = routeAnnotation.arguments.firstOrNull { it.name?.asString() == "name" }
                    val name = (nameArg?.value as? String) ?: ""

                    val exportArg = routeAnnotation.arguments.firstOrNull { it.name?.asString() == "export" }
                    val export = (exportArg?.value as? Boolean) ?: true

                    val routeInfo = RouteInfo(
                        path = path,
                        name = name,
                        export = export,
                        className = symbol.qualifiedName?.asString() ?: "",
                        moduleName = moduleName,
                        sourceFile = symbol.containingFile
                    )

                    logger.info("[Router] Route mapping: ${routeInfo.path} -> ${routeInfo.className} (export=${routeInfo.export}, source=${routeInfo.sourceFile?.fileName ?: "unknown"})")

                    validateRoute(routeInfo, symbol)

                    if (routeInfo.export) {
                        routeMap[routeInfo.path] = routeInfo
                        moduleRoutes.getOrPut(routeInfo.moduleName) { mutableListOf() }.add(routeInfo)
                    }
                } catch (e: Exception) {
                    logger.error("Error processing route: ${e.message}", symbol)
                }
            }
        }

        logger.info("[Router] Registered routes: ${routeMap.size}, Modules: ${moduleRoutes.size}")
        moduleRoutes.forEach { (mod, routes) ->
            logger.info("[Router] Module '$mod' routes: ${routes.joinToString { "${it.path} -> ${it.className}" }}")
        }

        generateModuleRegistries()

        logger.info("[Router] ========== KSP Process End ==========")

        return invalidSymbols
    }

    private fun validateRoute(routeInfo: RouteInfo, symbol: KSClassDeclaration) {
        if (!routeInfo.path.startsWith("/")) {
            logger.error("Route path must start with '/'", symbol)
        }
        if (routeInfo.path.length < 3) {
            logger.error("Route path is too short", symbol)
        }
        val pathRegex = Regex("^/[a-zA-Z0-9_/-]+$")
        if (!routeInfo.path.matches(pathRegex)) {
            logger.error("Invalid route path: ${routeInfo.path}, only letters, numbers, '-', '_', '/' are allowed", symbol)
        }
        routeMap[routeInfo.path]?.let { existing ->
            if (existing.className != routeInfo.className) {
                logger.error("Duplicate route path: ${routeInfo.path}, already defined in ${existing.className}", symbol)
            }
        }
    }

    private fun generateModuleRegistries() {
        moduleRoutes.forEach { (moduleName, routes) ->
            if (routes.isEmpty()) return@forEach

            val className = "RouterModule_${moduleName}"

            val registerCode = routes.joinToString("\n") {
                "Router.register(\"${it.path}\", ${it.className}::class.java)"
            }

            val objectSpec = TypeSpec.objectBuilder(className)
                .addInitializerBlock(CodeBlock.of(registerCode))
                .build()

            val fileSpec = FileSpec.builder("app.allever.android.lib.router.module", className)
                .addImport("app.allever.android.lib.router", "Router")
                .addType(objectSpec)
                .build()

            val sourceFiles = routes.mapNotNull { it.sourceFile }.toTypedArray()

            logger.info("[Router] Generating: app.allever.android.lib.router.module.$className (routes: ${routes.size}, incremental deps: ${sourceFiles.size})")

            try {
                codeGenerator.createNewFile(
                    dependencies = Dependencies(true, *sourceFiles),
                    packageName = "app.allever.android.lib.router.module",
                    fileName = className
                )?.use {
                    it.write(fileSpec.toString().toByteArray())
                }
            } catch (e: kotlin.io.FileAlreadyExistsException) {
                logger.info("[Router] File already exists, skipped: $className")
            }

            val registryClassName = "RouterModuleRegistry_${moduleName}"
            val registryObjectSpec = TypeSpec.objectBuilder(registryClassName)
                .addProperty(PropertySpec.builder("MODULE_NAME", String::class)
                    .addModifiers(KModifier.PUBLIC, KModifier.CONST)
                    .initializer("%S", className)
                    .build())
                .build()
            val registryFileSpec = FileSpec.builder("app.allever.android.lib.router.module", registryClassName)
                .addType(registryObjectSpec)
                .build()

            try {
                codeGenerator.createNewFile(
                    dependencies = Dependencies(true, *sourceFiles),
                    packageName = "app.allever.android.lib.router.module",
                    fileName = registryClassName
                )?.use {
                    it.write(registryFileSpec.toString().toByteArray())
                }
            } catch (e: kotlin.io.FileAlreadyExistsException) {
            }
        }
    }

    data class RouteInfo(
        val path: String,
        val name: String,
        val export: Boolean,
        val className: String,
        val moduleName: String,
        val sourceFile: KSFile?
    )
}