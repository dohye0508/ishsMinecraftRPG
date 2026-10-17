package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.RpgListener;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EntryGateCommand implements CommandExecutor {
    private final RpgListener rpgListener;

    public EntryGateCommand(RpgListener rpgListener) {
        this.rpgListener = rpgListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            Messages.send(sender, "common.no-permission");
            return true;
        }

        if (args.length == 0) {
            Messages.send(sender, "entry.status",
                    "state", rpgListener.isEntryOpen() ? Messages.get("entry.state-open") : Messages.get("entry.state-closed"));
            Messages.send(sender, "entry.usage");
            return true;
        }

        if (args[0].equalsIgnoreCase(Messages.get("entry.keyword-open"))) {
            rpgListener.setEntryOpen(true);
            Messages.send(sender, "entry.opened");
        } else if (args[0].equalsIgnoreCase(Messages.get("entry.keyword-close"))) {
            rpgListener.setEntryOpen(false);
            Messages.send(sender, "entry.closed");
        } else {
            Messages.send(sender, "entry.usage-error");
        }
        return true;
    }
}
