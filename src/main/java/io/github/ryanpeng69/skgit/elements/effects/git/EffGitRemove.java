package io.github.ryanpeng69.skgit.elements.effects.git;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.bukkit.event.Event;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;

/** @see EffGitAdd */
public class EffGitRemove extends Effect {
    public static void register(Registration reg) {
        reg.newEffect(EffGitRemove.class,
                        "git (remove|rm) [:and delete] (%-strings%|all:all [files]) (from|in) %git%",
                        "(remove|rm) [:and delete] (%-strings%|all:all [files]) (from|in) git %git%",
                        "%git% (remove|rm) [:and delete] (%-strings%|all:all [files])")
                .name("Git - Remove")
                .description("""
                        Removes files from Git tracking so they are no longer managed by the repository.
                        Without `and delete`, the files stay on disk but are no longer tracked.
                        With `and delete`, the files are removed from both Git tracking and the filesystem.
                        `{_git} remove all` removes all tracked files.""")
                .examples("""
                        {_git} remove "plugins/Skript/scripts" and "server.properties"
                        {_git} remove and delete "ops.json"
                        {_git} remove all""")
                .since("1.0.0")
                .register();
    }

    private Expression<Git> git;
    private Expression<String> files;
    private boolean isAll;
    private boolean shouldDelete;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.isAll = parseResult.hasTag("all");
        this.shouldDelete = parseResult.hasTag("and delete");
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
        RmCommand rm = git.rm();
        if (isAll) {
            rm.addFilepattern(".");
            SkGit.logAction("Removing"+(shouldDelete ? " and deleting" : "")+" all files from "+git);
        } else {
            for (String file : this.files.getArray(event)) {
                File fileObj = new File(file);
                File workTree = git.getRepository().getWorkTree();
                String relativePath = workTree.toURI().relativize(fileObj.toURI()).getPath();

                rm.addFilepattern(relativePath);
                SkGit.logAction("Removing"+(shouldDelete ? " and deleting" : "")+" '"+relativePath+"' from "+git);
            }
        }
        try {
            rm.setCached(!shouldDelete).call();
        } catch (GitAPIException e) {
            SkGit.throwError(e);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        if (shouldDelete) {
            if (isAll) return "git remove and delete all from "+this.git.getSingle(event);
            return "git remove and delete "+ Arrays.toString(this.files.getArray(event)) +" from "+this.git.getSingle(event);
        }
        if (isAll) return "git remove all files from "+this.git.getSingle(event);
        return "git remove "+ Arrays.toString(this.files.getArray(event)) +" from "+this.git.getSingle(event);
    }

}
