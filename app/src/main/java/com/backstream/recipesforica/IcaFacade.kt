package com.backstream.recipesforica

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext

class IcaFacade private constructor() : CoroutineScope {
    private val client = HttpClient {
        install(Auth) {
            basic {
                username = ""
                password = ""
                sendWithoutRequest = true
            }
        }
    }
    private val host = "https://handla.api.ica.se"

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    fun authenticate(onSuccess: (ticket: String) -> Unit) {
        lateinit var result: HttpResponse
        async {
            result = client.get<HttpResponse>("$host/api/login/")
        }.invokeOnCompletion {
            if (it != null) {
                println(it)
            } else {
                val ticket = result.headers.get("AuthenticationTicket")
                if (ticket == null) {
                    Log.e(javaClass.name, "Got no ticket!")
                } else {
                    onSuccess.invoke(ticket)
                }
            }
        }
    }

    companion object {
        val instance = IcaFacade()
    }
}