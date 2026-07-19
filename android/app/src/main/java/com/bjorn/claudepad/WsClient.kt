package com.bjorn.claudepad

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WsClient {

    private val client = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS)
        .connectTimeout(4, TimeUnit.SECONDS)
        .build()

    private var ws: WebSocket? = null

    @Volatile var connected = false
        private set

    /** true = terhubung & terautentikasi, false = putus/gagal (dengan pesan) */
    var onState: ((Boolean, String) -> Unit)? = null
    /** pesan JSON lain dari server (mis. clipboard) */
    var onMessage: ((JSONObject) -> Unit)? = null

    fun connect(host: String, port: Int, pin: String) {
        disconnect()
        val req = Request.Builder().url("ws://" + host + ":" + port).build()
        ws = client.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send(JSONObject().put("t", "auth").put("pin", pin).toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val o = try { JSONObject(text) } catch (e: Exception) { return }
                when (o.optString("t")) {
                    "auth_ok" -> { connected = true; onState?.invoke(true, "Terhubung") }
                    "auth_fail" -> {
                        connected = false
                        onState?.invoke(false, "PIN salah")
                        webSocket.close(1000, null)
                    }
                    else -> onMessage?.invoke(o)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                connected = false
                onState?.invoke(false, "Gagal: " + (t.message ?: "tidak diketahui"))
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                connected = false
                onState?.invoke(false, "Terputus")
            }
        })
    }

    fun disconnect() {
        connected = false
        ws?.close(1000, null)
        ws = null
    }

    fun send(o: JSONObject) { ws?.send(o.toString()) }

    fun send(t: String) = send(JSONObject().put("t", t))

    fun move(dx: Int, dy: Int) =
        send(JSONObject().put("t", "move").put("dx", dx).put("dy", dy))

    fun click(b: String, double: Boolean = false) =
        send(JSONObject().put("t", "click").put("b", b).put("double", double))

    fun buttonDown(b: String) = send(JSONObject().put("t", "down").put("b", b))
    fun buttonUp(b: String) = send(JSONObject().put("t", "up").put("b", b))

    fun scroll(dy: Int) = send(JSONObject().put("t", "scroll").put("dy", dy))

    fun text(s: String) = send(JSONObject().put("t", "text").put("s", s))

    fun key(k: String, mods: List<String> = emptyList()) {
        val o = JSONObject().put("t", "key").put("k", k)
        if (mods.isNotEmpty()) o.put("mods", org.json.JSONArray(mods))
        send(o)
    }

    fun media(a: String) = send(JSONObject().put("t", "media").put("a", a))

    fun clipSet(s: String) = send(JSONObject().put("t", "clipset").put("s", s))
    fun clipGet() = send("clipget")
}
