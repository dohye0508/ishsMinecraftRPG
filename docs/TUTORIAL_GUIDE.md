# 🎓 ISHS RPG 입장 흐름 가이드

서버 접속부터 동아리 선택, 첫 NPC 대화까지의 흐름과 각 단계의 수정 위치를 정리한 문서입니다.

---

## 🔄 입장 흐름

```text
[서버 접속]
   └─► [리로스쿨 인증 (ID/PW 채팅 입력)]            config.yml 의 auth
         └─► [인증 성공]
               └─► [동아리 선택 GUI 팝업]              clubs.yml
                     └─► 동아리 스탯 보너스 적용
                           └─► [최초 스폰]            config.yml 의 spawn.first
                                 └─► 교감선생님 NPC 대화  npcs.yml, conversations/vice_principal.yml
```

| 단계 | 수정 위치 |
|---|---|
| 인증 사용 여부, 인증 대기소 위치, 테스트 계정 | `ishsMinecraftRpg/config.yml` (`auth`) |
| 동아리 이름, 아이콘, 가입 보너스 | `ishsMinecraftRpg/clubs.yml` |
| 최초 스폰 위치, 맵 경계 | `ishsMinecraftRpg/config.yml` (`spawn`, `border`) |
| NPC 위치·이름·스킨 | `ishsMinecraftRpg/npcs.yml` 또는 게임의 `/엔피씨` 명령 |
| NPC 대사 | `BetonQuest/QuestPackages/default/conversations/*.yml` |

---

## 1. 스킬 사용법

스킬은 동아리와 무관하게 **스킬 아이템**을 들고 **우클릭**하면 발동합니다. 마나가 필요하고 쿨타임이 있습니다.

| 스킬 | 아이템 (재료 / 이름) | 마나 | 쿨타임 |
|---|---|---|---|
| 화염구 | 블레이즈 로드 / `파이어볼` | 20 | 5초 |
| 대지 가르기 | 다이아몬드 검 / `대지 가르기` | 30 | 10초 |
| 신성 폭발 | 마법이 부여된 책 / `신성 폭발` | 40 | 15초 |
| 치유의 빛 | 황금 사과 / `치유의 빛` | 25 | 8초 |

수치와 재료·이름은 `skills.yml`에서 바꿉니다. 아이템은 `/아이템지급` 또는 퀘스트 보상으로 지급합니다. 자세한 방법은 [기획팀 가이드](기획팀_가이드.md)를 참고합니다.

---

## 2. 교감선생님 NPC

현재 NPC는 교장선생님과 교감선생님입니다. ([NPC_LIST.md](NPC_LIST.md))
교감선생님은 `conversation: vice_principal`로 연결되어 있고 대사는 `conversations/vice_principal.yml`에 있습니다.

```yaml
conversations:
  vice_principal:
    quester: "교감선생님"
    first: start
    NPC_options:
      start:
        text: "&e[교감선생님]&f 인천과학고등학교 입학을 환영한다네, 신입생! ..."
        pointers: end_ok
```

대사를 고친 뒤 `/bq reload`로 적용합니다. 튜토리얼 보상(목검 지급 등)이나 퀘스트 시작을 이 대화에 연결하려면 `events:`에 `main.yml`의 이벤트 이름을 적습니다. (예시는 [기획팀 가이드](기획팀_가이드.md)의 "퀘스트와 NPC 추가")

---

## 3. 적용

1. `configs/배포.bat`을 실행합니다.
2. 게임에서 OP가 `/설정리로드`, `/bq reload`를 입력합니다.
3. 접속부터 NPC 대화까지 한 번 직접 진행해 확인합니다.
