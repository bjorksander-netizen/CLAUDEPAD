@echo off
title CLAUDEPAD
cd /d "%~dp0"
where python >nul 2>nul || (echo Python belum terpasang. Install dari python.org lalu centang "Add to PATH". & pause & exit /b 1)

REM --- Buka firewall untuk koneksi WiFi/Hotspot (butuh sekali saja) ---
netsh advfirewall firewall show rule name="CLAUDEPAD TCP" >nul 2>nul
if errorlevel 1 (
  netsh advfirewall firewall add rule name="CLAUDEPAD TCP" dir=in action=allow protocol=TCP localport=8765 >nul 2>nul
  netsh advfirewall firewall add rule name="CLAUDEPAD UDP" dir=in action=allow protocol=UDP localport=8766 >nul 2>nul
  if errorlevel 1 (
    echo [!] Gagal menambah aturan firewall otomatis - koneksi WiFi mungkin diblokir.
    echo     Jalankan file ini sekali sebagai Administrator, ATAU izinkan Python
    echo     di Windows Firewall untuk jaringan Private saat diminta.
  ) else (
    echo [i] Aturan firewall CLAUDEPAD ditambahkan.
  )
)

python -c "import websockets, pycaw, pystray, PIL" 2>nul || (
  echo Menyiapkan dependency, mohon tunggu...
  python -m pip install --quiet --disable-pip-version-check -r requirements.txt
)
start "" pythonw pc_server.py
exit
