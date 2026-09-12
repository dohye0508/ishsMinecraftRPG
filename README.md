# ⚔️ ISHS RPG

인천과학고 학생만 참여 가능한 마인크래프트 RPG 서버 플러그인입니다.
**리로스쿨 계정 인증**으로 실제 학번·이름을 검증하고, 7개 동아리 진영 시스템·스탯/전투·커스텀 아이템·퀘스트를 하나의 서버에서 제공합니다.

- **API**: PaperMC 1.21
- **Java**: 21+
- **다운로드**: [Releases](https://github.com/dohye0508/ishs_rpg/releases/latest)에서 빌드된 jar를 바로 받을 수 있습니다.
- **라이선스**: [MIT](LICENSE)

---

## 🌟 주요 기능

### 리로스쿨 인증
- `/인증` 후 채팅으로 아이디·비밀번호를 입력하면 리로스쿨 서버에 직접 로그인해 실명·학번·기수를 확인합니다.
- 인증 전에는 발광 효과 등 일부 연출이 보류되고, 인증 완료 시 정상 적용됩니다.
- 관리자는 `/입장허용 [열기|닫기]`로 서버는 켜둔 채 신규 로그인만 막을 수 있습니다 (기존 접속자는 유지).

### 동아리(직업) 시스템
| 동아리 | 테마 | 스킬(Shift+F) |
|---|---|---|
| 📘 LOGIC | 수학 · 마법 | 중력 왜곡 |
| 🔨 WIN | 물리 · 파괴 | 지진 강타 |
| 🧨 TNT | 화학 · 폭발 | 연쇄 폭발 |
| 🌾 LOTTOL | 생명 · 회복 | 생명의 파동 |
| 🔮 QUASAR | 천문 · 원거리 | 블랙홀 |
| 💻 RAIBIT | 정보 · 유틸리티 | 오버클럭/글리치 텔레포트 |
| ⚙️ PLUTONIUM | 공학 · 방어 | 나노 배리어 |

### 스탯 & 전투
- 힘/민첩/지능/체력/정신력 5대 스탯을 `/stat`으로 자유 분배하는 커스텀 HP/MP 시스템.
- 새 캐릭터의 시작 스탯·포인트는 `config.yml`의 `starting-stats`로 코드 수정 없이 조정 가능합니다.
- 서바이벌 모드에서는 전투(때리기)만 가능하고 블록 채굴/파괴는 막혀 있습니다.

### 커스텀 아이템
바닐라 재질에 마법부여 반짝임 효과를 입힌 자체 아이템 시스템. `/아이템지급 [플레이어] [아이템ID]`로 지급하거나 BetonQuest 퀘스트 보상으로 연결할 수 있습니다. 전체 목록은 [docs/ITEM_LIST.md](docs/ITEM_LIST.md) 참고.

### NPC & 퀘스트
NPC는 **Citizens**, 대화·퀘스트는 **BetonQuest**로 관리합니다. 시민(Citizens) ID만 맞추면 자동으로 대화가 연결되도록 되어 있어 새 NPC 배치도 코드 수정 없이 가능합니다. ID 매핑표는 [docs/NPC_LIST.md](docs/NPC_LIST.md) 참고.

### 도전과제
`/도전과제`로 3×3 그리드 GUI를 열어 달성/미달성 현황과 진행률을 확인합니다.

---

## 🎮 명령어

### 플레이어
| 명령어 | 설명 |
|---|---|
| `/도움말` (`/help`, `/?`) | 명령어 및 팁 안내 |
| `/인증`, `/인증정보` | 리로스쿨 인증 시작 / 내 인증 정보 확인 |
| `/stat` (`/스탯`) | 스탯 분배 GUI |
| `/퀘스트` (`/quest`, `/q`) | 진행 중인 퀘스트 목록 |
| `/도전과제` (`/업적`) | 달성 업적 GUI |
| `/lobby` (`/l`) | 로비로 이동 |

### 관리자
| 명령어 | 설명 |
|---|---|
| `/인증목록` | 현재 접속자 인증 현황 (`ishsrpg.auth.admin`) |
| `/아이템지급 [플레이어] [아이템ID]` | 커스텀 아이템 지급 |
| `/입장허용 [열기\|닫기]` | 서버는 켠 채로 신규 입장만 허용/차단 |
| `/npc ...` | NPC 생성/관리는 Citizens 플러그인 명령어 사용 |

---

## 🧩 연동 플러그인

| 역할 | 플러그인 |
|---|---|
| NPC | Citizens |
| 퀘스트/대화 | BetonQuest |
| 몬스터 | MythicMobs |
| 플레이스홀더 | PlaceholderAPI |

`%ishsrpg_club%` 등 커스텀 플레이스홀더로 동아리/레벨 정보를 다른 플러그인에서도 활용할 수 있습니다.

## ⚙️ 서버에 적용하기

1. 위 플러그인들과 이 플러그인의 빌드 결과물(`build/libs/*.jar`)을 서버 `plugins/` 폴더에 넣습니다.
2. [`configs/`](configs) 폴더 안의 BetonQuest 설정을 서버의 `plugins/BetonQuest/QuestPackages/` 경로에 그대로 복사합니다. 자세한 방법은 [configs/README.md](configs/README.md) 참고.
3. 서버를 켜면 자동으로 `config.yml`이 생성됩니다 — 시작 스탯, 입장 허용 여부 등을 여기서 조정할 수 있습니다.
4. 코드가 아닌 **설정 변경**(퀘스트 대사, NPC 배치, 몬스터 스탯 등)은 각 플러그인의 `.yml` 파일만 수정하면 되고, 코드(`.java`) 변경 시에만 재빌드가 필요합니다.

> ⚠️ 코드를 수정한 뒤에는 `/reload`로 반영되지 않습니다. Paper/Bukkit의 `/reload`는 디스크의 새 jar를 다시 읽지 않으므로, 서버를 완전히 재시작해야 합니다.

## 🛠️ 빌드 (개발자용)

```bash
git clone https://github.com/dohye0508/ishs_rpg.git
cd ishs_rpg
./gradlew build
```

빌드된 jar는 `build/libs/`에 생성됩니다.

## 📁 문서

- [docs/NPC_LIST.md](docs/NPC_LIST.md) — NPC ID 매핑 가이드
- [docs/ITEM_LIST.md](docs/ITEM_LIST.md) — 커스텀 아이템 목록 및 지급 방법
- [docs/TUTORIAL_GUIDE.md](docs/TUTORIAL_GUIDE.md) — 신입생 튜토리얼 가이드
- [docs/WORLDVIEW.md](docs/WORLDVIEW.md) — 세계관 설정
- [configs/README.md](configs/README.md) — 사전 구성 파일 적용 방법
