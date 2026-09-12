# 📂 ISHS RPG 사전 구성 파일 모음 (configs/)

본 폴더에는 마인크래프트 서버 운영 시 외부 플러그인(`BetonQuest`, `MythicMobs` 등)에 **복사해서 붙여넣기만 하면 즉시 적용되는 사전 설정 YML 파일들**이 들어있습니다.

## 🚀 적용 방법

서버 구동기 폴더의 `plugins/` 디렉토리에 맞춰 아래 폴더 내용물 전체를 그대로 복사(Copy & Paste)하세요.

```text
c:\Users\...\server\plugins\
  ├── BetonQuest\
  │   └── QuestPackages\
  │       └── default\
  │           ├── main.yml          <-- (configs/BetonQuest/QuestPackages/default/main.yml)
  │           └── conversations\
  │               └── tutorial.yml  <-- (configs/BetonQuest/QuestPackages/default/conversations/tutorial.yml)
  └── MythicMobs\
      └── Mobs\
          └── beginners.yml     <-- (configs/MythicMobs/Mobs/beginners.yml 복사)
```

## 📜 사전 구성된 콘텐츠 목록

1. **BetonQuest (퀘스트 및 대화)**
   - `tutorial.yml`: 본관 앞 교감선생님 NPC의 신입생 튜토리얼 대화 (동아리 안내, `Shift + F` 스킬 설명, 기본 무기 지급)
   - `main.yml`: `%ishsrpg_club%` 변수 기반 동아리별 분기 조건 및 튜토리얼 완료 이벤트

2. **MythicMobs (몬스터)**
   - `beginners.yml`: Chapter 2 신입생 실습용 몬스터 4종 (`변종 실험쥐`, `방사능 토끼`, `점성 슬라임`, `오작동 보안 드론`)
