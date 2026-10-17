# 🗡️ ISHS RPG 커스텀 아이템 가이드

커스텀 아이템의 **이름, 재료, 설명**은 [`configs/ishsMinecraftRpg/items.yml`](../configs/ishsMinecraftRpg/items.yml)에서 코드 수정 없이 편집합니다.
아이템의 **효과**는 아이템ID에 코드가 연결되어 있습니다. `/give` 명령어로는 지급할 수 없고,
전용 명령어 **`/아이템지급 [플레이어] [아이템ID]`** (OP 전용)로만 지급할 수 있습니다.

> 이 표는 효과 요약입니다. 이름과 재료는 `items.yml`이 항상 최신입니다.

## 📋 아이템 목록

| 아이템ID | 이름 | 효과 |
| :--- | :--- | :--- |
| `demerit_card` | 왕사감의 벌점카드 | 공격 적중 시 대상에게 약화(Weakness) 부여, 6초 쿨타임 |
| `wanil_staff` | 완일쌤의 지팡이 | 특수 효과 없는 평범한 무기 (이름/설명만 커스텀) |
| `jasmine_flower` | 자스민 꽃 | 우클릭 시 체력 6 회복, 소모품 |
| `schema` | 스키마 | 우클릭 시 90초간 공격력(힘 II)+이동속도(신속 II) 증가, 소모품 |
| `changbong_spear` | 이창봉도 창이다 | 공격 적중 시 대상을 강하게 찔러 밀쳐냄 |
| `changbong_rod` | 이창봉도 봉이다 | 공격 적중 시 대상을 후려쳐 하늘로 띄움 |

새 ID를 `items.yml`에 추가하면 **효과 없는 장식용 아이템**이 만들어집니다. 효과가 필요하면 개발자에게 요청하세요.

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

## 🔧 새 아이템을 추가하려면

이 4종은 순수 YAML로는 관리할 수 없고(효과가 코드로 동작하기 때문), 새 아이템을 추가할 때는
[`CustomItems.java`](../src/main/java/kr/ishs/raibit/ishsMinecraftRpg/item/CustomItems.java)에
`create()` 분기 하나만 추가하면 됩니다. 회복/버프처럼 우클릭 시 발동하는 효과는
[`CustomItemListener.java`](../src/main/java/kr/ishs/raibit/ishsMinecraftRpg/item/CustomItemListener.java)에서 처리합니다.
반대로 이름/설명만 있고 특수 효과가 없는 아이템(예: 완일쌤의 지팡이)은 `CustomItems.java`만 수정하면 끝입니다.
