package io.github.ryanpeng69.skgit.elements.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import io.github.ryanpeng69.skgit.SkGitCredentialsProvider;
import org.bukkit.event.Event;
import org.eclipse.jgit.api.Git;

import java.io.File;

public class EffGitClone extends Effect {
    public static void register(Registration reg) {
        reg.newEffect(EffGitClone.class,
                        "git clone %string% [(to|at|in) (root [dir[ectory]]|[dir[ectory]] %-string%)]")
                .name("Git Init")
                .description("""
                        Clones a remote git repository to a directory.
                        The directory should be empty or new.
                        Defaults to the server root directory if no path is specified.""")
                .examples("""
                        git clone "https://github.com/ryanpeng69/SkGit.git"
                        git clone "https://github.com/ryanpeng69/SkGit.git" to "../SkGit/"
                        git clone "https://github.com/ryanpeng69/SkGit.git" to root""")
                .since("1.0.1")
                .register();
    }

    private Expression<String> gitURI;
    private Expression<String> targetDirectory;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.gitURI = (Expression<String>) exprs[0];
        this.targetDirectory = (Expression<String>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        String uri = gitURI.getSingle(event);
        if (uri == null || uri.isEmpty()) return;
        String dirString = ".";
        if (targetDirectory != null) dirString = targetDirectory.getSingle(event);
        if (dirString == null || dirString.isEmpty()) dirString = ".";
        File repoDir = new File(dirString);

        try (Git git = Git.cloneRepository()
                .setURI(uri)
                .setDirectory(repoDir.getCanonicalFile())
                .setCredentialsProvider(SkGitCredentialsProvider.getProvider())
                .call()) {
            EffGitInit.lastRepository = git.getRepository();
            SkGit.logAction("Successfully cloned "+gitURI+" to \""+
                    git.getRepository().getDirectory().getAbsolutePath());
        } catch (Exception e) {
            SkGit.throwError("Failed to clone "+gitURI+" to \""+
                    repoDir.getAbsolutePath()+"\"\nCause: "+e.getMessage());
        }
    }

    @Override
    public String toString(Event event, boolean debug) {
        if (this.targetDirectory != null) {
            return "git clone in directory "+this.targetDirectory.toString(event, debug);
        }
        return "git clone";
    }
}
