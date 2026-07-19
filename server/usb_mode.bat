@echo off
title CLAUDEPAD - Mode USB
REM Hubungkan HP via kabel USB, aktifkan USB Debugging (Developer Options),
REM lalu jalankan file ini. Butuh ADB (platform-tools) di PATH atau di folder ini.
where adb >nul 2>nul || (echo ADB tidak ditemukan. Download "SDK Platform Tools" dari developer.android.com lalu taruh adb.exe di folder ini atau tambahkan ke PATH. & pause & exit /b 1)
adb start-server
adb reverse tcp:8765 tcp:8765
if %errorlevel%==0 (
  echo.
  echo Mode USB aktif! Di aplikasi HP, tekan tombol "USB" atau isi alamat: 127.0.0.1
) else (
  echo Gagal. Pastikan HP terhubung, USB Debugging aktif, dan izin ADB disetujui di layar HP.
)
pause
