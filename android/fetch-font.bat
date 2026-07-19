@echo off
REM Unduh JetBrains Mono untuk build lokal (opsional).
set DIR=%~dp0app\src\main\res\font
if not exist "%DIR%" mkdir "%DIR%"
curl -fsSL -o "%DIR%\jetbrains_mono.ttf" "https://raw.githubusercontent.com/JetBrains/JetBrainsMono/v2.304/fonts/ttf/JetBrainsMono-Regular.ttf"
echo JetBrains Mono tersimpan di %DIR%
pause
