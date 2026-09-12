package kr.ishs.raibit.ishsMinecraftRpg.auth;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 인천과학고 리로스쿨(iscience.riroschool.kr) 로그인 및 회원 정보 파싱
 *
 * 실제 마이페이지 구조 (파이썬 디버그로 확인):
 *   tr → td.td_title("아이디") + td.input_disabled("25-10116") + ... td.td_title("이름") + td.input_disabled("이도혜")
 *   tr → td.td_title("비밀번호") + td.td_title("학번") + td.input_disabled("20415")
 *
 * 이름 = td_title가 "\uc774\ub984"(이름)인 행의 input_disabled 값
 * 학번 = td_title가 "\ud559\ubc88"(학번)인 행의 input_disabled 값
 */
public class RiroSchoolAuth {

    private static final String BASE_URL   = "https://iscience.riroschool.kr";
    private static final String AJAX_URL   = BASE_URL + "/ajax.php";
    private static final String USER_URL   = BASE_URL + "/user.php";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    // \uad50\uc6d0=교원, \uad50\uc0ac=교사, \uad50\uc9c1\uc6d0=교직원, \uc120\uc0dd=선생
    private static final String[] TEACHER_KEYWORDS      = {"\uad50\uc6d0", "\uad55\ud558", "\uad50\uc9c1\uc6d0", "\uc120\uc0dd"};
    // \ud559\uc0dd=학생, \ud559\ubd80=학부, \uc9f8\uc5c5=졸업
    private static final String[] STUDENT_ANTI_KEYWORDS = {"\ud559\uc0dd", "\ud559\ubd80", "\uc1b8\uc5c5"};

    private final Logger logger;

    public RiroSchoolAuth(Logger logger) {
        this.logger = logger;
    }

    public PlayerAuthData login(String playerName, String userId, String password) {
        try {
            Map<String, String> cookies = new HashMap<>();

            // 1. 로그아웃으로 세션 초기화 (쿠키 수집)
            try {
                HttpURLConnection logoutConn = openPost(USER_URL + "?action=user_logout", "", null);
                collectCookies(logoutConn, cookies);
                logoutConn.disconnect();
            } catch (Exception ignored) {}

            // 2. ajax.php 로그인 - HttpURLConnection으로 JSON 원문 수신
            String loginBody = "app=user&mode=login&userType=1"
                    + "&id="       + java.net.URLEncoder.encode(userId,   "UTF-8")
                    + "&pw="       + java.net.URLEncoder.encode(password, "UTF-8")
                    + "&deeplink=&redirect_link=";

            HttpURLConnection loginConn = openPost(AJAX_URL, loginBody, cookies);
            collectCookies(loginConn, cookies);

            String jsonText = readBody(loginConn);
            loginConn.disconnect();

            logger.info("[RiroAuth] \ub85c\uadf8\uc778 \uc751\ub2f5: " + jsonText);

            String code = extractJsonField(jsonText, "code");
            logger.info("[RiroAuth] \ud30c\uc2f1\ub41c code: " + code);

            if ("902".equals(code)) {
                logger.warning("[RiroAuth] \uc544\uc774\ub514/\ube44\ubc00\ubc88\ud638 \uc624\ub958 - userId: " + userId);
                return null;
            }
            if (!"000".equals(code)) {
                logger.warning("[RiroAuth] \ub85c\uadf8\uc778 \uc2e4\ud328 (code=" + code + ") - userId: " + userId);
                return null;
            }

            String token = extractJsonField(jsonText, "token");
            if (token == null || token.isEmpty()) {
                logger.warning("[RiroAuth] \ud1a0\ud070 \ubbf8\ubc1c\uae09 - userId: " + userId);
                return null;
            }
            cookies.put("cookie_token", token);

            // 3. user.php 마이페이지 - HttpURLConnection으로 HTML 수신 후 Jsoup 파싱
            String mypageBody = "pw=" + java.net.URLEncoder.encode(password, "UTF-8");
            HttpURLConnection userConn = openPost(USER_URL, mypageBody, cookies);

            String htmlBody = readBody(userConn);
            userConn.disconnect();

            Document doc = Jsoup.parse(htmlBody, USER_URL);

            // 디버그: td_title 전체 목록 출력
            Elements allTitles = doc.select(".td_title");
            StringBuilder titleLog = new StringBuilder("[RiroAuth] td_title \ubaa9\ub85d: ");
            for (Element t : allTitles) titleLog.append("[").append(t.text().trim()).append("]");
            logger.info(titleLog.toString());

            // 4. 계정 유형 판별 (\ud1b5\ud569\uc544\uc774\ub514=통합아이디)
            String accountType = "normal";
            if (!allTitles.isEmpty() && "\ud1b5\ud569\uc544\uc774\ub514".equals(allTitles.first().text().trim())) {
                accountType = "integrated";
            }
            logger.info("[RiroAuth] accountType: " + accountType);

            // 5. 파싱 진행
            if ("normal".equals(accountType)) {
                return parseNormal(playerName, userId, doc);
            } else {
                return parseIntegrated(playerName, userId, doc);
            }

        } catch (IOException e) {
            logger.severe("[RiroAuth] \ud1b5\uc2e0 \uc624\ub958: " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────
    //  파싱 메서드
    // ─────────────────────────────────────────────

    private PlayerAuthData parseNormal(String playerName, String userId, Document doc) {
        Element elStudent   = doc.selectFirst("span.m_level3, span.m_level1, span.m_level2");
        String studentText  = elStudent != null ? elStudent.text().trim() : "";

        // \uc774\ub984=이름, \uc131\uba85=성명, \ud559\ubc88=학번
        String name             = extractByTitle(doc, "\uc774\ub984", "\uc131\uba85");
        String studentNumberRaw = extractByTitle(doc, "\ud559\ubc88", "\ud559\ubc88/\uc0ac\ubc88");

        logger.info("[RiroAuth] parseNormal \u2192 studentText=" + studentText
                + ", name=" + name + ", studentNumberRaw=" + studentNumberRaw);

        if (name.isEmpty()) {
            logger.warning("[RiroAuth] parseNormal \uc2e4\ud328: \uc774\ub984 \ud30c\uc2f1 \uc548\ub428");
            return null;
        }
        if (isTeacherRole(studentText)) {
            return new PlayerAuthData(playerName, name, "\uad50\uc0ac", "-", false);
        }

        String studentNumber = parseStudentNumber(studentNumberRaw);
        int gen = calcGeneration(userId);
        if (gen == 0 && isDigits(studentNumberRaw) && studentNumberRaw.length() >= 4)
            gen = calcGeneration(studentNumberRaw);

        String genStr = gen > 0 ? String.valueOf(gen) : "\ubbf8\ud655\uc778";
        String sidStr = !studentNumber.isEmpty() ? studentNumber
                : (studentNumberRaw.isEmpty() ? "\ud559\uc0dd" : studentNumberRaw);

        return new PlayerAuthData(playerName, name, sidStr, genStr, true);
    }

    private PlayerAuthData parseIntegrated(String playerName, String userId, Document doc) {
        String riroId = "", studentText = "";
        Elements elemFixes = doc.select(".elem_fix");
        if (!elemFixes.isEmpty()) {
            String raw = elemFixes.first().text();
            if (raw.length() >= 8)  riroId      = raw.substring(0, 8).trim();
            if (raw.length() >  15) studentText = raw.substring(15, raw.length() - 1).trim();
        }

        String name             = extractByTitle(doc, "\uc774\ub984", "\uc131\uba85");
        String studentNumberRaw = extractByTitle(doc, "\ud559\ubc88", "\ud559\ubc88/\uc0ac\ubc88");

        logger.info("[RiroAuth] parseIntegrated \u2192 studentText=" + studentText
                + ", name=" + name + ", studentNumberRaw=" + studentNumberRaw);

        if (name.isEmpty()) {
            logger.warning("[RiroAuth] parseIntegrated \uc2e4\ud328: \uc774\ub984 \ud30c\uc2f1 \uc548\ub428");
            return null;
        }
        if (isTeacherRole(studentText)) {
            return new PlayerAuthData(playerName, name, "\uad50\uc0ac", "-", false);
        }

        String studentNumber = parseStudentNumber(studentNumberRaw);
        int gen = calcGeneration(riroId);
        if (gen == 0) gen = calcGeneration(userId);
        if (gen == 0 && isDigits(studentNumberRaw) && studentNumberRaw.length() >= 4)
            gen = calcGeneration(studentNumberRaw);

        String genStr = gen > 0 ? String.valueOf(gen) : "\ubbf8\ud655\uc778";
        String sidStr = !studentNumber.isEmpty() ? studentNumber
                : (studentNumberRaw.isEmpty() ? "\ud559\uc0dd" : studentNumberRaw);

        return new PlayerAuthData(playerName, name, sidStr, genStr, true);
    }

    /**
     * 전체 문서에서 .td_title(항목명)을 찾아 해당 요소의 다음 형제 요소(nextElementSibling)에서 값을 추출
     */
    private String extractByTitle(Document doc, String... targetTitles) {
        for (Element titleEl : doc.select(".td_title")) {
            String title = titleEl.text().trim();
            boolean isTarget = false;
            for (String t : targetTitles) {
                if (title.equals(t)) {
                    isTarget = true;
                    break;
                }
            }
            if (!isTarget) continue;

            Element nextEl = titleEl.nextElementSibling();
            if (nextEl != null) {
                // 1) .input_disabled 클래스를 가진 하위 요소 찾기
                Element valEl = nextEl.selectFirst(".input_disabled");
                if (valEl != null && !valEl.text().trim().isEmpty()) {
                    return valEl.text().trim();
                }

                // 2) readonly 또는 disabled input 태그 찾기
                Element inputEl = nextEl.selectFirst("input[readonly], input[disabled]");
                if (inputEl != null && !inputEl.attr("value").trim().isEmpty()) {
                    return inputEl.attr("value").trim();
                }

                // 3) 그냥 엘리먼트 자체의 텍스트 반환
                String text = nextEl.text().trim();
                if (!text.isEmpty()) {
                    return text;
                }
            }
        }
        return "";
    }

    // ─────────────────────────────────────────────
    //  유틸 메서드
    // ─────────────────────────────────────────────

    private boolean isTeacherRole(String text) {
        boolean hasTeacher = false;
        for (String kw : TEACHER_KEYWORDS) { if (text.contains(kw)) { hasTeacher = true; break; } }
        if (!hasTeacher) return false;
        for (String kw : STUDENT_ANTI_KEYWORDS) { if (text.contains(kw)) return false; }
        return true;
    }

    private String parseStudentNumber(String raw) {
        if (isDigits(raw) && raw.length() >= 3) return raw.substring(0, 1) + raw.substring(2);
        return raw;
    }

    private int calcGeneration(String id) {
        if (id != null && id.length() >= 2 && isDigits(id.substring(0, 2))) {
            try { return Integer.parseInt("20" + id.substring(0, 2)) - 1994 + 1; }
            catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private boolean isDigits(String s) { return s != null && s.matches("^[0-9]+$"); }

    private String extractJsonField(String json, String key) {
        // "key":"value" 또는 "key":number 형태 모두 지원, 앞뒤 공백 허용
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"|\"" + key + "\"\\s*:\\s*([0-9]+)")
                .matcher(json);
        if (m.find()) return m.group(1) != null ? m.group(1) : m.group(2);
        return null;
    }

    /** HttpURLConnection POST 요청 생성 (쿠키 헤더 포함) */
    private HttpURLConnection openPost(String url, String body, Map<String, String> cookies) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(8_000);
        conn.setReadTimeout(12_000);
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        if (cookies != null && !cookies.isEmpty()) {
            StringBuilder cookieHdr = new StringBuilder();
            for (Map.Entry<String, String> e : cookies.entrySet()) {
                if (cookieHdr.length() > 0) cookieHdr.append("; ");
                cookieHdr.append(e.getKey()).append("=").append(e.getValue());
            }
            conn.setRequestProperty("Cookie", cookieHdr.toString());
        }
        if (body != null && !body.isEmpty()) {
            byte[] bytes = body.getBytes("UTF-8");
            conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            try (OutputStream os = conn.getOutputStream()) { os.write(bytes); }
        }
        return conn;
    }

    /** 응답 InputStream을 UTF-8 문자열로 읽기 */
    private String readBody(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        }
    }

    /** 응답 Set-Cookie 헤더를 name=value로 파싱하여 cookies 맵에 수집 */
    private void collectCookies(HttpURLConnection conn, Map<String, String> cookies) {
        List<String> setCookies = conn.getHeaderFields().get("Set-Cookie");
        if (setCookies == null) return;
        for (String sc : setCookies) {
            String[] parts = sc.split(";")[0].split("=", 2);
            if (parts.length == 2) cookies.put(parts[0].trim(), parts[1].trim());
        }
    }
}
