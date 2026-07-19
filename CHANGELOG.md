# Changelog

## v2.3

### Perbaikan koneksi WiFi/Hotspot (akar masalah ditemukan)
Pesan error pengguna membongkar dua bug nyata:
`failed to connect to /172.25.224.1 ... from /10.240.31.97`

- **Server menampilkan IP adapter virtual.** `172.25.224.1` adalah adapter
  WSL/Hyper-V, bukan alamat hotspot — alamat itu mustahil dijangkau HP.
  Deteksi IP kini membaca nama adapter dari `ipconfig`, menyaring
  WSL/Hyper-V/Docker/VirtualBox/VPN, dan mengurutkan alamat hotspot
  (192.168.43.x) paling atas. Adapter virtual tetap ditampilkan tetapi
  diredupkan dan diberi label "jangan dipakai".
- **Socket HP keluar lewat jaringan seluler.** `10.240.31.97` adalah IP data
  seluler: saat HP jadi hotspot sambil 4G menyala, Android mengikat socket
  aplikasi ke jaringan default (seluler), sehingga paket menuju PC dikirim ke
  internet. Kini aplikasi mencari alamat lokal yang **satu subnet** dengan PC
  dan mengikat socket ke alamat itu, memaksa lalu lintas lewat interface
  hotspot. Pencarian otomatis juga memakai socket terikat per-interface.

### Diagnostik & firewall
- **Tombol Diagnosa koneksi** di setting: melaporkan interface HP, rute yang
  dipilih, tes TCP, tes balasan server, dan tes pencarian — lengkap dengan
  kesimpulan. Laporan bisa disalin.
- **Tes lewat browser**: buka `http://<ip-pc>:8765` di HP. Server kini
  membalas halaman status, jadi jaringan bisa diuji tanpa aplikasi.
- **Perbaiki Firewall** di jendela server: memasang aturan lewat UAC untuk
  semua profil termasuk Public (jaringan hotspot selalu dianggap Public).
  Status firewall ditampilkan langsung di jendela server.
- `start_server.bat` kini meminta hak Administrator otomatis saat aturan
  firewall belum ada.


## v2.2

### Perbaikan (laporan pengguna)
- **WiFi/Hotspot & cari otomatis** — `start_server.bat` kini menambah aturan
  Windows Firewall otomatis (TCP 8765 & UDP 8766); pencarian di APK memakai
  broadcast global + per-interface dengan 3 percobaan
- **Blur background** — cross-window blur sering dimatikan vendor, kini
  ditambah lapisan frost berbasis intensitas yang bekerja di semua perangkat
- **Warna aksen wallpaper** — sumber diganti ke `WallpaperColors`
  (Android 8.1+, tanpa izin), dengan penyesuaian saturasi/luminance

### Fitur baru
- Slider **intensitas blur** dan slider **kekuatan haptic** di setting
- **Backspace** dikembalikan: tombol ⌫ dan penghapusan di kolom ketik
- **Kunci versi**: server menolak koneksi bila versi APK ≠ versi server
- Tombol **putuskan koneksi**: ⏻ di bar atas APK dan tombol Putuskan
  di jendela server


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
