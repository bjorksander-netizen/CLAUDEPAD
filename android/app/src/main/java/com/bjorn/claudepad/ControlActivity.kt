package com.bjorn.claudepad

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ControlActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private var suppressWatcher = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        tvStatus = findViewById(R.id.tvStatus)
        tvStatus.text = "Terhubung"

        WsClient.onState = { ok, msg ->
            runOnUiThread {
                tvStatus.text = msg
                if (!ok) finish()
            }
        }
        WsClient.onMessage = { o ->
            if (o.optString("t") == "clip") {
                val s = o.optString("s")
                runOnUiThread {
                    val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("PC", s))
                    toast("Clipboard PC disalin ke HP")
                }
            }
        }

        // ---------------- Trackpad ----------------
        findViewById<TrackpadView>(R.id.trackpad).listener = object : TrackpadView.Listener {
            override fun onMove(dx: Int, dy: Int) = WsClient.move(dx, dy)
            override fun onLeftClick() = WsClient.click("left")
            override fun onRightClick() = WsClient.click("right")
            override fun onScroll(notches: Int) = WsClient.scroll(notches)
            override fun onDragStart() = WsClient.buttonDown("left")
            override fun onDragEnd() = WsClient.buttonUp("left")
        }

        // ---------------- Tombol mouse ----------------
        findViewById<Button>(R.id.btnLeft).setOnClickListener { WsClient.click("left") }
        findViewById<Button>(R.id.btnMiddle).setOnClickListener { WsClient.click("middle") }
        findViewById<Button>(R.id.btnRight).setOnClickListener { WsClient.click("right") }

        // ---------------- Tombol spesial ----------------
        mapOf(
            R.id.kEsc to "esc", R.id.kTab to "tab", R.id.kWin to "win",
            R.id.kEnter to "enter", R.id.kBksp to "backspace", R.id.kDel to "delete",
            R.id.kUp to "up", R.id.kDown to "down", R.id.kLeft to "left", R.id.kRight to "right"
        ).forEach { (id, key) ->
            findViewById<Button>(id).setOnClickListener { WsClient.key(key) }
        }
        findViewById<Button>(R.id.kAltTab).setOnClickListener { WsClient.key("tab", listOf("alt")) }
        findViewById<Button>(R.id.kCtrlC).setOnClickListener { WsClient.key("c", listOf("ctrl")) }
        findViewById<Button>(R.id.kCtrlV).setOnClickListener { WsClient.key("v", listOf("ctrl")) }
        findViewById<Button>(R.id.kCtrlZ).setOnClickListener { WsClient.key("z", listOf("ctrl")) }

        // ---------------- Ketik langsung ----------------
        val etType = findViewById<EditText>(R.id.etType)
        etType.addTextChangedListener(object : TextWatcher {
            private var before = ""
            override fun beforeTextChanged(s: CharSequence, st: Int, c: Int, a: Int) {
                before = s.toString()
            }
            override fun onTextChanged(s: CharSequence, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (suppressWatcher) return
                val now = s.toString()
                if (now.length > before.length && now.startsWith(before)) {
                    WsClient.text(now.substring(before.length))
                } else if (now.length < before.length && before.startsWith(now)) {
                    repeat(before.length - now.length) { WsClient.key("backspace") }
                } else if (now != before) {
                    // perubahan kompleks (autocorrect dll): hapus lalu ketik ulang
                    repeat(before.length) { WsClient.key("backspace") }
                    if (now.isNotEmpty()) WsClient.text(now)
                }
            }
        })
        findViewById<Button>(R.id.btnEnterSend).setOnClickListener {
            WsClient.key("enter")
            suppressWatcher = true
            etType.setText("")
            suppressWatcher = false
        }

        // ---------------- Clipboard ----------------
        findViewById<Button>(R.id.btnClipToPc).setOnClickListener {
            val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val s = cm.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString() ?: ""
            if (s.isEmpty()) toast("Clipboard HP kosong")
            else { WsClient.clipSet(s); toast("Dikirim ke clipboard PC") }
        }
        findViewById<Button>(R.id.btnClipToHp).setOnClickListener { WsClient.clipGet() }

        // ---------------- Media ----------------
        mapOf(
            R.id.mPrev to "prev", R.id.mPlay to "playpause", R.id.mNext to "next",
            R.id.mVolDown to "voldown", R.id.mMute to "mute", R.id.mVolUp to "volup"
        ).forEach { (id, a) ->
            findViewById<Button>(id).setOnClickListener { WsClient.media(a) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) WsClient.disconnect()
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
