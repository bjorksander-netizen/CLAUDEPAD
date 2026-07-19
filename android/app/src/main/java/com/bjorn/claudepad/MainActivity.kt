package com.bjorn.claudepad

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MainActivity : AppCompatActivity() {

    private lateinit var etIp: EditText
    private lateinit var etPin: EditText
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etIp = findViewById(R.id.etIp)
        etPin = findViewById(R.id.etPin)
        tvStatus = findViewById(R.id.tvStatus)

        val prefs = getSharedPreferences("claudepad", MODE_PRIVATE)
        etIp.setText(prefs.getString("ip", ""))
        etPin.setText(prefs.getString("pin", ""))

        findViewById<Button>(R.id.btnConnect).setOnClickListener {
            connect(etIp.text.toString().trim())
        }
        findViewById<Button>(R.id.btnUsb).setOnClickListener {
            // Mode USB: PC menjalankan "adb reverse tcp:8765 tcp:8765",
            // sehingga server terlihat di 127.0.0.1 dari sisi HP.
            connect("127.0.0.1")
        }
        findViewById<Button>(R.id.btnScan).setOnClickListener { scan() }
    }

    private fun connect(host: String) {
        if (host.isEmpty()) {
            toast("Isi alamat IP PC dulu, atau tekan Cari Otomatis")
            return
        }
        val pin = etPin.text.toString().trim()
        getSharedPreferences("claudepad", MODE_PRIVATE).edit()
            .putString("ip", etIp.text.toString().trim())
            .putString("pin", pin).apply()

        tvStatus.text = "Menghubungkan ke " + host + "..."
        WsClient.onState = { ok, msg ->
            runOnUiThread {
                tvStatus.text = msg
                if (ok) startActivity(Intent(this, ControlActivity::class.java))
            }
        }
        WsClient.connect(host, 8765, pin)
    }

    private fun scan() {
        tvStatus.text = "Mencari PC di jaringan..."
        Thread {
            try {
                val sock = DatagramSocket()
                sock.broadcast = true
                sock.soTimeout = 2500
                val msg = "DISCOVER_CLAUDEPAD".toByteArray()
                sock.send(DatagramPacket(msg, msg.size,
                    InetAddress.getByName("255.255.255.255"), 8766))
                val buf = ByteArray(256)
                val resp = DatagramPacket(buf, buf.size)
                sock.receive(resp)
                sock.close()
                val parts = String(resp.data, 0, resp.length).split("|")
                if (parts.isNotEmpty() && parts[0] == "CLAUDEPAD") {
                    val ip = resp.address.hostAddress ?: ""
                    runOnUiThread {
                        etIp.setText(ip)
                        tvStatus.text = "Ketemu: " + (parts.getOrNull(1) ?: "") + " (" + ip + ")"
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { tvStatus.text = "Tidak ketemu. Pastikan server jalan & satu jaringan." }
            }
        }.start()
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
