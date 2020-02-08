package com.backstream.recipesforica

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import java.nio.charset.Charset
import kotlin.coroutines.CoroutineContext

class IcaFacade private constructor() : CoroutineScope {

    private lateinit var ticket: String

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

    fun authenticate() {
        lateinit var result: HttpResponse
        async {
            result = client.get("$host/api/login/")
        }.invokeOnCompletion {
            if (it != null) {
                println(it)
            } else {
                val ticket = result.headers.get("AuthenticationTicket")
                if (ticket == null) {
                    Log.e(javaClass.name, "Got no ticket!")
                } else {
                    this.ticket = ticket
                }
            }
        }
    }

    fun shoppingLists(onSuccess: (result: String) -> Unit) {

        val builder = HttpRequestBuilder()
        builder.header("AuthenticationTicket", ticket)
        builder.url("$host/api/user/shoppinglists/")
        "$host/api/user/shoppinglists/"

        lateinit var result: HttpResponse
        async {
            result = client.get(builder)
        }
            .invokeOnCompletion {
                if (it != null) {
                    println(it)
                } else {
                    var resultText: String? = null
                    async {
                        resultText = result.readText(Charset.defaultCharset())
                    }.invokeOnCompletion { t ->
                        if (t != null) {
                            println(it)
                        } else {
                            onSuccess.invoke(resultText!!)
                        }
                    }
                }

            }
    }

    companion object {
        val instance = IcaFacade()
    }
}