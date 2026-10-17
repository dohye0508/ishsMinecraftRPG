# 👥 ISHS RPG NPC 목록

NPC는 [`configs/ishsMinecraftRpg/npcs.yml`](../configs/ishsMinecraftRpg/npcs.yml)에서 관리합니다. 추가와 수정 방법은 [기획팀_가이드.md](기획팀_가이드.md)의 "퀘스트와 NPC 추가"를 참고합니다.

## 현재 NPC

| 키 | 이름 | 대화 파일 | 역할 |
| :--- | :--- | :--- | :--- |
| `principal` | 교장선생님 | `conversations/principal.yml` | 전쟁을 부추기는 최종 통제자 |
| `vice_principal` | 교감선생님 | `conversations/vice_principal.yml` | 첫 인증 환영 및 안내 |

> 좌표는 임시값입니다. 최초 스폰 근처에 두었으니 실제 위치로 고칩니다.

## 삭제된 NPC

행정실장, 7개 동아리 짱·부짱 14명, 1·2학년 사감쌤의 NPC와 대화 파일은 삭제되었습니다. 필요하면 Git 기록(`conversations/` 폴더)에서 복원합니다.
`main.yml`의 퀘스트(`events`, `objectives`)는 남아 있으며 새 NPC 대화에서 시작 이벤트(`start_*_quest`)로 연결할 수 있습니다.
