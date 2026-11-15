package io.github.ryanpeng69.skgit;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

public class SkGitCmd {
    public static LiteralArgumentBuilder<CommandSourceStack> createCommandTree() {
        return Commands.literal("skgit")
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    sender.sendRichMessage("Sk<gold>Git</gold> <gray>"+SkGit.getInstance().getPluginMeta().getVersion()+"</gray> by <gold>RyanPeng69</gold>");
                    String description = SkGit.getInstance().getPluginMeta().getDescription();
                    if (description!=null) sender.sendRichMessage(description);
                    sender.sendRichMessage("&eCommands:");
                    sender.sendRichMessage("  <click:run_command:'skgit reload'><gold>/skgit reload <yellow>Reloads config and credentials");
                    sender.sendRichMessage("<click:open_url:'https://github.com/ryanpeng69/SkGit'><yellow>GitHub<reset>");
                    return 1;
                })
                .then(Commands.literal("reload").executes(context -> {
                    SkGit.loadConfig();
                    context.getSource().getSender().sendRichMessage("<green>Reloaded config and credentials");
                    return 1;
                }));
    }
}
