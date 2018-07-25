package com.renyu.nimapp.bean

data class Resource<T>(val status: Status, val data: T?, val message: String?, val code:Int?) {
    companion object {
        fun <T> sucess(data: T?): Resource<T> {
            return Resource(Status.SUCESS, data, null, null)
        }

        fun <T> failed(code: Int?): Resource<T> {
            return Resource(Status.FAIL, null, null, code)
        }

        fun <T> exception(message: String?): Resource<T> {
            return Resource(Status.Exception, null, message, null)
        }

        fun <T> loading(): Resource<T> {
            return Resource(Status.LOADING, null, null, null)
        }
    }
}