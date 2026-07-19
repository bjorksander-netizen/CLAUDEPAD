# CLAUDEPAD — HP Android jadi Remote untuk Windows 10

HP kamu menjadi: **trackpad/touchpad, keyboard, clipboard sync, dan media control** untuk PC Windows 10.
Koneksi: **WiFi / Hotspot** dan **USB** (via ADB). *Bluetooth belum — bisa ditambah nanti.*

## Isi folder

```
CLAUDEPAD/
├── server/          ← jalan di PC Windows 10 (dengan UI desktop)
│   ├── pc_server.py
│   ├── start_server.bat
│   ├── usb_mode.bat
│   └── requirements.txt
└── android/         ← project Android Studio (Kotlin) untuk build APK
```

## 1. Setup PC (sekali saja)

1. Install Python 3 dari python.org (centang **Add Python to PATH**).
2. Dobel-klik `server/start_server.bat` — otomatis install `websockets` lalu jalan.
3. UI desktop menampilkan **PIN**, **alamat IP**, status koneksi, dan log.
4. Kalau Windows Firewall bertanya, pilih **Allow access** (Private network).

## 2. Dapatkan APK

**Cara termudah:** unduh dari [GitHub Releases](../../releases/tag/latest) — APK dibangun otomatis oleh GitHub Actions setiap ada perubahan di folder `android/`.

**Atau build sendiri:**

1. Install [Android Studio](https://developer.android.com/studio).
2. **Open** → pilih folder `android/`. Tunggu Gradle sync (butuh internet).
3. Menu **Build → Build App Bundle(s)/APK(s) → Build APK(s)**.
4. APK ada di `android/app/build/outputs/apk/debug/app-debug.apk` — kirim ke HP dan install
   (izinkan "install dari sumber tidak dikenal").

   Alternatif: hubungkan HP via USB (USB debugging aktif) lalu tekan **Run ▶** di Android Studio.

## 3. Cara pakai

### WiFi / Hotspot
- HP & PC harus **satu jaringan** (router yang sama, ATAU PC connect ke hotspot HP, ATAU HP connect ke hotspot PC).
- Buka app → tekan **Cari Otomatis** (atau ketik IP dari layar server) → masukkan **PIN** → **Hubungkan**.

### USB
1. Di HP: aktifkan **Developer Options → USB Debugging**.
2. Colok kabel USB, setujui prompt "Allow USB debugging" di HP.
3. Di PC: klik tombol **Aktifkan Mode USB** di UI server, atau jalankan `server/usb_mode.bat` (butuh [ADB platform-tools](https://developer.android.com/tools/releases/platform-tools) di PATH atau di folder `server/`).
4. Di app: tekan tombol **USB**, masukkan PIN.

### Kontrol
| Gestur / Tombol | Fungsi |
|---|---|
| 1 jari geser | gerakkan kursor |
| 1 jari tap | klik kiri |
| 2 jari tap | klik kanan |
| 2 jari geser | scroll |
| tap 2x lalu tahan | drag & drop |
| Kolom ketik | keyboard langsung ke PC (termasuk backspace) |
| Baris tombol | Esc, Tab, Win, Alt+Tab, Ctrl+C/V/Z, panah, dll |
| Clipboard HP↔PC | sinkronisasi teks clipboard dua arah |
| ⏮ ⏯ ⏭ 🔉 🔇 🔊 | media & volume PC |

## Keamanan
- Server minta **PIN** (acak tiap kali server dijalankan) sebelum menerima perintah.
- Hanya bind ke jaringan lokal; jangan buka port 8765 ke internet.

## Troubleshooting
- **"Cari Otomatis" tidak ketemu** → cek firewall (allow python), pastikan satu subnet. Isi IP manual.
- **Hotspot HP**: IP PC biasanya `192.168.43.x` / `192.168.x.x` — lihat di UI server.
- **USB gagal** → cek `adb devices` menampilkan device (bukan "unauthorized").
- **Ketikan dobel** → matikan autocorrect keyboard HP saat memakai kolom ketik.

## Roadmap Bluetooth (belum diimplementasi)
Butuh jalur RFCOMM: modul Bluetooth serial di app + server Python `pybluez`/socket Bluetooth di Windows. Protokol JSON-nya sudah siap dipakai ulang — tinggal ganti transport WebSocket dengan socket RFCOMM.
