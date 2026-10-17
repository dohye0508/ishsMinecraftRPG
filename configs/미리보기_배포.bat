@echo off
chcp 65001 >nul
echo [미리보기] 배포하면 어떤 파일이 바뀌는지만 보여줍니다. 실제로 복사하지 않습니다.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0sync.ps1" -Direction push -DryRun
echo.
pause
