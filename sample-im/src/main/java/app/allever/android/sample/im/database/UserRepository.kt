package app.allever.android.sample.im.database

import app.allever.android.sample.im.database.entity.UserEntity

object UserRepository {

    private val userDao = AppDatabase.getInstance().userDao()

    /**
     * 注册
     * @return 成功返回用户ID，失败返回 null（用户名重复/参数非法）
     */
    fun register(username: String, password: String): Long? {
        if (username.isBlank() || password.isBlank()) return null
        if (username.length < 3 || username.length > 20) return null
        if (password.length < 6 || password.length > 32) return null

        val passwordHash = PasswordUtils.hash(password)
        val user = UserEntity(username = username, passwordHash = passwordHash)
        val rowId = userDao.insertUser(user)
        return if (rowId == -1L) null else rowId
    }

    /**
     * 登录校验
     * @return 成功返回用户信息，失败返回 null
     */
    fun login(username: String, password: String): UserEntity? {
        if (username.isBlank() || password.isBlank()) return null
        val user = userDao.getUserByUsername(username) ?: return null
        if (!PasswordUtils.matches(password, user.passwordHash)) return null
        return user
    }

    /**
     * 检查用户是否存在
     */
    fun isUserExists(username: String): Boolean {
        return getUserByUsername(username) != null
    }

    fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }
}