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
│   ├── CustomAchievement.java  # 도전과제 정의
│   ├── auth/                   # 리로스쿨 계정 인증 (커맨드/리스너/매니저)
│   ├── item/                   # 커스텀 아이템 정의 및 지급 로직
│   ├── player/                 # 플레이어 데이터, 동아리(직업), 경험치, 스킬, 로비 등
│   ├── stat/                   # 스탯(HP/MP/능력치) 시스템 및 GUI
│   ├── listener/               # 개별 기능 리스너
│   └── task/                   # 주기 실행 태스크
├── src/main/resources/
│   ├── plugin.yml              # 플러그인 메타데이터, 커맨드/권한 정의
│   └── config.yml              # 기본 설정값 (최초 실행 시 서버에 자동 생성)
├── configs/                    # 서버 plugins/ 폴더에 그대로 복사해 쓰는 사전 구성 파일
│   ├── BetonQuest/QuestPackages/default/   # 퀘스트/대화 정의
│   ├── MythicMobs/Mobs/                    # 몬스터 정의
│   └── README.md                           # 적용 방법 상세
└── docs/                        # 콘텐츠 문서 (NPC/아이템 목록, 세계관 등)
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

## 🚀 서버 배포 방법

1. 위 연동 플러그인들과 이 플러그인의 빌드 결과물(`build/libs/*.jar`)을 서버의 `plugins/` 폴더에 넣습니다.
2. [`configs/`](configs) 폴더 안의 내용을 [configs/README.md](configs/README.md) 안내에 따라 각 플러그인의 설정 경로에 그대로 복사합니다.
3. 서버를 최초 실행하면 `config.yml`이 자동 생성되며, 이 파일에서 시작 스탯 등 코드 수정 없이 조정 가능한 값들을 설정할 수 있습니다.
4. 퀘스트 대사, NPC 배치, 몬스터 스탯 등 `.yml` 설정만 바꾸는 변경은 리빌드가 필요 없고, 자바 소스가 바뀐 경우에만 리빌드가 필요합니다.

> ⚠️ `/reload`는 새로 빌드한 jar를 다시 읽지 않으므로, 코드가 바뀐 뒤에는 서버를 완전히 재시작해야 합니다.

## 📚 문서

- [docs/NPC_LIST.md](docs/NPC_LIST.md) — NPC ID 매핑 가이드
- [docs/ITEM_LIST.md](docs/ITEM_LIST.md) — 커스텀 아이템 목록 및 지급 방법
- [docs/TUTORIAL_GUIDE.md](docs/TUTORIAL_GUIDE.md) — 신입생 튜토리얼 가이드
- [docs/WORLDVIEW.md](docs/WORLDVIEW.md) — 세계관/설정
- [configs/README.md](configs/README.md) — 사전 구성 파일 적용 방법

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
