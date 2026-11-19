package io.github.ryanpeng69.skgit.elements;

import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.elements.expressions.ExprTypeGitRemote;
import io.github.ryanpeng69.skgit.elements.propertyhandler.PropHandlerPersonIdentName;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.skriptlang.skript.lang.properties.Property;

public class Types {
    public static void register(Registration reg) {
        reg.newType(Repository.class, "gitrepository")
                .user("git ?repo(?:sitory)?")
                .name("Git Repository")
                .description("A Git repository holds all objects and refs used for managing source code history.")
                .examples("git repo at root")
                .since("1.0.0")
                .parser(new Parser<>() {
                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }
                    @Override
                    public String toString(Repository o, int flags) {
                        return "git repository at \""+o.getDirectory().getPath()+"\"";
                    }
                    @Override
                    public String toVariableNameString(Repository o) {
                        return "gitrepository_"+o.getDirectory().getPath();
                    }
                }).register();
        reg.newType(Git.class, "git")
                .user("git")
                .name("Git")
                .description("Represents a high-level Git command facade for executing operations like add, commit, status, and push.")
                .examples("git of {_repo}")
                .since("1.0.0")
                .parser(new Parser<>() {
                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }
                    @Override
                    public String toString(Git o, int flags) {
                        return "git of repository at \""+o.getRepository().getDirectory().getPath()+"\"";
                    }
                    @Override
                    public String toVariableNameString(Git o) {
                        return "git_"+o.getRepository().getDirectory().getPath();
                    }
                }).register();
        reg.newType(ExprTypeGitRemote.TypeGitRemote.class, "gitremote")
                .user("git ?remote")
                .name("Git Remote")
                .description("A type holding the name and URI of a Git remote.")
                .examples("remotes of {_git}")
                .since("1.0.0")
                .parser(new Parser<>() {
                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }
                    @Override
                    public String toString(ExprTypeGitRemote.TypeGitRemote o, int flags) {
                        return "git remote \""+o.name()+"\" with uri \""+o.uri()+"\"";
                    }
                    @Override
                    public String toVariableNameString(ExprTypeGitRemote.TypeGitRemote o) {
                        return "gitremote_"+o.name();
                    }
                }).register();
        reg.newType(RevCommit.class, "gitcommit")
                .user("git ?commit")
                .name("Git Commit")
                .description("Represents a Git commit with its metadata (author, message, hash, parents).")
                .examples("commits of {_commit}")
                .since("1.0.0")
                .parser(new Parser<>() {
                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }
                    @Override
                    public String toString(RevCommit o, int flags) {
                        return "git commit "+o.getId().getName()+" with message \"" + o.getShortMessage()+"\"";
                    }
                    @Override
                    public String toVariableNameString(RevCommit o) {
                        return "gitcommit_" + o.getId().getName();
                    }
                }).register();
        reg.newType(PersonIdent.class, "personident")
                .user("person ?ident(?:ifier)?|commit ?author|commit ?creator|commiter")
                .name("Person Ident")
                .description("Represents a Git user that created the commit. Holds the name, email, and time.")
                .examples("""
                        set {_author} to author of {_commit}
                        send name of {_author}
                        send email of {_author}""")
                .since("1.0.0")
                .parser(new Parser<>() {
                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }
                    @Override
                    public String toString(PersonIdent o, int flags) {
                        return "git user \""+o.getName()+"\" ("+o.getEmailAddress()+")";
                    }
                    @Override
                    public String toVariableNameString(PersonIdent o) {
                        return "personident_"+o.getEmailAddress();
                    }
                })
                .property(Property.NAME, "The name of a person.", new PropHandlerPersonIdentName())
                .register();
    }
}
