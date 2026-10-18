package kr.ishs.raibit.ishsMinecraftRpg.task;

/** 사이드바의 막대 그래프(예: ▰▰▰▱▱▱▱▱▱▱). 서버 없이 만들 수 있다. */
public final class ProgressBar {

    private ProgressBar() {}

    /**
     * @param ratio       채울 비율 (0~1, 범위를 벗어나면 잘라냄)
     * @param length      칸 수
     * @param filledColor 채워진 칸 앞에 붙일 색상 코드(이미 변환된 글자), 없으면 ""
     * @param emptyColor  빈 칸 앞에 붙일 색상 코드, 없으면 ""
     */
    public static String render(double ratio, int length, String filledColor, String filledChar,
                                String emptyColor, String emptyChar) {
        if (length < 1) return "";
        double safe = Double.isNaN(ratio) ? 0.0 : Math.max(0.0, Math.min(1.0, ratio));
        int filled = (int) Math.round(safe * length);
        return filledColor + filledChar.repeat(filled) + emptyColor + emptyChar.repeat(length - filled);
    }
}
