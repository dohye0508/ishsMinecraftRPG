# ============================================================
#  configs 폴더 <-> 서버 plugins 폴더 동기화 스크립트
#  직접 실행하지 말고 "배포.bat" 또는 "서버에서_가져오기.bat" 을 더블클릭하세요.
#
#   push : configs(저장소) -> 서버   (내가 고친 내용을 서버에 적용)
#   pull : 서버 -> configs(저장소)   (서버에서 직접 고친 내용을 저장소로 가져오기)
#
#  -DryRun 을 붙이면 실제로 복사하지 않고 무엇이 바뀔지만 보여줍니다.
# ============================================================
param(
    [Parameter(Mandatory = $true)][ValidateSet('push', 'pull')][string]$Direction,
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'
$configs  = $PSScriptRoot
$repoRoot = Split-Path $configs -Parent
$pathFile = Join-Path $configs 'server-path.txt'

# ---------- 서버 폴더 경로 ----------
$serverRoot = $null
if (Test-Path $pathFile) {
    $serverRoot = (Get-Content $pathFile -Encoding UTF8 -TotalCount 1).Trim().Trim('"')
}
while (-not $serverRoot -or -not (Test-Path (Join-Path $serverRoot 'plugins'))) {
    if ($serverRoot) { Write-Host "[오류] '$serverRoot' 안에 plugins 폴더가 없습니다." -ForegroundColor Red }
    $serverRoot = (Read-Host "서버 폴더 경로를 입력하세요 (예: C:\Users\이름\Desktop\Minecraft\server)").Trim().Trim('"')
    if (-not $serverRoot) { Write-Host "취소했습니다."; exit 1 }
}
[System.IO.File]::WriteAllText($pathFile, $serverRoot, (New-Object System.Text.UTF8Encoding($false)))
$plugins = Join-Path $serverRoot 'plugins'

# ---------- 동기화 대상 ----------
# Repo   : configs 안의 폴더,  Server : plugins 안의 폴더
$maps = @(
    @{ Repo = 'ishsMinecraftRpg';                 Server = 'ishsMinecraftRpg';                 Filter = @('*.yml');                                                    Recurse = $false; Exclude = @() },
    @{ Repo = 'BetonQuest';                       Server = 'BetonQuest';                       Filter = @('config.yml');                                              Recurse = $false; Exclude = @() },
    @{ Repo = 'BetonQuest\lang';                  Server = 'BetonQuest\lang';                  Filter = @('ko-KR.yml');                                               Recurse = $false; Exclude = @() },
    @{ Repo = 'BetonQuest\QuestPackages\default'; Server = 'BetonQuest\QuestPackages\default'; Filter = @('*.yml');                                                   Recurse = $true;  Exclude = @('npcs_generated.yml') },
    @{ Repo = 'MythicMobs\Mobs';                  Server = 'MythicMobs\Mobs';                  Filter = @('*.yml');                                                   Recurse = $false; Exclude = @('Example*.yml', 'VanillaMobs.yml') }
)

function Get-RelativeFiles($root, $map) {
    if (-not (Test-Path $root)) { return @() }
    $found = @()
    foreach ($f in $map.Filter) {
        $found += Get-ChildItem $root -Filter $f -File -Recurse:$map.Recurse -ErrorAction SilentlyContinue
    }
    $rels = $found | Sort-Object FullName -Unique | ForEach-Object { $_.FullName.Substring($root.Length).TrimStart('\') }
    foreach ($ex in $map.Exclude) { $rels = $rels | Where-Object { (Split-Path $_ -Leaf) -notlike $ex } }
    return @($rels)
}

$stamp     = Get-Date -Format 'yyyyMMdd_HHmmss'
$backupDir = Join-Path $serverRoot "backup_config\sync_$stamp"
$changed   = 0
$unchanged = 0

# 줄바꿈(CRLF/LF) 차이는 무시하고 내용만 비교하기 위한 해시
function Get-TextHash($path) {
    $bytes = [System.IO.File]::ReadAllBytes($path)
    $filtered = New-Object System.Collections.Generic.List[byte]
    foreach ($b in $bytes) { if ($b -ne 13) { $filtered.Add($b) } }
    $sha = [System.Security.Cryptography.SHA256]::Create()
    return [System.BitConverter]::ToString($sha.ComputeHash($filtered.ToArray()))
}
function Sync-One($src, $dst, $label) {
    $isNew = -not (Test-Path $dst)
    if (-not $isNew -and ((Get-TextHash $src) -eq (Get-TextHash $dst))) {
        $script:unchanged++
        return
    }
    $script:changed++
    $tag = if ($isNew) { '[새 파일]' } else { '[변경]  ' }
    Write-Host "  $tag $label"
    if ($DryRun) { return }
    if (-not $isNew -and $Direction -eq 'push') {
        # 서버 파일을 덮어쓰기 전에 백업
        $bk = Join-Path $backupDir $label
        New-Item -ItemType Directory -Path (Split-Path $bk -Parent) -Force | Out-Null
        Copy-Item $dst $bk -Force
    }
    New-Item -ItemType Directory -Path (Split-Path $dst -Parent) -Force | Out-Null
    Copy-Item $src $dst -Force
}

$title = if ($Direction -eq 'push') { '배포: configs(저장소) -> 서버' } else { '가져오기: 서버 -> configs(저장소)' }
Write-Host ""
Write-Host "== $title ==" -ForegroundColor Cyan
Write-Host "서버: $serverRoot"
if ($DryRun) { Write-Host "(DryRun: 실제로 복사하지 않습니다)" -ForegroundColor Yellow }
Write-Host ""

foreach ($m in $maps) {
    $repoDir   = Join-Path $configs $m.Repo
    $serverDir = Join-Path $plugins $m.Server
    if ($Direction -eq 'push') {
        foreach ($rel in (Get-RelativeFiles $repoDir $m)) {
            Sync-One (Join-Path $repoDir $rel) (Join-Path $serverDir $rel) (Join-Path $m.Server $rel)
        }
    } else {
        foreach ($rel in (Get-RelativeFiles $serverDir $m)) {
            Sync-One (Join-Path $serverDir $rel) (Join-Path $repoDir $rel) (Join-Path $m.Repo $rel)
        }
    }
}

# 기획팀 가이드는 서버 쪽에서 볼 수 있도록 배포할 때만 복사
if ($Direction -eq 'push') {
    $guide = Join-Path $repoRoot 'docs\기획팀_가이드.md'
    if (Test-Path $guide) {
        Sync-One $guide (Join-Path $plugins 'ishsMinecraftRpg\기획팀_가이드.md') 'ishsMinecraftRpg\기획팀_가이드.md'
    }
}

Write-Host ""
if ($changed -eq 0) {
    Write-Host "바뀐 파일이 없습니다. (같은 파일 $unchanged 개)" -ForegroundColor Green
} elseif ($DryRun) {
    Write-Host "바뀔 파일 $changed 개 (같은 파일 $unchanged 개)" -ForegroundColor Yellow
} else {
    Write-Host "완료: $changed 개 파일 복사 (같은 파일 $unchanged 개)" -ForegroundColor Green
    if ($Direction -eq 'push') {
        Write-Host "  서버 파일은 덮어쓰기 전에 백업했습니다: $backupDir"
        Write-Host ""
        Write-Host "[다음 단계] 서버가 켜져 있다면 게임에서 OP가 아래를 입력하세요." -ForegroundColor Cyan
        Write-Host "  /bq reload      (퀘스트, 대화 적용)"
        Write-Host "  /설정리로드      (config, 아이템, 도전과제, 도움말 적용)"
    } else {
        Write-Host "  이제 git 에 올리면 됩니다. (git add configs, commit, push)"
    }
}
