# ============================================================
#  월드(맵) 백업 스크립트
#  직접 실행하지 말고 "월드백업.bat" 을 더블클릭하세요.
#
#  서버 폴더의 world 폴더를 날짜·시간이 붙은 zip 파일로 저장합니다.
#    저장 위치: <서버 폴더>\backup_world\world_2026-10-17_21-30.zip
#  - 서버가 켜져 있으면 저장 중인 파일이 깨질 수 있어서 중단합니다. 서버를 끈 뒤 실행하세요.
#  - 오래된 백업은 자동으로 지우지 않습니다. 용량이 커지면 backup_world 폴더에서 직접 정리하세요.
#  - 복원하려면 서버를 끄고, world 폴더 이름을 바꿔 둔 뒤 zip 의 world 폴더를 서버 폴더에 풉니다.
# ============================================================
param(
    [switch]$Force   # 서버가 켜져 있어도 강제로 백업 (권장하지 않음)
)

$ErrorActionPreference = 'Stop'
$pathFile = Join-Path $PSScriptRoot 'server-path.txt'

# ---------- 서버 폴더 경로 ----------
$serverRoot = $null
if (Test-Path $pathFile) {
    $serverRoot = (Get-Content $pathFile -Encoding UTF8 -TotalCount 1).Trim().Trim('"')
}
while (-not $serverRoot -or -not (Test-Path (Join-Path $serverRoot 'world'))) {
    if ($serverRoot) { Write-Host "[오류] '$serverRoot' 안에 world 폴더가 없습니다." -ForegroundColor Red }
    $serverRoot = (Read-Host "서버 폴더 경로를 입력하세요 (예: C:\Users\이름\Desktop\Minecraft\server)").Trim().Trim('"')
    if (-not $serverRoot) { Write-Host "취소했습니다."; exit 1 }
}
[System.IO.File]::WriteAllText($pathFile, $serverRoot, (New-Object System.Text.UTF8Encoding($false)))

# ---------- 서버가 켜져 있는지 확인 ----------
$running = @(Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
    Where-Object { $_.CommandLine -match 'paper-.*\.jar' })
if ($running.Count -gt 0 -and -not $Force) {
    Write-Host "[중단] 서버가 켜져 있습니다. 서버를 끈 뒤 다시 실행하세요. (콘솔에서 stop)" -ForegroundColor Yellow
    exit 1
}

# ---------- 백업 ----------
$stamp  = Get-Date -Format 'yyyy-MM-dd_HH-mm'
$outDir = Join-Path $serverRoot 'backup_world'
$zip    = Join-Path $outDir "world_$stamp.zip"
New-Item -ItemType Directory -Path $outDir -Force | Out-Null

Write-Host "== 월드 백업 =="
Write-Host "대상: $(Join-Path $serverRoot 'world')"
Compress-Archive -Path (Join-Path $serverRoot 'world') -DestinationPath $zip -CompressionLevel Optimal

$mb = [math]::Round((Get-Item $zip).Length / 1MB, 1)
Write-Host ""
Write-Host "완료: $zip ($mb MB)" -ForegroundColor Green
Write-Host "백업 개수: $((Get-ChildItem $outDir -Filter 'world_*.zip').Count)개"
