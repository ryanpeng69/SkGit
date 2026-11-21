package io.github.ryanpeng69.skgit.elements.effects.git;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.bukkit.event.Event;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class EffGitCheckout extends Effect {
    public static void register(Registration reg) {
        reg.newEffect(EffGitCheckout.class,
                        "[git] checkout to [[the] branch] %string% on %git%",
                        "%git% checkout to [[the] branch] %string%",
                        "[git] checkout to [[the] branch] %string% and create [it] on %git%",
                        "%git% checkout -b %string%",
                        "[git] checkout and create [[the|a new] branch] %string% on %git%",
                        "%git% checkout and create [[the|a new] branch] %string%",
                        "[git] checkout [restore] [the] file[s] %strings% on %git%",
                        "%git% checkout [restore] [the] file[s] %strings%")
                .name("Git - Checkout")
                .description("""
                        Switch branches or restore working tree files.
                        `checkout to branch`: Switch to an existing branch.
                        `checkout and create branch` or `-b`: Create a new branch and switch to it.
                        `checkout files`: Restore files from HEAD to the working tree, discarding local changes.""")
                .examples("""
                        {_git} checkout to branch "main"
                        {_git} checkout -b "feature"
                        {_git} checkout files "README.md" and "plugins/Skript/config.sk\"""")
                .since("1.0.1")
                .register();
    }

    private Expression<Git> git;
    private Expression<String> name; // branch name or file paths
    private int mode; // 0 = switch branch, 1 = create branch, 2 = checkout file

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        switch (matchedPattern) {
            case 0 -> {
                this.name = (Expression<String>) expressions[0];
                this.git = (Expression<Git>) expressions[1];
                this.mode = 0;
            }
            case 1 -> {
                this.git = (Expression<Git>) expressions[0];
                this.name = (Expression<String>) expressions[1];
                this.mode = 0;
            }
            case 2, 4 -> {
                this.git = (Expression<Git>) expressions[1];
                this.name = (Expression<String>) expressions[0];
                this.mode = 1;
            }
            case 3, 5 -> {
                this.git = (Expression<Git>) expressions[0];
                this.name = (Expression<String>) expressions[1];
                this.mode = 1;
            }
            case 6 -> {
                this.git = (Expression<Git>) expressions[1];
                this.name = (Expression<String>) expressions[0];
                this.mode = 2;
            }
            case 7 -> {
                this.git = (Expression<Git>) expressions[0];
                this.name = (Expression<String>) expressions[1];
                this.mode = 2;
            }
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        Git git = this.git.getSingle(event);
        if (git == null) return;
        String branchName = "";
        List<String> files = null;
        if (this.name.isSingle()) {
            branchName = this.name.getSingle(event);
            if (branchName == null || branchName.isEmpty()) {
                SkGit.throwError("Please set the branch name.");
                return;
            }
        } else {
            files = Arrays.stream(this.name.getArray(event)).map(input->input.replace('\\', '/')).toList();
        }
        try {
            CheckoutCommand cmd = git.checkout();
            if (this.mode == 0) {
                cmd.setName(branchName).call();
                SkGit.logAction("Checked out branch '" + branchName + "' on " + git);
            } else if (this.mode == 1) {
                cmd.setCreateBranch(true).setName(branchName).call();
                SkGit.logAction("Created and checked out new branch '" + branchName + "' on " + git);
            } else {
                cmd.addPaths(files).call();
                SkGit.logAction("Restored file '" + files + "' from HEAD on " + git);
            }
        } catch (Exception e) {
            SkGit.throwError(e);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        StringBuilder sb = new StringBuilder("git checkout ");
        if (this.mode == 0) {
            sb.append("to branch ").append(this.name.getSingle(event));
        } else if (this.mode == 1) {
            sb.append("create branch ").append(this.name.getSingle(event));
        } else {
            sb.append("restore files ").append(Arrays.toString(this.name.getArray(event)));
        }
        sb.append(" on ").append(this.git.getSingle(event));
        return sb.toString();
    }
}
