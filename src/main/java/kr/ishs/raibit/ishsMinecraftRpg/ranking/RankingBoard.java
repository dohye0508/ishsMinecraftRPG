package kr.ishs.raibit.ishsMinecraftRpg.ranking;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.config.ClubConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import kr.ishs.raibit.ishsMinecraftRpg.player.ClubScoreManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.List;

/**
 * 월드에 떠 있는 큰 글씨(TextDisplay)로 동아리 랭킹을 보여 주는 랭킹판.
 * 위치와 크기, 문구는 ranking.yml. 글씨 엔티티는 저장하지 않고(서버를 껐다 켜도 중복되지 않음)
 * 주기적으로 확인하며 없으면 다시 만든다.
 */
public final class RankingBoard {

    private final IshsMinecraftRpg plugin;
    private TextDisplay display;
    private BukkitTask task;

    public RankingBoard(IshsMinecraftRpg plugin) {
        this.plugin = plugin;
    }

    /** ranking.yml 이 바뀌었을 때 호출한다. 기존 글씨를 지우고 새 설정으로 다시 시작한다. */
    public void refresh() {
        remove();
        RankingConfig.Settings s = RankingConfig.get();
        if (!s.enabled || !s.hasLocation) return;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, s.updateTicks);
    }

    /** 글씨와 갱신 작업을 모두 없앤다. */
    public void remove() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (display != null) {
            if (display.isValid()) display.remove();
            display = null;
        }
    }

    private void tick() {
        RankingConfig.Settings s = RankingConfig.get();
        World world = s.world.isEmpty() ? defaultWorld() : Bukkit.getWorld(s.world);
        if (world == null) return;

        if (display == null || !display.isValid()) {
            // 그 자리의 청크가 불러와져 있을 때만 만든다. (사람이 없는 곳의 청크를 억지로 불러오지 않는다)
            if (!world.isChunkLoaded((int) Math.floor(s.x) >> 4, (int) Math.floor(s.z) >> 4)) return;
            display = spawn(world, s);
        }
        display.text(LegacyComponentSerializer.legacySection().deserialize(text(s)));
    }

    private TextDisplay spawn(World world, RankingConfig.Settings s) {
        Location loc = new Location(world, s.x, s.y, s.z, s.yaw, 0f);
        return world.spawn(loc, TextDisplay.class, d -> {
            d.setPersistent(false);
            d.setBillboard(Display.Billboard.FIXED);
            d.setAlignment(TextDisplay.TextAlignment.CENTER);
            d.setShadowed(true);
            d.setBackgroundColor(s.background ? Color.fromARGB(110, 0, 0, 0) : Color.fromARGB(0, 0, 0, 0));
            d.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(),
                    new Vector3f((float) s.scale, (float) s.scale, (float) s.scale), new AxisAngle4f()));
            d.setViewRange(1.5f);
        });
    }

    /** 제목과 순위 줄들을 이어 붙인 글 */
    private static String text(RankingConfig.Settings s) {
        List<String> lines = RankingConfig.rankedLines(ClubConfig.all(),
                club -> ClubScoreManager.getInstance().getScore(club.id), s.line);
        String body = String.join("\n", lines);
        return s.title.isBlank() ? body : Messages.color(s.title) + "\n" + body;
    }

    private static World defaultWorld() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NORMAL) return w;
        }
        return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
    }
}
