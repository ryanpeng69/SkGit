package io.github.ryanpeng69.skgit;

import com.github.shanebeee.skr.JsonDocGenerator;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.ryanpeng69.skgit.features.Updater;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

public class SkGitCmd {
    public static LiteralArgumentBuilder<CommandSourceStack> createCommandTree() {
        return Commands.literal("skgit")
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    sender.sendRichMessage("<red>Sk</red><gold>Git</gold> <gray>" + SkGit.getInstance().getPluginMeta().getVersion() +
                            "</gray> by <click:open_url:'https://github.com/ryanpeng69'><gold>RyanPeng69</gold>");
                    String description = SkGit.getInstance().getPluginMeta().getDescription();
                    if (description != null) sender.sendRichMessage(description);
                    if (sender.hasPermission("op")) {
                        sender.sendRichMessage("<gray><st>                                                                ");
                        sender.sendRichMessage("  <click:run_command:'skgit reload'><gold>/skgit reload <yellow>Reloads config and credentials");
                        sender.sendRichMessage("  <click:run_command:'skgit gendocs'><gold>/skgit gendocs <yellow>Generates syntax documentation");
                        sender.sendRichMessage("  <click:run_command:'skgit update'><gold>/skgit update <yellow>Check for new updates");
                    }
                    sender.sendRichMessage("<gray><st>                                                                ");
                    sender.sendRichMessage("  <click:open_url:'https://github.com/ryanpeng69/SkGit'><yellow>GitHub<reset>" +
                            " <gray>| </gray>" +
                            "<click:open_url:'https://modrinth.com/plugin/SkGit'><yellow>Modrinth<reset>" +
                            " <gray>| </gray>" +
                            "<click:open_url:'https://skdocs.org/docs?addon=SkGit'><yellow>SkDocs<reset>" +
                            " <gray>| </gray>" +
                            "<click:open_url:'https://https://skripthub.net/docs/?addon=SkGit'><yellow>SkriptHub<reset>"
                    );
                    sender.sendRichMessage("<gray><st>                                                                ");
                    return 1;
                })
                .then(Commands.literal("reload")
                        .requires(source -> source.getSender().hasPermission("op"))
                        .executes(context -> {
                            SkGit.loadConfig();
                            context.getSource().getSender().sendRichMessage("<green>Reloaded config and credentials");
                            return 1;
                        }))
                .then(Commands.literal("gendocs")
                        .requires(source -> source.getSender().hasPermission("op"))
                        .executes(context -> {
                            new JsonDocGenerator(SkGit.getInstance(), SkGit.getInstance().registration).generateDocs();
                            context.getSource().getSender().sendRichMessage("<Green>Docs generated, find it in plugins/SkGit/");
                            return 1;
                        }))
                .then(Commands.literal("update")
                        .requires(source -> source.getSender().hasPermission("op"))
                        .executes(context -> {
                            Updater.checkUpdate().thenAccept(update -> {
                                String msg;
                                if (update == null) {
                                    msg = "<red>Error occurred while checking update";
                                } else if (update.equals("latest")) {
                                    msg = "<green>Up to date";
                                } else {
                                    msg = "<green>New release: "+update;
                                }
                                context.getSource().getSender().sendRichMessage(msg);
                            });
                           return 1;
                        }));
    }
}
