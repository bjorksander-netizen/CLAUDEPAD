@echo off
title CLAUDEPAD
cd /d "%~dp0"
where python >nul 2>nul || (echo Python belum terpasang. Install dari python.org lalu centang "Add to PATH". & pause & exit /b 1)
python -c "import websockets, pycaw, pystray, PIL" 2>nul || (
  echo Menyiapkan dependency, mohon tunggu...
  python -m pip install --quiet --disable-pip-version-check -r requirements.txt
)
REM pythonw = tanpa jendela konsol
start "" pythonw pc_server.py
exit
