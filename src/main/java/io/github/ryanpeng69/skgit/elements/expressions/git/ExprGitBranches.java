package io.github.ryanpeng69.skgit.elements.expressions.git;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.bukkit.event.Event;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ExprGitBranches extends PropertyExpression<Git, String> {
    public static void register(Registration reg) {
        reg.newPropertyExpression(ExprGitBranches.class, String.class, "[git] branch[es]", "git")
                .name("Git - Branches")
                .description("Gets or modifies the branches of a Git repository. A repository can have multiple branches.")
                .examples("""
                        set {_branches::*} to branches of {_git}
                        add "feature" to branches of {_git}
                        remove "old-branch" from branches of {_git}""")
                .since("1.0.0")
                .register();
    }

    @Override
    protected String[] get(Event event, Git[] source) {
        ArrayList<String> rst = new ArrayList<>();
        for (Git git : source) {
            try {
                git.branchList().call().forEach(ref ->
                        rst.add(ref.getName().replace("refs/heads/", "")));
            } catch (Exception e) {
                SkGit.throwError(e);
            }
        }
        return rst.toArray(new String[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, REMOVE_ALL -> CollectionUtils.array(String.class);
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object[] delta, Changer.ChangeMode mode) {
        Git git = getExpr().getSingle(event);
        if (git == null) return;
        String[] branchDelta = new String[delta != null ? delta.length : 0];
        if (delta != null && delta.length != 0) {
            for (int i = 0; i < delta.length; i++) {
                if (delta[i] instanceof String)
                    branchDelta[i] = (String) delta[i];
                else {
                    branchDelta[i] = null;
                }
            }
        }
        try {
            switch (mode) {
                case SET -> {
                    List<String> branchList = git.branchList().call().stream()
                            .map(ref -> ref.getName().replace("refs/heads/", "")).toList();
                    List<String> branchWantList = new ArrayList<>();
                    for (String branch : branchDelta) {
                        if (branch != null) branchWantList.add(branch);
                    }
                    for (String branch : branchList) {
                        if (!branchWantList.contains(branch)) {
                            deleteBranch(git, branch);
                        } else {
                            branchWantList.remove(branch);
                        }
                    }
                    for (String branch : branchWantList) {
                        createBranch(git, branch);
                    }
                }
                case ADD -> {
                    List<String> branchList = git.branchList().call().stream()
                            .map(ref -> ref.getName().replace("refs/heads/", "")).toList();
                    for (String branch : branchDelta) {
                        if (branch != null && !branchList.contains(branch)) createBranch(git, branch);
                    }
                }
                case REMOVE -> {
                    List<String> branchList = git.branchList().call().stream()
                            .map(ref -> ref.getName().replace("refs/heads/", "")).toList();
                    for (String branch : branchDelta) {
                        if (branch != null && branchList.contains(branch)) deleteBranch(git, branch);
                    }
                }
                case REMOVE_ALL -> {
                    for (Ref ref : git.branchList().call()) {
                        String branchName = ref.getName().replace("refs/heads/", "");
                        deleteBranch(git, branchName);
                    }
                }
            }
        } catch (Exception e) {
            SkGit.throwError(e);
        }
    }

    private void createBranch(Git git, String branch) throws GitAPIException {
        git.branchCreate().setName(branch).call();
        SkGit.logAction("Branch created: " + branch);
    }

    private void deleteBranch(Git git, String branch) throws GitAPIException {
        git.branchDelete().setForce(true).setBranchNames(branch).call();
        SkGit.logAction("Branch deleted: " + branch);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends Git>) expressions[0]);
        return true;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "git branches";
    }
}
