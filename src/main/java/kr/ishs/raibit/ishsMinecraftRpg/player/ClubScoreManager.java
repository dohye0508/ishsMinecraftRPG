package kr.ishs.raibit.ishsMinecraftRpg.player;

import java.util.HashMap;
import java.util.Map;

public class ClubScoreManager {
    private static ClubScoreManager instance = new ClubScoreManager();
    private final Map<String, Integer> clubScores = new HashMap<>();

    private ClubScoreManager() {
        String[] clubs = {"LOGIC", "WIN", "TNT", "LOTTOL", "QUASAR", "RAIBIT", "PLUTONIUM"};
        for (String c : clubs) {
            clubScores.put(c, 0);
        }
    }

    public static ClubScoreManager getInstance() {
        return instance;
    }

    public int getScore(String club) {
        return clubScores.getOrDefault(club, 0);
    }

    public void addScore(String club, int amount) {
        if (clubScores.containsKey(club)) {
            clubScores.put(club, clubScores.get(club) + amount);
        }
    }
}
