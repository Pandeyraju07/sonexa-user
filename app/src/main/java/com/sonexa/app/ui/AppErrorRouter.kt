package com.sonexa.app.ui

/**
 * Maps failures from repositories / ViewModels to AppStep error destinations.
 */
object AppErrorRouter {
    fun isNetworkFailure(throwable: Throwable?): Boolean {
        if (throwable == null) return false
        val msg = (throwable.message ?: "").lowercase()
        val causeMsg = (throwable.cause?.message ?: "").lowercase()
        return throwable is java.net.UnknownHostException ||
            throwable is java.net.ConnectException ||
            throwable is java.net.SocketTimeoutException ||
            throwable is java.io.IOException ||
            msg.contains("unable to resolve host") ||
            msg.contains("failed to connect") ||
            msg.contains("timeout") ||
            msg.contains("network") ||
            causeMsg.contains("unable to resolve host") ||
            causeMsg.contains("failed to connect")
    }

    fun stepFor(throwable: Throwable?): AppStep =
        if (isNetworkFailure(throwable)) AppStep.NO_INTERNET_ERROR else AppStep.SERVER_ERROR

    fun stepForMessage(message: String?): AppStep {
        val msg = (message ?: "").lowercase()
        return if (
            msg.contains("unable to resolve host") ||
            msg.contains("failed to connect") ||
            msg.contains("timeout") ||
            msg.contains("network") ||
            msg.contains("unreachable")
        ) {
            AppStep.NO_INTERNET_ERROR
        } else {
            AppStep.SERVER_ERROR
        }
    }
}
