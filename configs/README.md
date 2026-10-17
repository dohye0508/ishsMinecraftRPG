# 📂 configs/ — 기획팀이 수정하는 파일 모음

코드(Java)를 건드리지 않고 **텍스트 파일만 고쳐서** 게임 내용을 바꾸는 폴더입니다.
서버의 `plugins/` 구조와 똑같이 맞춰져 있어서, 이 폴더가 **Git에 올라가는 기준 원본**입니다.

```text
configs/
├── ishsMinecraftRpg/            ← 이 플러그인 설정 (/설정리로드 로 적용)
│   ├── config.yml                 맵 경계, 스폰, 리로스쿨 인증, /퀘스트 표시 이름
│   ├── gameplay.yml               스탯 공식, 레벨, 보상, 게임 규칙, 월드 설정
│   ├── clubs.yml                  동아리 (이름, 아이콘, 가입 보너스)
│   ├── skills.yml                 스킬 (쿨타임, 마나, 피해량)
│   ├── items.yml                  커스텀 아이템 (이름, 설명, 기능)
│   ├── achievements.yml           도전과제 이름·설명·알림 문구
│   ├── messages.yml               알림·안내·GUI·사이드바 문구
│   └── help.yml                   /도움말 문구
├── BetonQuest/QuestPackages/default/
│   ├── main.yml                   퀘스트 목표·보상, NPC 연결
│   ├── package.yml                (BetonQuest가 관리, 수정 금지)
│   └── conversations/*.yml        NPC 대사·선택지
├── MythicMobs/Mobs/
│   └── beginners.yml              몬스터 정의
├── 배포.bat                       configs → 서버로 적용
├── 미리보기_배포.bat              배포하면 무엇이 바뀌는지만 확인 (복사 안 함)
└── 서버에서_가져오기.bat          서버 → configs 로 가져오기
```

## 작업 순서

1. **configs 안의 파일을 수정**합니다. (메모장, VS Code 등)
2. **`배포.bat`을 더블클릭**합니다. 바뀐 파일만 서버로 복사되고, 서버의 기존 파일은 `서버폴더\backup_config\sync_날짜시간\`에 백업됩니다.
   - 처음 실행하면 서버 폴더 경로를 한 번 물어봅니다. (`configs/server-path.txt`에 저장되고, 이 파일은 Git에 올라가지 않습니다)
   - 헷갈리면 먼저 `미리보기_배포.bat`으로 확인하세요.
3. 서버가 켜져 있다면 게임에서 OP가 입력합니다.
   - `/bq reload` — 퀘스트, 대화 적용
   - `/설정리로드` — config, 아이템, 도전과제, 도움말 적용
4. 확인이 끝나면 Git에 올립니다. (`git add configs` → `git commit` → `git push`)

## 서버에서 직접 고쳤다면

서버 폴더의 파일을 직접 고친 경우에는 **`서버에서_가져오기.bat`** 으로 configs에 반영한 뒤 Git에 올리세요.
그렇지 않고 `배포.bat`을 먼저 누르면 **서버에서 고친 내용이 configs의 옛 내용으로 덮어써집니다.** (덮어쓰기 전 백업은 남습니다)

## 동기화 대상과 제외 대상

- 동기화: 위 트리에 있는 `.yml` 파일. (`MythicMobs/Mobs`의 `Example*.yml`, `VanillaMobs.yml`은 MythicMobs 기본 파일이라 제외)
- **동기화하지 않는 것 (Git에 올리지 마세요):**
  - `plugins/ishsMinecraftRpg/userdata/` — 플레이어 개인 데이터
  - `plugins/BetonQuest/database.db` — 플레이어 퀘스트 진행 데이터
  - `plugins/Citizens/saves.yml` — NPC 위치 데이터 (소유자 UUID 포함). NPC는 게임 안에서 `/npc` 명령으로 만듭니다.
  - 인증 관련 파일, 서버 로그

## 처음 서버를 세팅할 때

`ishsMinecraftRpg/` 안의 `.yml`은 플러그인 jar 안에 기본값이 들어 있어서, 서버를 처음 켜면 `plugins/ishsMinecraftRpg/`에 자동으로 만들어집니다.
(빌드할 때 이 폴더의 `ishsMinecraftRpg/*.yml`이 jar의 기본 파일로 들어갑니다.)
파일에서 항목이 빠져 있으면 jar 안의 기본값으로 채워집니다.
BetonQuest와 MythicMobs 파일은 `배포.bat`으로 서버에 넣습니다.

자세한 편집 방법(NPC, 퀘스트, 아이템 추가)은 [docs/기획팀_가이드.md](../docs/기획팀_가이드.md)를 보세요.
