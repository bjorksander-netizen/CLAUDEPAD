# Changelog

## v2.1

### Perbaikan bug (laporan pengguna)
- **Trackpad hilang** — panel baris bawah memakai weight di parent wrap_content
  sehingga melahap seluruh tinggi layar; tinggi baris bawah kini tetap (190dp)
- **D-Pad terlalu besar** — panel D-Pad kini berukuran sama dengan panel media
- **Slider volume mati** — server memanggil COM audio dari thread tanpa
  CoInitialize sehingga pycaw selalu gagal; ditambah mode cadangan bertingkat
  bila server memang tanpa pycaw

### Revisi & fitur
- Menu Advance kini **pop-up persegi** (bukan dropdown baris)
- **Wallpaper diblur** di belakang aplikasi (Android 12+)
- Fitur orientasi diluruskan: hanya **arah input trackpad** yang diputar 90° —
  layout tidak berubah; swipe kanan = kursor ke atas saat aktif
- **Warna aksen mengikuti wallpaper** perangkat (Material You, Android 12+)


## v2.0

### Tampilan
- Tema baru bergaya Control Center: panel kaca tembus pandang
- Background aplikasi memperlihatkan **wallpaper HP** (bukan warna solid)
- Font monospace **JetBrains Mono** di seluruh UI
- Semua tombol memakai **simbol fungsi** (Ctrl+Z → ↩, Tab → ⇥, dst)
- GUI desktop dirombak: layout lebih rapi, tema gelap senada

### Layout
- Keyboard dipindah ke **bawah tombol kontrol mouse**
- Esc, Tab, Win, Del dikelompokkan ke dropdown **Advance** (⋯)
- Baris bawah dibagi 2 kolom seimbang: volume + media (kiri), D-Pad (kanan)
- Tombol arah berbentuk **D-Pad ala DualShock** dengan auto-repeat

### Fitur baru
- **Slider volume absolut** menggantikan tombol mute (via pycaw)
- **Gesture Windows Precision Touchpad**: 2 jari scroll & pinch zoom,
  3 jari untuk Task View / Show Desktop / ganti aplikasi
- **Getaran haptic bertingkat** — kekuatan menyesuaikan bobot aksi
- **Tombol ubah orientasi** vertikal ↔ horizontal tanpa mengubah layout
- **Halaman Setting**: status koneksi, jalur koneksi, nama PC, versi server,
  toggle haptic / scroll natural / layar menyala, sensitivitas kursor,
  change log, panduan gesture, bantuan
- Nama PC yang terhubung tampil di tengah trackpad
- Server: **minimize to system tray**, jalan **tanpa jendela konsol**

### Perubahan
- Alt+Tab → **Win+Tab**
- Fitur **clipboard sync dihapus**
- Fitur **backspace dihapus**

## v1.0
- Rilis pertama: trackpad, keyboard, clipboard sync, media control,
  koneksi WiFi/Hotspot & USB
