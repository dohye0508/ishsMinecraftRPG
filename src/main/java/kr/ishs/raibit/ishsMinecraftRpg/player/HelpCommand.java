package kr.ishs.raibit.ishsMinecraftRpg.player;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /도움말 명령어 처리기
 */
@SuppressWarnings("deprecation")
public class HelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }

        Player player = (Player) sender;

        player.sendMessage(ChatColor.GOLD + "================= [ 인곽 RPG 도움말 ] =================");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "📜 [ 유저 기본 명령어 ]");
        player.sendMessage(ChatColor.GREEN + "/lobby " + ChatColor.DARK_GRAY + "(또는 /l) " + ChatColor.WHITE + "- 중앙 로비(본관 1층)로 즉시 이동");
        player.sendMessage(ChatColor.GREEN + "/stat " + ChatColor.DARK_GRAY + "(또는 /스탯) " + ChatColor.WHITE + "- 내 정보 및 스탯 포인트 투자 GUI 열기");
        player.sendMessage(ChatColor.GREEN + "/도전과제 " + ChatColor.DARK_GRAY + "(또는 /업적) " + ChatColor.WHITE + "- 내 도전과제(업적) 목록 GUI 열기");
        player.sendMessage(ChatColor.GREEN + "/인증 " + ChatColor.WHITE + "- 리로스쿨 계정 재인증 진행");
        
        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "💡 [ 게임 플레이 핵심 팁 ]");
        player.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + "스킬 발동: " + ChatColor.GRAY + "동아리 가입 후 " + ChatColor.GREEN + "Shift + F " + ChatColor.GRAY + "키를 눌러 고유 스킬(마나 소모)을 사용합니다!");
        player.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + "퀘스트 진행: " + ChatColor.GRAY + "머리 위에 이름이 떠 있는 NPC를 " + ChatColor.YELLOW + "우클릭" + ChatColor.GRAY + "하면 대화가 시작됩니다.");

        if (player.isOp()) {
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "👑 [ OP/관리자 전용 외부 플러그인 가이드 ]");
            player.sendMessage(ChatColor.GRAY + "[1] NPC 만들기 (Citizens)");
            player.sendMessage(ChatColor.WHITE + "  - /npc create [이름] " + ChatColor.GRAY + ": 바라보는 곳에 NPC를 스폰합니다.");
            player.sendMessage(ChatColor.WHITE + "  - /npc select " + ChatColor.GRAY + ": 이전에 만든 NPC를 선택합니다. (명령어 사용시 필수)");
            player.sendMessage(ChatColor.WHITE + "  - /npc skin [스킨닉네임] " + ChatColor.GRAY + ": NPC에게 예쁜 마크 스킨을 입힙니다.");
            player.sendMessage(ChatColor.WHITE + "  - /npc look " + ChatColor.GRAY + ": 플레이어가 다가가면 고개를 돌려 쳐다보게 합니다.");
            player.sendMessage("");
            player.sendMessage(ChatColor.GRAY + "[2] NPC에 대화/퀘스트 넣기 (BetonQuest)");
            player.sendMessage(ChatColor.WHITE + "  * BetonQuest 3.x 버전은 명령어 대신 " + ChatColor.YELLOW + "main.yml" + ChatColor.WHITE + " 파일을 직접 수정합니다.");
            player.sendMessage(ChatColor.WHITE + "  - 서버 폴더: plugins/BetonQuest/QuestPackages/default/main.yml 열기");
            player.sendMessage(ChatColor.WHITE + "  - 맨 아래에 다음 내용 추가: ");
            player.sendMessage(ChatColor.GRAY + "     npcs:");
            player.sendMessage(ChatColor.GRAY + "       '0': tutorial  (0은 NPC ID, tutorial은 대화파일 이름)");
            player.sendMessage(ChatColor.WHITE + "  - 파일을 저장하고 " + ChatColor.GREEN + "/bq reload" + ChatColor.WHITE + " 를 치면 즉시 적용됩니다!");
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "==================================================");

        return true;
    }
}
