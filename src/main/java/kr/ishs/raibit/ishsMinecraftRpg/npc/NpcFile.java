package kr.ishs.raibit.ishsMinecraftRpg.npc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * npcs.yml 을 글자 그대로 고친다. YAML 을 다시 쓰면 기획팀이 적어 둔 주석이 사라지므로,
 * 해당 NPC 의 줄만 바꾼다. (NPC 항목은 npcs: 아래 2칸 들여쓰기라고 가정한다)
 */
public final class NpcFile {

    private NpcFile() {}

    private static final Pattern LOCATION_LINE = Pattern.compile("^\\s+location:.*$");

    /** @return 고친 전체 글. 그 키의 NPC 가 없으면 null */
    public static String setLocation(String text, String key, String inlineLocation) {
        List<String> lines = new ArrayList<>(Arrays.asList(text.split("\n", -1)));
        int start = find(lines, key);
        if (start < 0) return null;
        int end = blockEnd(lines, start);

        for (int i = start + 1; i < end; i++) {
            if (LOCATION_LINE.matcher(stripCr(lines.get(i))).matches()) {
                lines.set(i, "    location: " + inlineLocation + cr(lines.get(i)));
                return String.join("\n", lines);
            }
        }
        lines.add(start + 1, "    location: " + inlineLocation + cr(lines.get(start)));
        return String.join("\n", lines);
    }

    /** @return NPC 항목을 지운 전체 글. 그 키의 NPC 가 없으면 null */
    public static String remove(String text, String key) {
        List<String> lines = new ArrayList<>(Arrays.asList(text.split("\n", -1)));
        int start = find(lines, key);
        if (start < 0) return null;
        int end = blockEnd(lines, start);
        // 항목 뒤의 빈 줄 하나도 함께 지운다
        if (end < lines.size() && stripCr(lines.get(end)).isBlank()) end++;
        lines.subList(start, end).clear();
        return String.join("\n", lines);
    }

    /** 키 줄("  key:")의 위치. npcs: 아래에 있는 것만 찾는다. */
    private static int find(List<String> lines, String key) {
        boolean inNpcs = false;
        Pattern keyLine = Pattern.compile("^  " + Pattern.quote(key) + ":\\s*$");
        for (int i = 0; i < lines.size(); i++) {
            String line = stripCr(lines.get(i));
            if (line.matches("^npcs:\\s*(#.*)?$")) {
                inNpcs = true;
            } else if (!line.isEmpty() && !line.startsWith(" ") && !line.startsWith("#")) {
                inNpcs = false; // 다른 최상위 항목
            } else if (inNpcs && keyLine.matcher(line).matches()) {
                return i;
            }
        }
        return -1;
    }

    /** 항목이 끝나는 줄(다음 NPC 키 줄, 다음 최상위 항목, 또는 파일 끝). 항목 사이의 주석/빈 줄은 다음 항목 쪽에 둔다. */
    private static int blockEnd(List<String> lines, int start) {
        int last = start; // 항목에 속하는 마지막 줄
        for (int i = start + 1; i < lines.size(); i++) {
            String line = stripCr(lines.get(i));
            if (line.isBlank() || line.trim().startsWith("#") && !line.startsWith("    ")) continue;
            if (line.startsWith("    ")) {
                last = i;
            } else {
                break;
            }
        }
        return last + 1;
    }

    private static String stripCr(String s) {
        return s.endsWith("\r") ? s.substring(0, s.length() - 1) : s;
    }

    private static String cr(String s) {
        return s.endsWith("\r") ? "\r" : "";
    }
}
