package kr.ishs.raibit.ishsMinecraftRpg;

import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderHook extends PlaceholderExpansion {

    private final IshsMinecraftRpg plugin;

    public PlaceholderHook(IshsMinecraftRpg plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ishsrpg";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Raibit";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; 
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        PlayerData pd = PlayerManager.getInstance().getPlayerData(player.getUniqueId());
        if (pd == null) {
            return "";
        }

        // %ishsrpg_club%
        if (params.equalsIgnoreCase("club")) {
            return pd.hasClub() ? pd.getClub() : "미가입";
        }
        
        // %ishsrpg_level%
        if (params.equalsIgnoreCase("level")) {
            return String.valueOf(pd.getLevel());
        }

        return null; // 알 수 없는 플레이스홀더
    }
}
