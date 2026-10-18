# 기획팀·기여 안내

GitHub 저장소 상단의 **Contributing** 탭에 표시되는 문서입니다. 코드를 몰라도 `.yml` 파일만 고쳐 서버 내용을 바꿀 수 있습니다.

## 📋 기획팀: 무엇을 어디서 고치나

| 바꾸고 싶은 것 | 파일 (`configs/` 기준) | 적용 |
|---|---|---|
| 스탯 공식, 레벨, 보상, 게임 규칙 | `ishsMinecraftRpg/gameplay.yml` | `/설정리로드` |
| 동아리 (이름, 아이콘, 가입 보너스) | `ishsMinecraftRpg/clubs.yml` | `/설정리로드` |
| 스킬 (쿨타임, 마나, 피해량) | `ishsMinecraftRpg/skills.yml` | `/설정리로드` |
| 커스텀 아이템 (이름, 설명, 기능) | `ishsMinecraftRpg/items.yml` | `/설정리로드` |
| NPC (이름, 스킨, 위치, 연결할 대화) | `ishsMinecraftRpg/npcs.yml` | `/설정리로드` |
| 동아리 랭킹판 (크기, 문구) | `ishsMinecraftRpg/ranking.yml` | `/설정리로드` |
| 도전과제, 안내·GUI·사이드바 문구, `/도움말` | `achievements.yml`, `messages.yml`, `help.yml` | `/설정리로드` |
| 맵 경계·스폰, 리로스쿨 인증 | `ishsMinecraftRpg/config.yml` | `/설정리로드` |
| 퀘스트 목표·보상 | `BetonQuest/QuestPackages/default/main.yml` | `/bq reload` |
| NPC 대사·선택지 | `BetonQuest/QuestPackages/default/conversations/<이름>.yml` | `/bq reload` |
| 몬스터 | `MythicMobs/Mobs/*.yml` | `/mm reload` |

NPC 위치는 게임에서 명령으로 관리합니다. (`/엔피씨 추가|이동|삭제|목록`, OP 전용) 랭킹판 위치도 `/랭킹판 설치|삭제`로 정합니다.

## 🔄 작업 순서

1. `configs/` 안의 파일을 수정합니다.
2. `configs/배포.bat`을 실행합니다. (서버 `plugins/`로 복사, 덮어쓰기 전 자동 백업)
3. 게임에서 OP가 위 표의 적용 명령을 입력하고, 서버 콘솔에 경고가 없는지 확인합니다.
4. 이상이 없으면 브랜치에 커밋하고 Pull Request를 올립니다.

자세한 방법(아이템·NPC·퀘스트 추가 예시, 문제 해결)은 **[기획팀 가이드](docs/기획팀_가이드.md)**를 봅니다. 폴더 구조와 배포 스크립트는 [configs/README.md](configs/README.md)에 있습니다.

## ✅ 수정 후 검증

Java와 Gradle이 있는 PC에서는 서버 없이 설정 파일의 연결과 형식을 확인할 수 있습니다.

```bash
./gradlew test
```

## 🌿 브랜치와 Pull Request

1. `main`에서 작업 영역에 맞는 브랜치를 만듭니다. (`feature/quest`, `feature/npc`, `feature/skillset` 또는 `feature/<영역명>`)
2. 변경을 커밋합니다. 한 커밋에는 한 가지 변경만 담습니다.
3. `./gradlew build`가 통과하는지 확인합니다. (코드를 고쳤다면)
4. `main`을 대상으로 Pull Request를 생성합니다.

브랜치 규칙은 [README의 브랜치 전략](README.md#-브랜치-전략)을 따릅니다.
