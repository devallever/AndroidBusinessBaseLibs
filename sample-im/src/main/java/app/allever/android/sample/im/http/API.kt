package app.allever.android.sample.im.http

class API {
    companion object {
        //root
        const val ROOT = "/"

        const val USER_INFO = "/api/user"
        const val REGISTER = "/api/user/register"
        const val LOGIN = "/api/user/login"
        const val LOGOUT = "/api/user/logout"
        const val USER_QUERY = "/api/user/query"
        const val USER_LIST = "/api/user/list"
        const val USER_STATUS = "/api/user/status"
        const val USER_ONLINE = "/api/user/onlineList"

        //echo
        const val ECHO = "/api/echo"

        //image
        const val IMAGE = "/api/image"
        const val IMAGE_UPLOAD = "/api/image/upload"
        const val IMAGE_LIST = "/api/image/list"
        const val IMAGE_DELETE = "/api/image/delete"
    }
}