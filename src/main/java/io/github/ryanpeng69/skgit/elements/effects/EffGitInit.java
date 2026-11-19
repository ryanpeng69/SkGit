package io.github.ryanpeng69.skgit.elements.effects;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Effect;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.bukkit.event.Event;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.jetbrains.annotations.Nullable;
import java.io.File;

public class EffGitInit extends Effect {
    static Repository lastRepository;

    public static void register(Registration reg) {
        reg.newEffect(EffGitInit.class,
                        "git init[ialize] [(to|at|in) (root [dir[ectory]]|[dir[ectory]] %-string%)]",
                        "initialize git [(to|at|in) (root [dir[ectory]]|[dir[ectory]] %-string%)]")
                .name("Git Init")
                .description("Creates an empty Git repository or reinitialize an existing one. Defaults to the server root directory if no path is specified.")
                .examples("""
                        git init
                        initialize git in "plugins/Skript/scripts"
                        git init at root
                        git init at {_someVariable} # If the variable is not set, it will also be initialized at root.""")
                .since("1.0.0")
                .register();
    }

    private Expression<String> targetDirectory;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.targetDirectory = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected void execute(Event event) {
        String dirString = ".";
        if (targetDirectory != null) dirString = targetDirectory.getSingle(event);
        if (dirString == null || dirString.isEmpty()) dirString = ".";

        File repoDir = new File(dirString);
        if (!repoDir.isDirectory()) repoDir = repoDir.getParentFile();
        try (Git git = Git.init().setDirectory(repoDir.getCanonicalFile()).call()) {
            lastRepository = git.getRepository();
            SkGit.logAction("Successfully initialized git repository at: "+
                    git.getRepository().getDirectory().getAbsolutePath());
        } catch (Exception e) {
            SkGit.throwError("Failed to initialize git repository at "+repoDir.getAbsolutePath()+"\nCause: "+e.getMessage());
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        if (this.targetDirectory != null) {
            return "git init in directory "+this.targetDirectory.toString(event, debug);
        }
        return "git init";
    }

    public static Repository getLastRepository() {
        return lastRepository;
    }
}
