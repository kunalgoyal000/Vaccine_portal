package com.kunalapps.vaccinePortal.networking

import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
            .removeHeader("User-Agent")
            .addHeader("User-Agent", USER_AGENT)
            .build()
        return chain.proceed(requestWithUserAgent)
    }
}