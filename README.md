# ISHS RPG

PaperMC 기반 마인크래프트 서버용 커스텀 RPG 플러그인입니다.

- **API**: PaperMC 1.21
- **Java**: 21+ (툴체인 기준, 실행 서버는 [PaperMC 요구사항](https://docs.papermc.io/misc/java-install)에 맞는 버전 필요)
- **빌드 도구**: Gradle (Wrapper 포함)
- **License**: [MIT](LICENSE)

---

## 📁 프로젝트 구조

```text
ishsMinecraftRpg/
├── build.gradle                # Gradle 빌드 설정 (의존성, Java 버전 등)
├── settings.gradle
├── src/main/java/kr/ishs/raibit/ishsMinecraftRpg/
│   ├── IshsMinecraftRpg.java   # 플러그인 진입점 (onEnable/onDisable)
│   ├── RpgListener.java        # 공용 이벤트 리스너
│   ├── PlaceholderHook.java    # PlaceholderAPI 연동
│   ├── CustomAchievement.java  # 도전과제 달성 처리
│   ├── AchievementConfig.java  # 도전과제 정의·문구 로더 (achievements.yml)
│   ├── MapConfig.java          # 맵 경계/스폰/인증 설정 로더 (config.yml)
│   ├── ReloadCommand.java      # /설정리로드 (설정 변경을 재시작 없이 적용)
│   ├── config/                 # 설정 로더: 게임 수치(gameplay.yml), 동아리(clubs.yml), 문구(messages.yml)
│   ├── gui/                    # GUI 종류 구분 표식 (제목 문구에 의존하지 않음)
│   ├── auth/                   # 리로스쿨 계정 인증 (커맨드/리스너/매니저)
│   ├── item/                   # 커스텀 아이템 로드·지급, 효과 해석/실행 (정의는 items.yml)
│   ├── player/                 # 플레이어 데이터, 동아리(직업), 경험치, 스킬(skills.yml), 로비 등
│   ├── stat/                   # 스탯(HP/MP/능력치) 시스템 및 GUI
│   ├── listener/               # 개별 기능 리스너 (직사각형 맵 경계 포함)
│   └── task/                   # 주기 실행 태스크
├── src/main/resources/
│   └── plugin.yml              # 플러그인 메타데이터, 커맨드/권한 정의
├── src/test/                   # 설정 파일 검증 테스트 (연결/형식, 이전 값과의 동일성)
├── configs/                    # 기획팀이 수정하는 설정 파일 (서버 plugins/ 구조와 동일, Git 기준 원본)
│   ├── ishsMinecraftRpg/                   # 이 플러그인의 모든 설정 yml (빌드 시 jar 기본값으로 포함)
│   ├── BetonQuest/QuestPackages/default/   # 퀘스트/대화 정의
│   ├── MythicMobs/Mobs/                    # 몬스터 정의
│   ├── 배포.bat, 서버에서_가져오기.bat      # configs ↔ 서버 plugins 동기화 (sync.ps1)
│   └── README.md                           # 사용 방법 상세
└── docs/                        # 콘텐츠 문서 (기획팀 가이드, NPC/아이템 목록, 세계관 등)
```

## 🧩 연동 플러그인 (별도 설치 필요)

빌드 산출물 단독으로는 동작하지 않으며, 서버에 아래 플러그인이 함께 설치되어 있어야 합니다.

| 역할 | 플러그인 |
|---|---|
| NPC | Citizens |
| 퀘스트 / 대화 | BetonQuest |
| 몬스터 | MythicMobs |
| 플레이스홀더 | PlaceholderAPI |

## 🛠️ 빌드 방법

```bash
git clone https://github.com/dohye0508/ishsMinecraftRpg.git
cd ishsMinecraftRpg
./gradlew build
```

빌드 결과물은 `build/libs/ishsMinecraftRpg-<version>.jar` 에 생성됩니다.

## ✅ 테스트

```bash
./gradlew test          # 설정 파일의 연결/형식 검증 (서버 불필요, 수치를 바꿔도 통과)
./gradlew parityTest    # 설정 기본값이 이전 코드의 값/계산식과 같은지 검증 (개발용)
```
## 🚀 서버 배포 방법

1. 위 연동 플러그인들과 이 플러그인의 빌드 결과물(`build/libs/*.jar`)을 서버의 `plugins/` 폴더에 넣습니다.
2. 서버를 최초 실행하면 `plugins/ishsMinecraftRpg/`에 설정 파일들(`config.yml`, `gameplay.yml`, `clubs.yml`, `skills.yml`, `items.yml`, `achievements.yml`, `messages.yml`, `help.yml`)이 자동 생성됩니다.
3. [`configs/`](configs) 폴더의 **`배포.bat`**을 실행하면 BetonQuest, MythicMobs 설정과 위 파일들이 서버 `plugins/`로 복사됩니다. (서버 폴더 경로는 처음 한 번 입력, 상세는 [configs/README.md](configs/README.md))
4. 스탯·레벨·보상 수치, 동아리, 스킬, 아이템, 도전과제, 문구, 맵·인증 설정, 퀘스트 대사 등 `.yml` 설정만 바꾸는 변경은 리빌드가 필요 없습니다. `configs/`를 고치고 `배포.bat` 실행 후 게임에서 `/bq reload`, `/설정리로드`를 입력하면 됩니다. 자바 소스가 바뀐 경우에만 리빌드가 필요합니다.

> ⚠️ `/reload`는 새로 빌드한 jar를 다시 읽지 않으므로, 코드가 바뀐 뒤에는 서버를 완전히 재시작해야 합니다.

## 📚 문서

- [docs/기획팀_가이드.md](docs/기획팀_가이드.md) — 기획팀용: 설정 파일별 수정 방법, 아이템·NPC·퀘스트 추가
- [docs/NPC_LIST.md](docs/NPC_LIST.md) — 현재 NPC 목록 (관리는 `npcs.yml`)
- [docs/ITEM_LIST.md](docs/ITEM_LIST.md) — 커스텀 아이템 목록 및 지급 방법
- [docs/TUTORIAL_GUIDE.md](docs/TUTORIAL_GUIDE.md) — 신입생 튜토리얼 가이드
- [docs/WORLDVIEW.md](docs/WORLDVIEW.md) — 세계관/설정
- [configs/README.md](configs/README.md) — 기획 파일 폴더 구조와 `배포.bat` 사용 방법

## 🌿 브랜치 전략

`main`은 항상 빌드 가능한 상태를 유지합니다. 기능 개발은 아래처럼 영역별 브랜치에서 진행한 뒤 PR로 `main`에 병합합니다.

| 브랜치 | 담당 영역 |
|---|---|
| `feature/quest` | BetonQuest 퀘스트/대화 콘텐츠 (`configs/BetonQuest`) |
| `feature/npc` | Citizens NPC 배치 및 ID 매핑 (`docs/NPC_LIST.md` 연동) |
| `feature/skillset` | 동아리별 스킬 로직 (`player/SkillManager.java` 등) |

새 기능은 위 브랜치 중 해당하는 곳에서 작업하거나, 없는 영역이면 `feature/<영역명>`으로 새로 브랜치를 파서 작업해 주세요.

## 🤝 기여 방법

1. 이 저장소를 fork하거나 브랜치를 생성합니다.
2. 위 프로젝트 구조를 참고해 해당 영역의 코드/설정을 수정합니다.
3. `./gradlew build`로 빌드가 깨지지 않는지 확인합니다.
4. `main`을 대상으로 Pull Request를 생성합니다.
