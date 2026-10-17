# 🗡️ ISHS RPG 커스텀 아이템 가이드

커스텀 아이템의 **이름, 재료, 설명, 효과**는 모두 [`configs/ishsMinecraftRpg/items.yml`](../configs/ishsMinecraftRpg/items.yml)에서 코드 수정 없이 편집합니다.
`/give` 명령어로는 지급할 수 없고, 전용 명령어 **`/아이템지급 [플레이어] [아이템ID]`** (OP 전용)로만 지급할 수 있습니다.

> 이 표는 기본 아이템의 효과 요약입니다. 실제 값(수치, 지속시간 등)은 `items.yml`이 항상 최신입니다.

## 📋 아이템 목록

| 아이템ID | 이름 | 효과 |
| :--- | :--- | :--- |
| `demerit_card` | 왕사감의 벌점카드 | 공격 적중 시 대상에게 약화(Weakness II) 5초 부여, 6초 쿨타임 |
| `wanil_staff` | 완일쌤의 지팡이 | 특수 효과 없는 평범한 무기 (이름/설명만 커스텀) |
| `jasmine_flower` | 자스민 꽃 | 우클릭 시 체력 6 회복, 소모품 |
| `schema` | 스키마 | 우클릭 시 90초간 공격력(힘 II)+이동속도(신속 II) 증가, 소모품 |
| `changbong_spear` | 이창봉도 창이다 | 공격 적중 시 대상을 강하게 찔러 밀쳐냄 |
| `changbong_rod` | 이창봉도 봉이다 | 공격 적중 시 대상을 후려쳐 하늘로 띄움 |
| `comfort_blanket` | 애착담요 | 우클릭 시 30초간 이동속도 소폭 증가(속도 I), 재사용 대기 60초, 소모되지 않음 |

## 🚀 지급 방법

* **인게임 직접 지급**: `/아이템지급 [닉네임] [아이템ID]`
  예) `/아이템지급 도혜 jasmine_flower`
* **BetonQuest 퀘스트 보상으로 지급**: `main.yml`의 `events:` 섹션에 아래처럼 `command` 이벤트로 등록하면
  다른 보상들과 동일하게 퀘스트 완료 시 자동 지급됩니다.
  ```yaml
  events:
    give_jasmine: "command 아이템지급 %player% jasmine_flower"
  ```
  이렇게 만든 이벤트를 기존 보상 `folder` 이벤트(`logic_quest_reward` 등)에 추가해서 연결하면 됩니다.

## 🔧 새 아이템 추가 / 효과 수정

`items.yml`에 새 아이템 ID(영어 소문자)를 추가하고, 기능은 `effects`에 정의합니다. 코드 수정은 필요 없습니다.

```yaml
items:
  speed_candy:
    material: SUGAR
    name: "&e스피드 사탕"
    lore:
      - "&7먹으면 잠깐 빨라진다."
    effects:
      - trigger: RIGHT_CLICK        # 우클릭 시 발동
        consume: true               # 사용하면 1개 소모
        cooldown: 3                 # 재사용 대기시간 3초
        message: "&e🍬 몸이 가벼워졌다!"
        sound: ENTITY_PLAYER_BURP
        actions:
          - type: POTION
            potion: SPEED
            seconds: 30
            level: 2
```

### 효과 구성

| 항목 | 설명 |
| :--- | :--- |
| `trigger` | `RIGHT_CLICK`(우클릭) / `HIT`(들고 공격 적중) |
| `cooldown`, `consume` | 재사용 대기시간(초), 사용 시 1개 소모(`RIGHT_CLICK` 전용) |
| `message`, `target-message` | 사용자 / 맞은 대상(`HIT` 전용)에게 보낼 메시지 |
| `sound`, `particle` | 효과음, 파티클 (대문자 이름. 상세 옵션은 `items.yml` 상단 주석 참고) |
| `actions` | 실제 동작 목록 |

### 동작 종류 (`actions`)

| type | 설명 | 주요 값 |
| :--- | :--- | :--- |
| `HEAL` | 사용자 체력 회복 | `amount` (2 = 하트 1칸) |
| `POTION` | 포션 효과 부여 | `potion`, `seconds`, `level`, `target`(`SELF`/`TARGET`) |
| `KNOCKBACK` | 맞은 대상을 밀쳐냄 (`HIT` 전용) | `strength`, `lift` |
| `LAUNCH` | 맞은 대상을 위로 띄움 (`HIT` 전용) | `height` |

한 아이템에 `effects`를 여러 개, 한 효과에 `actions`를 여러 개 넣을 수 있습니다. (예: `schema`는 힘과 신속을 한 번에 부여)

위 4가지 동작으로 표현할 수 없는 새로운 종류의 효과(예: 소환, 순간이동)는 코드 추가가 필요합니다.
동작 실행은 [`CustomItemListener.java`](../src/main/java/kr/ishs/raibit/ishsMinecraftRpg/item/CustomItemListener.java), YAML 해석은 [`ItemEffect.java`](../src/main/java/kr/ishs/raibit/ishsMinecraftRpg/item/ItemEffect.java)에 있습니다.
