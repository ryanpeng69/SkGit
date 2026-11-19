package io.github.ryanpeng69.skgit.elements.expressions.git;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import io.github.ryanpeng69.skgit.elements.expressions.ExprTypeGitRemote;
import org.bukkit.event.Event;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExprGitRemotes extends PropertyExpression<Git, ExprTypeGitRemote.TypeGitRemote> {
    public static void register(Registration reg) {
        reg.newPropertyExpression(ExprGitRemotes.class, ExprTypeGitRemote.TypeGitRemote.class, "[git] remote[s]", "git")
                .name("Git - Remotes")
                .description("Gets or modifies the remotes of a Git repository. A repository can have multiple remotes.")
                .examples("""
                        set remote of {_git} to git remote named "origin" with uri "https://github.com/username/repo1name.git"
                        add git remote named "origin" with uri "https://github.com/username/repo2name.git" to remotes of {_git}
                        set {_remotes::*} to remotes of {_git}""")
                .since("1.0.0")
                .register();
    }

    @Override
    protected ExprTypeGitRemote.TypeGitRemote[] get(Event event, Git[] source) {
        ArrayList<ExprTypeGitRemote.TypeGitRemote> rst = new ArrayList<>();
        for (Git git : source) {
            try {
                git.remoteList().call().forEach(remote ->
                        rst.add(new ExprTypeGitRemote.TypeGitRemote(remote.getName(), remote.getURIs().getFirst())));
            } catch (Exception e) {
                SkGit.throwError(e);
            }
        }
        return rst.toArray(new ExprTypeGitRemote.TypeGitRemote[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends ExprTypeGitRemote.TypeGitRemote> getReturnType() {
        return ExprTypeGitRemote.TypeGitRemote.class;
    }

    @Override
    public Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, REMOVE_ALL -> CollectionUtils.array(ExprTypeGitRemote.TypeGitRemote.class);
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object[] delta, Changer.ChangeMode mode) {
        Git git = getExpr().getSingle(event);
        if (git == null) return;
        ExprTypeGitRemote.TypeGitRemote[] remoteDelta = new ExprTypeGitRemote.TypeGitRemote[delta != null ? delta.length : 0];
        if (delta != null && delta.length != 0) {
            for (int i = 0; i < delta.length; i++) {
                if (delta[i] instanceof ExprTypeGitRemote.TypeGitRemote)
                    remoteDelta[i] = (ExprTypeGitRemote.TypeGitRemote) delta[i];
                else {
                    remoteDelta[i] = null;
                }
            }
        }
        try {
            switch (mode) {
                case SET -> {
                    List<RemoteConfig> remoteList = git.remoteList().call();
                    Map<String, URIish> remoteWantMap = new HashMap<>();
                    for (ExprTypeGitRemote.TypeGitRemote remote : remoteDelta) {
                        remoteWantMap.put(remote.name(), remote.uri());
                    }
                    for (RemoteConfig remote : remoteList) {
                        if (!remoteWantMap.containsKey(remote.getName())) {
                            removeRemoteByName(git, remote.getName());
                        } else {
                            remoteWantMap.remove(remote.getName());
                        }
                    }
                    for (Map.Entry<String, URIish> entry : remoteWantMap.entrySet()) {
                        addRemote(git, new ExprTypeGitRemote.TypeGitRemote(entry.getKey(), entry.getValue()));
                    }
                }
                case ADD -> {
                    List<String> remoteList = git.remoteList().call().stream().map(RemoteConfig::getName).toList();
                    for (ExprTypeGitRemote.TypeGitRemote remote : remoteDelta) {
                        if (!remoteList.contains(remote.name())) addRemote(git, remote);
                    }
                }
                case REMOVE -> {
                    List<String> remoteList = git.remoteList().call().stream().map(RemoteConfig::getName).toList();
                    for (ExprTypeGitRemote.TypeGitRemote remote : remoteDelta) {
                        if (remoteList.contains(remote.name())) removeRemote(git, remote);
                    }
                }
                case REMOVE_ALL -> {
                    for (RemoteConfig remote : git.remoteList().call()) {
                        removeRemoteByName(git, remote.getName());
                    }
                }
            }
        } catch (Exception e) {
            SkGit.throwError(e);
        }
    }


    private void addRemote(Git git, ExprTypeGitRemote.TypeGitRemote remote) throws GitAPIException {
        git.remoteAdd().setName(remote.name()).setUri(remote.uri()).call();
        SkGit.logAction("Remote added: " + remote);
    }
    private void removeRemote(Git git, ExprTypeGitRemote.TypeGitRemote remote) throws GitAPIException {
        git.remoteRemove().setRemoteName(remote.name()).call();
        SkGit.logAction("Remote removed: " + remote);
    }
    private void removeRemoteByName(Git git, String remote) throws GitAPIException {
        git.remoteRemove().setRemoteName(remote).call();
        SkGit.logAction("Remote removed: " + remote);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends Git>) expressions[0]);
        return true;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "git remotes";
    }
}
