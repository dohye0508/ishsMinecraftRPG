# 👥 ISHS RPG 주요 NPC ID 매핑 가이드

인게임에서 `/bq config add npcs [시민ID] [퀘스트]` 명령어를 사용할 필요 없이, 시민(Citizens) 플러그인에서 NPC의 ID만 아래 표에 맞게 설정해주면 자동으로 대화가 연결되도록 `main.yml`에 프로그래밍되어 있습니다.

> **💡 사용 방법**
> 1. 관리자 권한으로 게임 내에서 `/npc create [NPC이름]` 입력
> 2. 생성된 NPC를 바라보고 `/npc id` 확인
> 3. 만약 만들어진 NPC ID가 `5`번이라면, 그 NPC는 자동으로 **WIN 짱** 역할을 부여받게 됩니다!

---

## 🏛️ 중립 구역 (본관 로비)
| 시민 ID | NPC 대화(역할) | 주요 특징 |
| :---: | :--- | :--- |
| **0** | `vice_principal` (교감선생님) | 첫 인증 환영 및 안내 |
| **1** | `principal` (교장선생님) | 전쟁을 부추기는 최종 통제자 |
| **2** | `admin_chief` (행정실장) | 툴툴대며 장비 등을 판매/지원 |

---

## ⚔️ 7대 동아리 진영 (짱 & 부짱)
| 시민 ID | 동아리 진영 | NPC 대화(역할) | 퀘스트 및 특징 |
| :---: | :--- | :--- | :--- |
| **3** | **LOGIC (수학)** | `logic_zzang` (LOGIC 짱) | 가입 권유 (수식과 벡터의 힘) |
| **4** | **LOGIC (수학)** | `logic_buzzang` (LOGIC 부짱) | 거점 점령 및 타 진영생 3명 킬 임무 |
| **5** | **WIN (물리)** | `win_zzang` (WIN 짱) | 가입 권유 (압도적인 타격감, 중력) |
| **6** | **WIN (물리)** | `win_buzzang` (WIN 부짱) | 타 진영생 3명 킬 임무 |
| **7** | **TNT (화학)** | `tnt_zzang` (TNT 짱) | 가입 권유 (광역 폭발 특화) |
| **8** | **TNT (화학)** | `tnt_buzzang` (TNT 부짱) | 타 진영생 3명 킬 임무 |
| **9** | **LOTTOL (생명)** | `lottol_zzang` (LOTTOL 짱) | 가입 권유 (생체 에너지 흡수, 좀비) |
| **10** | **LOTTOL (생명)** | `lottol_buzzang` (LOTTOL 부짱) | 타 진영생 3명 킬 임무 |
| **11** | **QUASAR (천문)** | `quasar_zzang` (QUASAR 짱)| 가입 권유 (시공간 왜곡, 저격) |
| **12** | **QUASAR (천문)** | `quasar_buzzang` (QUASAR 부짱)| 타 진영생 3명 킬 임무 |
| **13** | **RAIBIT (정보)** | `raibit_zzang` (RAIBIT 짱) | 가입 권유 (시스템 해킹, 시야 차단) |
| **14** | **RAIBIT (정보)** | `raibit_buzzang` (RAIBIT 부짱)| 타 진영생 3명 킬 임무 |
| **15** | **PLUTONIUM (공학)** | `plutonium_zzang` (PLUTONIUM 짱)| 가입 권유 (메카닉 슈트, 절대 방어) |
| **16** | **PLUTONIUM (공학)** | `plutonium_buzzang` (PLUTONIUM 부짱)| 타 진영생 3명 킬 임무 |

---

### 🔧 팁 (ID 번호가 어긋났을 때)
만약 게임 안에서 실수로 NPC를 삭제해서 시민 ID 순서가 16번을 넘어가버렸거나 꼬였다면:
* 서버 폴더의 `configs/BetonQuest/QuestPackages/default/main.yml` 파일을 열고,
* `npcs:` 구역에서 해당 역할 옆의 `"citizens 번호"` 부분 숫자만 게임에 맞춰서 수정해 주시면 언제든지 1초 만에 다시 연결됩니다!
