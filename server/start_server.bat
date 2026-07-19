@echo off
title CLAUDEPAD
cd /d "%~dp0"

where python >nul 2>nul || (echo Python belum terpasang. Install dari python.org lalu centang "Add to PATH". & pause & exit /b 1)

REM ---- Pastikan aturan firewall ada (butuh Administrator sekali saja) ----
netsh advfirewall firewall show rule name="CLAUDEPAD TCP" >nul 2>nul
if errorlevel 1 (
  echo [i] Memasang aturan firewall CLAUDEPAD - setujui prompt Administrator...
  powershell -NoProfile -Command "Start-Process cmd -Verb RunAs -WindowStyle Hidden -ArgumentList '/c netsh advfirewall firewall add rule name=\"CLAUDEPAD TCP\" dir=in action=allow protocol=TCP localport=8765 profile=any ^& netsh advfirewall firewall add rule name=\"CLAUDEPAD UDP\" dir=in action=allow protocol=UDP localport=8766 profile=any'" >nul 2>nul
  timeout /t 3 /nobreak >nul
)

python -c "import websockets, pycaw, pystray, PIL" 2>nul || (
  echo Menyiapkan dependency, mohon tunggu...
  python -m pip install --quiet --disable-pip-version-check -r requirements.txt
)
start "" pythonw pc_server.py
exit
