@echo off
title CLAUDEPAD Server
cd /d "%~dp0"
where python >nul 2>nul || (echo Python belum terpasang. Install dari python.org lalu centang "Add to PATH". & pause & exit /b 1)
python -c "import websockets" 2>nul || pip install websockets
python pc_server.py
pause
