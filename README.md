# 🔥🔥🔥Android基础组件库&示例代码🔥🔥🔥

打造一个简单易用的基础组件库，封装架构组件、网络组件、存储组件、图片加载组件、媒体组件、权限组件、播放器组件、广告组件，并提供完整的示例代码。

## 背景

这是对我在开发中的总结和思考，旨在帮助大家快速开发，减少冗余代码，提升开发效率。每个组件都遵循「核心 +
引擎实现」的解耦设计，方便按需引入和替换底层实现。

# 目录说明

- core：最基础的组件
- lib-xx：按照功能提取出来的组件
- sample：示例代码
- z-lib：从旧项目迁移过来的组件
- z-sample：从旧项目迁移过来的示例代码

# 组件用法和设计

## 组件设计说明

本项目采用 **「核心抽象 + 引擎实现」** 的模块化设计模式：

- **core 模块**：提供最基础的通用能力，如工具类、基础 Activity/Fragment、常量定义等，所有业务模块均可依赖。
- **lib-xxx-core 模块**：定义该领域的接口抽象和通用逻辑，不绑定具体第三方实现。
- **lib-xxx-engine-yyy / lib-xxx-provider-yyy 模块**：基于具体的第三方库实现核心接口，方便按需替换。

例如：

- `lib-ad-core` 定义广告接口 → `lib-ad-provider-admob` /
  `lib-ad-provider-pangle` 提供具体实现
- `lib-store-engine-mmkv` / `lib-store-engine-datastore` 提供两种存储方案
- `lib-player-core` 定义播放器接口 → `lib-player-engine-media3` / `lib-player-engine-ijk` 提供具体实现

## Activity/Fragment基类

### 各基类职责

| 基类                            | 模块            | 核心能力                                                          |
|-------------------------------|---------------|---------------------------------------------------------------|
| `AbstractActivity`            | core          | 滑动返回、状态栏/导航栏适配、全屏模式、暗色模式、Activity 栈管理、进入/退出动画、双击退出            |
| `AbstractBindingActivity<VB>` | core          | 继承 AbstractActivity，自动完成 ViewBinding inflate 和 setContentView |
| `BaseMvvmActivity<DB, VM>`    | lib-mvvm      | 继承 AbstractActivity，自动创建 ViewBinding 和 ViewModel（反射泛型参数）      |
| `BaseActivity<DB, VM>`        | sample-common | 继承 BaseMvvmActivity，封装通用顶栏、状态栏适配、内容容器布局                       |
| `AbstractFragment`            | core          | Handler 封装、按键事件回调、setVisibility 便捷方法                          |
| `AbstractBindingFragment<VB>` | core          | 继承 AbstractFragment，自动完成 ViewBinding inflate                  |
| `BaseMvvmFragment<DB, VM>`    | lib-mvvm      | 继承 AbstractFragment，自动创建 ViewBinding 和 ViewModel              |
| `BaseFragment<DB, VM>`        | sample-common | 继承 BaseMvvmFragment，业务层通用 Fragment 基类                         |

### AbstractActivity 内置能力

`AbstractActivity` 是所有 Activity 的根基类，内置以下能力（均可通过 `open` 方法覆写控制）：

| 方法                     | 默认值     | 说明                          |
|------------------------|---------|-----------------------------|
| `isFullScreen()`       | `true`  | 是否沉浸式全屏（透明状态栏）              |
| `isDarkMode()`         | `false` | 暗色模式（状态栏图标/导航栏颜色自动适配）       |
| `enableEnterAnim()`    | `true`  | 是否启用进入动画                    |
| `enableExitAnim()`     | `true`  | 是否启用退出动画                    |
| `showTopBar()`         | `true`  | 是否显示顶栏                      |
| `adaptStatusBar(view)` | —       | 给指定 View 添加状态栏高度的 marginTop |
| `checkExit(runnable)`  | —       | 双击返回键退出，2 秒内再按则执行退出         |

- Activity 生命周期自动注册到 `ActivityHelper`（统一管理 Activity 栈）

### 使用方法

#### 1. 直接使用 AbstractActivity（最简方式）

最基础的方式，继承 AbstractActivity，和 Activity 一样使用

```kotlin
class SplashActivity : AbstractActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }
}
```

#### 2. 直接使用 AbstractBindingActivity（最简方式）

适用 ViewBinding 的方式，继承 AbstractBindingActivity

```kotlin
class FullScreenActivity : AbstractBindingActivity<EmptyPageBinding>() {
    override fun inflate() = EmptyPageBinding.inflate(layoutInflater)
    override fun init() {
        // mBinding 已自动绑定，直接使用
        adaptStatusBar(mBinding.btnTopView)  // 适配状态栏
    }
}
```

#### 3. 使用 BaseMvvmActivity（MVVM 架构）

需要 ViewModel 的页面，继承 `BaseMvvmActivity<DB, VM>`：

```kotlin
class UserActivity : BaseMvvmActivity<ActivityUserBinding, UserViewModel>() {
    override fun inflate() = ActivityUserBinding.inflate(layoutInflater)

    override fun init() {
        // mBinding 和 mViewModel 已自动创建
        mViewModel.userInfo.observe(this) { user ->
            mBinding.tvName.text = user.name
        }
        mViewModel.loadUser()
    }
}
class UserViewModel : BaseViewModel() {
    private val _userInfo = MutableLiveData<User>()
    val userInfo: LiveData<User> = _userInfo
    override fun init() {
        // 在 Activity#init() 之后调用
    }

    fun loadUser() {
        viewModelScope.launch {
            // 协程请求...
        }
    }
}
```

#### 4. 使用 BaseActivity（sample-common 封装）

`BaseActivity` 在 `BaseMvvmActivity` 基础上封装了通用顶栏和内容容器，业务页面只需提供子布局：
建议独立项目中使用，封装特定功能

```kotlin
class TitleActivity : BaseActivity<EmptyPageBinding, BaseViewModel>() {
    override fun inflateChildBinding() = EmptyPageBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("标题")  // 通用顶栏已封装
    }

    // 可选：隐藏顶栏
    override fun showTopBar(): Boolean = false

    // 可选：暗色模式
    override fun isDarkMode(): Boolean = true
}
```

## 广告组件

广告组件支持多广告平台接入、三种加载策略（单平台 / 瀑布流 /
竞价），并内置广告缓存、自动预加载、缓存过期等机制。

### 设计要点

1. **策略模式**：三种加载策略（单平台 / 瀑布流 / 竞价）通过 `ILoadModeStrategy` 接口解耦，运行时通过
   `AdCore.setLoadMode()` 切换，业务代码无需改动。

2. **工厂 + 注册机制**：`AdProviderFactory` 管理所有 Provider 的注册和创建，新增平台只需
   `registerProvider()` 后实现 `BaseAdProvider`，无需修改核心代码。

3. **模板方法模式**：`BaseAdProvider` 定义了 `doLoadAd()` → `loadSplashAd()` / `loadInterstitialAd()`
   的模板流程，子类只需实现具体平台的加载逻辑，缓存、日志、回调分发由基类统一处理。

4. **缓存优先 + 自动预加载**：广告加载后自动缓存（1 小时有效期），展示后自动预加载下一条，实现「使用 →
   关闭 → 预加载 → 就绪」的流水线，最大化广告填充率。

5. **竞价并行请求**：`BiddingModeStrategy` 使用 Kotlin 协程并行请求所有竞价 Provider，通过
   `withTimeout` 控制超时，超时后仍对已收集的结果执行竞价，保证响应速度。

6. **降级容错**：瀑布流和竞价策略在无可用 Provider 时自动降级为单平台策

### 广告生命周期与缓存机制

loadAd() → [缓存检查] → 命中? → onAdLoaded() (直接返回缓存)   
↓ 未命中   
[策略加载]→ Single/Waterfall/Bidding   
↓  
onAdLoaded() → cacheAd() (缓存广告 + 记录时间)   
↓   
showAd() → [缓存有效?] → 展示广告 → onAdShow()   
↓ 缓存过期   
清除缓存 → 自动重新加载   
↓ 用户关闭 → onAdDismiss() → removeCachedAd() (清除当前缓存)   
↓   
[自动预加载] → preload() → 加载下一条广告到缓存   
↓  
下次 showAd() → 缓存命中 → 直接展示

### 核心类说明

| 类/接口                    | 模块                       | 说明                                                            |
|-------------------------|--------------------------|---------------------------------------------------------------|
| `AdCore`                | lib-ad-core              | 门面单例，统一入口，管理 Provider 池、加载策略、缓存优先开关                           |
| `IAdProvider`           | lib-ad-core              | 广告平台提供者接口，定义 init / loadAd / showAd / isReady / destroy       |
| `BaseAdProvider`        | lib-ad-core              | 抽象基类，内置广告缓存、缓存过期检测、自动预加载、统一日志、回调分发                            |
| `AdProviderFactory`     | lib-ad-core              | 工厂 + 注册中心，管理 Provider 的注册、创建、配置查询                             |
| `ILoadModeStrategy`     | lib-ad-core              | 加载策略接口，定义 loadAd / preload / checkCache / getProviders        |
| `SingleModeStrategy`    | lib-ad-core              | 单平台策略：仅从当前活跃 Provider 加载                                      |
| `WaterfallModeStrategy` | lib-ad-core              | 瀑布流策略：按顺序依次尝试各 Provider，前一个失败则尝试下一个                           |
| `BiddingModeStrategy`   | lib-ad-core              | 竞价策略：并行请求所有支持竞价的 Provider，选择 eCPM 最高的胜出                       |
| `AdProviderConfig`      | lib-ad-core              | 广告配置数据类，包含 AppID、各广告位 ID、瀑布流/竞价开关                             |
| `IAdCallback`           | lib-ad-core              | 广告回调接口，所有方法均有默认空实现，按需覆写                                       |
| `AdType`                | lib-ad-core              | 广告类型枚举：SPLASH / INTERSTITIAL / REWARD_VIDEO / BANNER / NATIVE |
| `AdMobAdProvider`       | lib-ad-provider-admob    | Google AdMob 平台适配                                             |
| `PangleAdProvider`      | lib-ad-provider-pangle   | 穿山甲（Pangle）平台适配                                               |
| `BigoAdProvider`        | lib-ad-provider-bigo     | Bigo Ads 平台适配                                                 |
| `AppLovinAdProvider`    | lib-ad-provider-applovin | AppLovin 平台适配                                                 |

### 使用方法

#### 1. 初始化（Application.onCreate 中）

```kotlin
// 注册广告平台
AdCore.registerProvider(
    providerType = AdMobAdProvider.PROVIDER_NAME,
    providerClass = AdMobAdProvider::class.java,
    config = AdProviderConfig(
        appId = "ca-app-pub-xxxxxxxxxxxx",
        splashAdId = "ca-app-pub-xxx/xxx",
        interstitialAdId = "ca-app-pub-xxx/xxx",
        rewardVideoAdId = "ca-app-pub-xxx/xxx",
        bannerAdId = "ca-app-pub-xxx/xxx",
        supportWaterfall = true,
        supportBidding = true,
        biddingTimeout = 15000L
    )
)
// 可注册多个平台
AdCore.registerProvider(
    providerType = PangleAdProvider.PROVIDER_NAME,
    providerClass = PangleAdProvider::class.java,
    config = AdProviderConfig(
        appId = "pangle_app_id",
        rewardVideoAdId = "pangle_reward_id",
        supportWaterfall = true,
        supportBidding = true
    )
)
//注册多个平台...其他

// 初始化指定平台 
AdCore.init(context, AdMobAdProvider.PROVIDER_NAME) {
}

AdCore.init(context, PangleAdProvider.PROVIDER_NAME) {
}
//...

```

#### 2. 设置加载策略（可选，默认 SINGLE）可运行时修改

```kotlin
// 单平台模式（默认）
AdCore.setLoadMode(AdCore.LoadMode.SINGLE)
// 瀑布流模式：根据初始化顺序依次尝试 AdMob → Pangle → ...
AdCore.setLoadMode(AdCore.LoadMode.WATERFALL)
// 竞价模式：协程并行请求，最高 eCPM 胜出
AdCore.setLoadMode(AdCore.LoadMode.BIDDING)
```

#### 3. 加载广告

```kotlin
//加载不展示广告，有缓存且没过期直接成功，没缓触发加载，根据策略选择合适的加载机制
AdCore.loadAd(context, AdType.INTERSTITIAL, object : IAdCallback {
    override fun onAdLoadedWithPrice(eCPM: Double) {
        // 加载成功
    }
    override fun onAdFail(errorCode: Int, errorMessage: String) {
        // 加载失败 
    }
})
```

#### 4. 展示广告

//展示广告，有缓存直接展示，没缓存回调失败触发加载

```kotlin
AdCore.showAd(
    activity = activity,
    adType = AdType.INTERSTITIAL,
    callback = object : IAdCallback {
        override fun onAdFail(errorCode: Int, errorMessage: String) {
            // 展示失败
        }

        override fun onAdShow() {
            // 展示成功
        }

        override fun onAdClick() {
            // 点击
        }
        override fun onAdDismiss() {
            // 关闭
        }

        override fun onAdRewarded(rewardAmount: Int, rewardName: String) {
            // 激励成功
        }
    }
)
```

#### 5. 一步加载并展示

```kotlin
AdCore.loadAndShowAd(activity, AdType.INTERSTITIAL, callback)
```

### 自定义广告平台接入

实现一个新的广告平台只需三步：

1. 继承 BaseAdProvider, 实现具体平台逻辑

```kotlin
class MyAdProvider : BaseAdProvider() {
    companion object {
        const val PROVIDER_NAME = "MyAd"
    }

    override fun getProviderType() = PROVIDER_NAME

    override fun init(context: Context, config: AdProviderConfig, callback: (() -> Unit)?) {
        initInternal(realInit = {
            // 初始化第三方 SDK
            MyAdSDK.init(context, config.appId) {
                finishInit(callback)  // 必须调用，标记初始化完成
            }
        }, callback = callback)
    }

    override fun loadRewardedAd(context: Context, adId: String, callback: IAdCallback?) {
        MyAdSDK.loadRewardedAd(adId, object : MyAdCallback {
            override fun onLoaded(ad: MyAd) {
                // 调用基类方法，自动缓存 + 回调
                handleOnAdLoaded(AdType.REWARD_VIDEO, ad, callback)
            }
            override fun onFailed(error: MyAdError) {
                handleOnAdLoadFail(AdType.REWARD_VIDEO, error.code, error.msg, callback)
            }

        })
    }

    override fun showRewardedAd(activity: Activity, callback: IAdCallback?) {
        rewardAd.show(activity, object : MyAdCallback {
            override fun onShowed() {
                handleOnAdShow(AdType.REWARD_VIDEO, callback)
            }
            override fun onShowed() {
                handleOnAdShow(AdType.REWARD_VIDEO, callback)
            }
            override fun onDismissed() {
                // 自动清除缓存 + 回调 + 触发预加载
                handleAdDismissed(AdType.REWARD_VIDEO, callback)
            }
            override fun onRewarded(amount: Int, name: String) {
                handleOnAdRewarded(AdType.REWARD_VIDEO, amount, name, callback)
            }
        })
    }

    // 其他广告类型同理：loadSplashAd / loadInterstitialAd / loadBannerAd ...

    override fun onDestroy() {
        // 释放第三方 SDK 资源
    }
}
```

2. 注册

```kotlin

AdCore.registerProvider(
    providerType = MyAdProvider.PROVIDER_NAME,
    providerClass = MyAdProvider::class.java,
    config = AdProviderConfig(
        appId = "my_app_id",
        rewardVideoAdId = "my_reward_id",
        supportWaterfall = true,
        supportBidding = true
    )
)

```

3. 初始化

```kotlin
AdCore.init(context, MyAdProvider.PROVIDER_NAME) { // 就绪 
}
```

## 权限组件

权限组件采用 **门面 + 策略 + 引擎** 三重设计模式，支持运行时权限请求、Android
各版本自动适配、多引擎无缝切换（内置 / PermissionX），并内置「说明理由 → 请求 → 拒绝 → 跳转设置」完整流程。

### 设计要点

1. **引擎可替换**：`IPermissionEngine` 接口定义权限引擎契约，`PermissionCore.init()` 可在运行时切换底层实现（
   `DefaultEngine` 自研 / `PermissionXEngine`），业务代码零改动。

2. **策略模式自动版本适配**：`PermissionStrategy` 接口封装各权限在不同 Android 版本下的差异（如
   Android 13 细粒度媒体权限、Android 12 蓝牙新权限、Android 10 后台位置权限等），调用方只需指定策略，无需关心版本分支。

3. **链式构建器**：`RequestBuilder` 提供流畅的链式 API（
   `permissions() → explainReason() → forwardToSettings() → onAllGranted() → onDenied() → request()`
   ），支持说明理由弹窗、跳转设置弹窗等高级场景。

4. **三态回调**：`PermissionResultCallback` 区分「全部授予」「部分拒绝」「总是拒绝（不再询问）」三种状态，
   `onAlwaysDenied` 默认弹出 `JumpPermissionSettingDialog` 引导用户跳转设置页。

5. **ActivityResultContract**：基于 Jetpack `ActivityResultContracts` 注册权限请求，替代废弃的
   `requestPermissions()` API，生命周期安全。

### 核心类说明

| 类/接口                          | 模块                                | 说明                                                             |
|-------------------------------|-----------------------------------|----------------------------------------------------------------|
| `PermissionCore`              | core                              | 门面单例，统一入口，管理引擎初始化、链式 API、便捷检查方法                                |
| `IPermissionEngine`           | core                              | 权限引擎接口，定义 createLauncher / isGranted / areAllGranted / destroy |
| `IPermissionLauncher`         | core                              | Launcher 接口，定义 request / requestByStrategy / requireContext    |
| `DefaultEngine`               | core                              | 默认引擎实现，基于自研 PermissionLauncher + ActivityResultContract，无第三方依赖 |
| `PermissionXEngine`           | lib-permission-engine-permissionx | PermissionX 引擎实现，委托 PermissionX.init() 发起请求                    |
| `PermissionLauncher`          | core                              | 权限请求核心组件，注册单/多权限 Launcher，处理结果分发和 always denied 判断             |
| `PermissionStrategy`          | core                              | 权限策略接口，getPermissions() + shouldSkipRequest()，封装版本适配逻辑         |
| `JumpPermissionSettingDialog` | core                              | 跳转设置弹窗，确认后调用 PermissionHelper 跳转系统权限设置页                        |
| `WhyRequestPermissionDialog`  | core                              | 说明理由弹窗，用于 explainReason 场景                                     |

### 内置权限策略

| 策略                                     | 适配版本                                                                                                                                                    | 请求的权限  |
|----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|--------|
| `MediaPermissionStrategy`              | Android 13+: `READ_MEDIA_IMAGES` + `READ_MEDIA_VIDEO` + `READ_MEDIA_AUDIO`<br>Android 12-: `READ_EXTERNAL_STORAGE`                                      | 媒体资源访问 |
| `StoragePermissionStrategy`            | Android 13+: 细粒度媒体权限<br>Android 10-12: `READ_EXTERNAL_STORAGE`<br>Android 9-: `READ_EXTERNAL_STORAGE` + `WRITE_EXTERNAL_STORAGE`                        | 存储访问   |
| `NotificationPermissionStrategy`       | Android 13+: `POST_NOTIFICATIONS`<br>Android 8-12: 跳转通知设置页（shouldSkipRequest）<br>Android 7.1-: 无需权限                                                     | 通知推送   |
| `BackgroundLocationPermissionStrategy` | Android 10+: `ACCESS_BACKGROUND_LOCATION`<br>Android 9-: 无独立权限（shouldSkipRequest）                                                                       | 后台定位   |
| `BluetoothPermissionStrategy`          | Android 12+: `BLUETOOTH_SCAN` + `BLUETOOTH_CONNECT` + `BLUETOOTH_ADVERTISE`<br>Android 11-: `BLUETOOTH` + `BLUETOOTH_ADMIN` + `ACCESS_FINE_LOCATION`    | 蓝牙     |
| `NearbyDevicesPermissionStrategy`      | Android 13+: `NEARBY_WIFI_DEVICES`<br>Android 12: `NEARBY_DEVICES`<br>Android 11-: 无独立权限（shouldSkipRequest）                                             | 邻近设备   |
| `SensorPermissionStrategy`             | Android 13+: `BODY_SENSORS` + `BODY_SENSORS_BACKGROUND`<br>Android 10-12: `BODY_SENSORS` + `ACTIVITY_RECOGNITION`<br>Android 9-: `ACTIVITY_RECOGNITION` | 传感器    |

### 使用方法

#### 直接使用 PermissionLauncher 组件（轻量）

```kotlin
class MyFragment : Fragment() {
    // ✅ 必须在属性声明时创建（registerForActivityResult 要求在 STARTED 之前注册）
    private val permissionLauncher = PermissionLauncher(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 基础用法：直接传权限数组
        btnCamera.setOnClickListener {
            permissionLauncher.request(
                permissions = arrayOf(Manifest.permission.CAMERA),
                onAllGranted = { toast("相机权限已授予") },
                onDenied = { toast("相机权限被拒绝") },
            )
        }

        // 策略模式：自动版本适配
        btnBluetooth.setOnClickListener {
            permissionLauncher.requestByStrategy(
                strategy = StoragePermissionStrategy,
                onAllGranted = { toast("存储权限已授予") },
                onDenied = { deniedList -> toast("存储权限被拒绝: $deniedList") },
            )
        }

        // 高级用法：自定义 always denied 弹窗
        btnAdvanced.setOnClickListener {
            permissionLauncher.request(
                strategy = StoragePermissionStrategy,
                onAllGranted = { toast("存储权限已授予") },
                onDenied = { toast("存储权限被拒绝") },
                onAlwaysDenied = { _, context ->
                    JumpPermissionSettingDialog(
                        context,
                        title = "需要存储权限",
                        message = "您选择了'不再询问'，请前往设置手动开启"
                    ).show()
                }
            )
        }
    }
}
```

#### 方式二：使用 PermissionCore 引擎模式（可替换底层引擎）

1. 初始化引擎（Application.onCreate 中，可选，默认使用 DefaultEngine

```kotlin
PermissionCore.init { DefaultEngine() } // 自研（默认） 
//PermissionCore.init { PermissionXEngine() } // PermissionX
```

2. 创建PermissionLauncher，后续用法同 方式一

```kotlin
// ✅ 必须在属性声明时创建
private val permissionLauncher = PermissionCore.with(this)
```

## 网络组件

网络组件采用 **门面 + 引擎注册 + 拦截器链** 三重设计模式，支持多 HTTP 引擎无缝切换（OkHttp /
HttpURLConnection / 未来 Ktor），内置协程优先 API、密封类异常体系、灵活的响应体字段适配（注解 / 字段名 /
函数式提取器三级优先级），以及 失败重试、公共请求头、请求日志等拦截器。

### 设计要点

1. **引擎注册制**：`HttpEngine` 接口定义引擎契约，`EngineRegistry` 管理所有引擎的注册和创建。核心库不含任何引擎实现，各引擎模块通过
   `companion init{}` 自动注册，引入依赖即生效，切换引擎只需改一行 `engine("okhttp")`。

2. **引擎无关拦截器链**：`NetInterceptor` + `NetChain` 在引擎抽象层之上运行，所有引擎共享同一套拦截器。内置
   `RetryInterceptor`（线性退避重试）、`HeaderInterceptor`（公共请求头）、`LoggerInterceptor`（请求日志）。

3. **协程优先 API**：`NetCore.get<T>()` / `post<T>()` 等方法均为 `suspend inline reified` 函数，自动在
   `Dispatchers.IO` 上执行，通过 `TypeToken` 保留完整泛型类型信息，解决 Java 泛型擦除问题。同时提供
   `NetCall.await()` 支持协程取消自动取消底层请求。

4. **失败响应自动构造**：当请求失败时，通过 `FailureResponseFactory` 反射创建 `responseClass`
   实例并填充错误信息，业务层永远拿到非 null 响应对象，无需 try-catch。

### 核心类说明

| 类/接口                | 模块                        | 说明                                                                                 |
|---------------------|---------------------------|------------------------------------------------------------------------------------|
| `NetCore`           | lib-network-core          | 门面单例，统一入口，提供 get/post/put/delete/patch 协程 API、newCall 异步 API、配置管理                  |
| `HttpEngine`        | lib-network-core          | HTTP 引擎接口，定义 execute / newCall / shutdown                                          |
| `EngineRegistry`    | lib-network-core          | 引擎注册表，管理引擎注册、默认引擎设置、实例创建                                                           |
| `EngineConfig`      | lib-network-core          | 引擎基础配置（connectTimeout / readTimeout / writeTimeout），各引擎可继承扩展                       |
| `NetRequest`        | lib-network-core          | 引擎无关请求模型，Builder DSL 构建，支持 headers / params / body / tag / timeout                 |
| `NetResponse`       | lib-network-core          | 引擎无关响应模型，提供 isSuccessful / isRedirect / isClientError / isServerError / bodyString |
| `NetCall`           | lib-network-core          | 抽象调用接口，支持 execute（同步）/ enqueue（回调）/ await（协程）/ cancel                              |
| `NetInterceptor`    | lib-network-core          | 应用层拦截器接口，与 OkHttp Interceptor 类似但引擎无关                                              |
| `NetChain`          | lib-network-core          | 拦截器链，管理执行顺序，proceed() 传递到下一环节或引擎执行                                                 |
| `IBaseResponse`     | lib-network-core          | 业务响应抽象接口，                                                                          |
| `AuthInterceptor`   | lib-network-core          | Token 认证拦截器，自动注入 Authorization 头，401 时自动刷新 Token 并重发                               |
| `RetryInterceptor`  | lib-network-core          | 重试拦截器，支持线性退避，仅对网络层错误重试（业务错误不重试）                                                    |
| `HeaderInterceptor` | lib-network-core          | 公共请求头拦截器，自动注入全局 headers                                                            |
| `LoggerInterceptor` | lib-network-core          | 日志拦截器，打印请求/响应信息                                                                    |
| `OkHttpEngine`      | lib-network-engine-okhttp | OkHttp 引擎实现，利用 OkHttp 连接池、HTTP/2、拦截器等原生能力                                          |
| `OkHttpConfig`      | lib-network-engine-okhttp | OkHttp 专属配置，扩展拦截器、认证器、缓存、连接池、协议等                                                   |
| `OkHttpCall`        | lib-network-engine-okhttp | OkHttp Call 实现，委托 okhttp3.Call 提供同步/异步/取消能力                                        |

### 使用方法

#### 1. 初始化（Application.onCreate 中）

```kotlin
NetCore.init {
    // 使用公开测试 API
    baseUrl("https://www.wanandroid.com")

    // 选择 OkHttp 引擎
    engine(OkHttpEngine.ENGINE_NAME) {
        // OkHttp 专属配置
        (this as? OkHttpConfig)?.apply {
            connectionPool(5, 5, java.util.concurrent.TimeUnit.MINUTES)
            retryOnConnectionFailure(true)
//                    addInterceptor("LoggingInterceptor")
//                    addNetworkInterceptor("LoggingInterceptor")
        }
    }

    // 公共请求头
    header("App-Version", "1.0.0")

    // 启用日志
    enableLog(true)

    // 设置统一业务响应类型
    responseClass(BaseResponse::class.java)
}
```

#### 2. 定义业务响应模型

```kotlin
/**
 * 统一业务响应体（演示非标准字段名：errorCode / errorMsg）
 *
 * 通过 @ResponseCode / @ResponseMsg / @ResponseData 注解适配不同服务端字段名
 */
data class BaseResponse<T>(
    @ResponseCode val errorCode: Int = -1,
    @ResponseMsg val errorMsg: String = "",
    @ResponseData val data: T? = null
) : IBaseResponse {
    override fun getResponseCode(): Int = errorCode
    override fun getResponseMsg(): String = errorMsg
}
```

#### 3. 请求示例

- GET 请求示例（协程方式 - 高层封装）

```kotlin
val resp = NetCore.get<BaseResponse<List<BannerData>>>("banner/json")
if (resp.isSuccess()) {
    val data = resp.data
    if (data != null) {
        log("OkHttp-Sample", "response = ${data.toJson()}")
    }
} else {
    logE("业务失败: code=${resp.errorCode}, msg=${resp.errorMsg}")
}
```

- POST 请求示例（协程方式 - 高层封装）

```kotlin
val resp = NetCore.post<BaseResponse<TokenData>>(
    "user/login",
    bodyData = mapOf("username" to "allever", "password" to "123456")
)
```

- 异步请求 - 回调方式 (enqueue)

```kotlin
val currentCall = NetCore.newCall(HttpMethod.GET, "banner/json") {
    header("X-Request-Type", "okhttp-callback")
}

currentCall!!.enqueue(object : NetCallback {
    override fun onSuccess(response: NetResponse) { // 手动反序列化业务数据
        // 手动反序列化业务数据
        response.body?.let { bytes ->
            @Suppress("UNCHECKED_CAST")
            val resp = NetCore.config.converter.convert(
                bytes,
                BaseResponse::class.java
            ) as? BaseResponse<*>
            if (resp != null && resp.errorCode == 0) {
                log("OkHttp-Sample", "Banner 数据: ${resp.data?.toJson()}")
            }
        }
    }

    override fun onFailure(exception: Exception) {
        logE("OkHttp-Sample", "enqueue 失败: ${exception.message}")
    }
})
```

- 异步请求 - 协程方式 (await)

```kotlin
val call = NetCore.newCall(HttpMethod.GET, "banner/json") { }

val response = call.await()

log("OkHttp-Sample", "await 成功! HTTP ${response.code}, 耗时 ${response.elapsedMs}ms")

// 手动反序列化业务数据
val parsed = response.toObject(NetCore.config.converter, BaseResponse::class.java)
        as? BaseResponse<*>

if (parsed != null && parsed.errorCode == 0) {
    log("OkHttp-Sample", "Banner 数据: ${parsed.data?.toJson()}")
} else {
    logE("业务失败: code=${parsed?.errorCode}, msg=${parsed?.errorMsg}")
}
```

## 媒体组件

媒体库组件，提供统一的 API 查询系统媒体库中的图片、视频、音频资源，
支持任意类型组合查询、目录分组、分页加载。  
三种返回方式：

- suspend 函数：直接返回原始数据，适合一次性操作
- Flow：流式/逐页/自动刷新，适合列表加载和 Compose UI
- LiveData：UI 直接观察，适合传统 Activity/Fragment

### 使用方法

#### 全量查找

```kotlin 
//1. 全量按目录｜类型查询
val folders = MediaCore.queryFolders {
    types = setOf(MediaType.ALL)//setOf(MediaType.IMAGE, MediaType.VIDEO，MediaType.AUDIO)
}

//2. 全量查询不区分目录
val items = MediaCore.queryAll {
    types = setOf(MediaType.Type.IMAGE)//setOf(MediaType.IMAGE, MediaType.VIDEO，MediaType.AUDIO)
}
```

#### 分页查找

```kotlin
//分页查询
val folders = MediaCore.queryFolders {
    types = MediaType.ALL;
    pagination = Pagination.Paged(page, pageSize);
    sortBy = SortBy.DATE_DESC
}
showToast("第${page + 1}页: ${folders.size} 个目录")
```

#### LiveData 观察目录

```kotlin
 MediaCore.queryFoldersLiveData {
    types = MediaType.ALL;
    pagination = Pagination.All;
    sortBy = SortBy.DATE_DESC
}.observe(viewLifecycleOwner) { folders ->
    log("LiveData: ${folders.size} 个目录")
}
```

#### 流式查找

```kotlin
MediaCore.queryFoldersFlow {
    types = MediaType.ALL;
    pagination = Pagination.Paged(0, 3)
    sortBy = SortBy.DATE_DESC
}.catch { e -> logE("MediaSample", "queryFoldersFlow error: ${e.message}") }
    .collect { pageFolders ->
        pageCount++
        totalFolders += pageFolders.size
        log("MediaSample", "Flow 第${pageCount}页: ${pageFolders.size}个 | 累计:$totalFolders")
        for (f in pageFolders) {
            log(
                "MediaSample",
                "  [${f.name}] img:${f.images.size} vid:${f.videos.size} aud:${f.audios.size}"
            )
        }
    }
showToast("Flow 完成: $pageCount 页, $totalFolders 个目录")
```

## 媒体选择器

在媒体库基础上，封装一个选择器，提供统一 API，方便快速集成媒体选择功能。支持分类型，分目录、预览功能。

### 使用方法

#### 创建Launcher以及回调

```kotlin
    private val mediaPickerLauncher = MediaPickerCore.registerPickerLauncher(this) { items ->
    if (items.isNotEmpty()) {
        log("选择器返回 ${items.size} 项:\n")
    } else {
        toast("未选择任何资源")
    }
}
```

#### 启动选择器

```kotlin
//1.多类型多选
mediaPickerLauncher.launch(
    MediaPickerConfig(
        types = setOf(MediaType.Type.IMAGE, MediaType.Type.VIDEO, MediaType.Type.AUDIO),
        maxSelect = 9,
    )
)

//2.快捷方式 & 默认单选
MediaPickerCore.launchVideo(mediaPickerLauncher)
MediaPickerCore.launchImage(mediaPickerLauncher)
MediaPickerCore.launchAudio(mediaPickerLauncher, maxSelect = 9)
```

## 播放器组件

### 设计要点

内核层 + 渲染层 + UI控制层 组合方式

#### 内核层-IPlayerEngine

- 管理播放引擎的完整生命周期（初始化 → 准备 → 播放 → 暂停 → 停止 → 释放）
- 处理数据源设置（url/file/assets/uri）
- 提供播放控制（play、pause、stop、seek、speed、volume）
- 查询播放状态和进度

#### 渲染层-IVideoRender

- 管理 Surface 的创建和生命周期
- 处理 Surface 的就绪/销毁回调
- 提供布局自适应能力，缩放模式
- 暴露 Surface 供引擎绑定

#### UI控制层-IVideoUiController

- 负责提供UI操作

### 核心类

- IPlayerEngine：播放内核，内置MediaPlayerEngine，使用MediaPlayer内核
- IVideoRender：视频渲染，内置TextureView实现，SurfaceView实现, VideoView实现
- IVideoUiController：UI控制，内置StdController实现
- VideoPlayer：视频播放器协调器（组合模式），组合 [IPlayerEngine]（引擎）和 [IVideoRender],
  （渲染器）完成视频播放。引擎和渲染器可以独立替换，实现完全解耦。
- StdVideoPlayer：内置含UI层的视频播放器，集成了 [VideoPlayer]、[IVideoUiController]
  。内置触摸控制，包含双击播放/暂停、滑动调节音量、亮度、进度控制。缩放模式、循环模式、倍速、进度条、时间轴。

### 使用方法

#### 1.使用VideoPlayer播放器

1. 创建渲染层容器

```xml
<!--视频显示区域（渲染器会在此容器中添加视图）渲染器会动态将 SurfaceView / TextureView / VideoView 添加到此处-->
<FrameLayout android:id="@+id/containerVideo" android:layout_width="match_parent"
    android:layout_height="300dp" android:background="#222222"
    android:layout_marginBottom="4dp"></FrameLayout>
```

2. 初始化播放器

```kotlin
// 使用默认配置：MediaPlayerEngine + SurfaceViewRender
player = VideoPlayer(
    engine = MediaPlayerEngine(),
    render = SurfaceViewRender()
).apply {
    attach(mBinding.containerVideo)
    setListener(playerListener)
    retryCount = 3
    progressIntervalMs = 200
}
```

3. 设置数据源

```kotlin
// 设置数据源, 支持url、file、assets、uri，内部自用准备
player.setSource(sourceUrl)
```

4. 播放控制

```kotlin
player.play()
player.pause()
player.stop()
player.seekTo(position)
player.setVolume(volume)
player.setSpeed(speed)
player.release()
```

#### 2.使用StdVideoPlayer播放器-使用默认ui控制器

1. 添加播放器到布局或代码中创建

```xml

<app.allever.android.lib.player.core.player.StdVideoPlayer android:id="@+id/stdVideoPlayer"
    android:layout_width="match_parent" android:layout_height="match_parent" />
```

2. (可选)设置监听器回调

```kotlin
mBinding.stdVideoPlayer.setListener(object : IVideoPlayerViewListener {

})
```

3. 设置数据源

```kotlin
// 设置数据源, 支持url、file、assets、uri，内部自动准备并播放
mBinding.stdVideoPlayer.setSource(sourceUrl)
```

4. 播放控制
   在UI层控制

5. 切换渲染器

```kotlin
stdVideoPlayer.switchRender(VideoViewRender.NAME)
stdVideoPlayer.switchRender(TextureViewRender.NAME)
stdVideoPlayer.switchRender(SurfaceViewRender.NAME)
```

6. 切换内核引擎

```kotlin
stdVideoPlayer.switchEngine(MediaPlayerEngine.NAME)
stdVideoPlayer.switchEngine(Media3PlayerEngine.NAME)
stdVideoPlayer.switchEngine(IjkPlayerEngine.NAME)
```

7. 释放播放器
   内部自动释放

#### 3.使用StdVideoPlayer播放器-自定义ui控制器

1. 创建自定义UI控制器，继承 [IVideoUiController]，实现相关获取控件方法和更新回调方法

```kotlin
class CustomStdVideoController : IVideoUiController {

    private val binding =
        CustomStdUiControllerBinding.inflate(LayoutInflater.from(context), null, false)

    override fun getTitleView(): TextView? {
        return binding.tvTitle
    }

    override fun getBackView(): View? {
        //如果没有返回按钮，则返回null
        return null
    }

    override fun onProgressChanged(
        position: Long,
        duration: Long,
        progress: Int
    ) {
        binding.seekBar.progress = progress
        binding.tvCurrentTime.text = formatTime(position)
        binding.tvDuration.text = formatTime(duration)
    }
}
```

2. 创建自定义播放器，继承 [StdVideoPlayer]，并设置自定义UI控制器

```kotlin
class CustomStdVideoPlayer(context: Context, attrs: AttributeSet? = null) :
    StdVideoPlayer(context, attrs, 0) {

    override fun bindUiController(): IVideoUiController {
        return CustomStdVideoController(mContext)// 也可以使用内置的控制器
    }

    override fun enableWidget() {
        //做一些显示的初始化
    }

    override fun initView() {
        //初始化特有的功能
    }
}
```
