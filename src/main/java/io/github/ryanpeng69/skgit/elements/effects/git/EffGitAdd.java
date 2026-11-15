package io.github.ryanpeng69.skgit.elements.effects.git;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.bukkit.event.Event;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;

/** @see EffGitRemove */
public class EffGitAdd extends Effect {
    public static void register(Registration reg) {
        reg.newEffect(EffGitAdd.class,
                        "git add (%-strings%|:all) (to|in) %git%",
                        "add (%-strings%|:all) (to|in) git %git%",
                        "%git% add (%-strings%|:all)")
                .name("Git - Add")
                .description("""
                        Stages files for the next commit.
                        Ignored files are not added.
                        **NOTE**: This syntax will not throw any error if the file was already tracked by git.
                        Use `{_git} add all` or `{_git} add "."` to stage all modified and untracked files (except ignored ones).""")
                .examples("""
                        {_git} add "plugins/Skript/scripts" and "server.properties"
                        {_git} add all""")
                .since("1.0")
                .register();
    }

    private Expression<Git> git;
    private Expression<String> files;
    private boolean isAll;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.isAll = parseResult.hasTag("all");
        if (matchedPattern == 2) {
            this.git = (Expression<Git>) expressions[0];
            this.files = expressions.length > 1 ? (Expression<String>) expressions[1] : null;
        } else {
            this.files = expressions.length > 1 ? (Expression<String>) expressions[0] : null;
            this.git = (Expression<Git>) expressions[1];
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        Git git = this.git.getSingle(event);
        if (git == null) return;
        AddCommand add = git.add();
        if (isAll) {
            add.addFilepattern(".");
            SkGit.logAction("Adding all files to "+git);
        } else {
            for (String file : this.files.getArray(event)) {
                File fileObj = new File(file);
                if (!fileObj.exists()) {
                    SkGit.getInstance().getLogger().warning("File does not exist: "+file);
                    continue;
                }
                File workTree = git.getRepository().getWorkTree();
                String relativePath = workTree.toURI().relativize(fileObj.toURI()).getPath();

                add.addFilepattern(relativePath);
                SkGit.logAction("Adding '"+relativePath+"' to "+git);
            }
        }
        try {
            add.call();
        } catch (GitAPIException e) {
            SkGit.throwError(e);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        if (isAll) return "git add all to "+this.git.getSingle(event);
        return "git add "+ Arrays.toString(this.files.getArray(event)) +" to "+this.git.getSingle(event);
    }

}
