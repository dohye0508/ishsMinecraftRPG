package kr.ishs.raibit.ishsMinecraftRpg.auth;

/**
 * 리로스쿨 인증을 통해 얻은 플레이어 정보
 */
public class PlayerAuthData {
    private final String playerName;      // 마인크래프트 닉네임
    private final String realName;        // 실명
    private final String studentId;       // 학번 (예: 30412)
    private final String generation;      // 기수 (예: 30)
    private final boolean isStudent;      // 학생 여부 (false = 교사)

    public PlayerAuthData(String playerName, String realName, String studentId, String generation, boolean isStudent) {
        this.playerName = playerName;
        this.realName = realName;
        this.studentId = studentId;
        this.generation = generation;
        this.isStudent = isStudent;
    }

    public String getPlayerName() { return playerName; }
    public String getRealName()   { return realName; }
    public String getStudentId()  { return studentId; }
    public String getGeneration() { return generation; }
    public boolean isStudent()    { return isStudent; }

    @Override
    public String toString() {
        return "PlayerAuthData{" +
                "playerName='" + playerName + '\'' +
                ", realName='" + realName + '\'' +
                ", studentId='" + studentId + '\'' +
                ", generation='" + generation + '\'' +
                ", isStudent=" + isStudent +
                '}';
    }
}
