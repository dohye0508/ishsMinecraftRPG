package kr.ishs.raibit.ishsMinecraftRpg.ranking;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import org.bukkit.FluidCollisionMode;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

/**
 * /랭킹판 설치  - 바라보는 벽에 랭킹판을 붙인다. (위치는 ranking.yml 에 적힌다)
 * /랭킹판 삭제  - 랭킹판을 없앤다.
 * 크기, 문구, 갱신 주기는 ranking.yml 을 고친 뒤 /설정리로드. (관리자 전용)
 */
public class RankingCommand implements CommandExecutor {

    private static final double REACH = 12.0;

    private final IshsMinecraftRpg plugin;

    public RankingCommand(IshsMinecraftRpg plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            Messages.send(sender, "common.no-permission");
            return true;
        }
        if (args.length == 0) {
            Messages.send(sender, "ranking-board.usage");
            return true;
        }
        switch (args[0]) {
            case "설치", "install" -> install(sender);
            case "삭제", "remove" -> remove(sender);
            default -> Messages.send(sender, "ranking-board.usage");
        }
        return true;
    }

    private void install(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Messages.send(sender, "common.players-only");
            return;
        }
        RayTraceResult hit = player.rayTraceBlocks(REACH, FluidCollisionMode.NEVER);
        if (hit == null || hit.getHitBlockFace() == null) {
            Messages.send(player, "ranking-board.look-at-wall");
            return;
        }
        BlockFace face = hit.getHitBlockFace();
        if (face == BlockFace.UP || face == BlockFace.DOWN) {
            Messages.send(player, "ranking-board.look-at-wall");
            return;
        }

        // 벽에서 살짝 띄워, 벽면이 향한 방향(바라보는 사람 쪽)으로 글씨를 붙인다
        Vector normal = face.getDirection();
        Vector at = hit.getHitPosition().add(normal.clone().multiply(0.03));
        float yaw = (float) Math.toDegrees(Math.atan2(-normal.getX(), normal.getZ()));
        String inline = String.format(Locale.ROOT, "{x: %.2f, y: %.2f, z: %.2f, yaw: %d}",
                at.getX(), at.getY(), at.getZ(), Math.round(yaw));

        if (edit(sender, text -> RankingConfig.withLocation(text, player.getWorld().getName(), inline))) {
            Messages.send(player, "ranking-board.installed",
                    "x", (int) Math.floor(at.getX()), "y", (int) Math.floor(at.getY()), "z", (int) Math.floor(at.getZ()));
        }
    }

    private void remove(CommandSender sender) {
        if (edit(sender, RankingConfig::withoutLocation)) {
            Messages.send(sender, "ranking-board.removed");
        }
    }

    private boolean edit(CommandSender sender, java.util.function.UnaryOperator<String> change) {
        File file = new File(plugin.getDataFolder(), "ranking.yml");
        try {
            String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            Files.writeString(file.toPath(), change.apply(text), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Messages.send(sender, "ranking-board.write-failed", "error", e.getMessage());
            return false;
        }
        plugin.reloadRanking();
        return true;
    }
}
