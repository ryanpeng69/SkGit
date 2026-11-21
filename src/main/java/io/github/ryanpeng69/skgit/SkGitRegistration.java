package io.github.ryanpeng69.skgit;

import com.github.shanebeee.skr.Registration;

import io.github.ryanpeng69.skgit.elements.conditions.*;
import io.github.ryanpeng69.skgit.elements.effects.*;
import io.github.ryanpeng69.skgit.elements.effects.git.*;
import io.github.ryanpeng69.skgit.elements.expressions.*;
import io.github.ryanpeng69.skgit.elements.expressions.git.*;
import io.github.ryanpeng69.skgit.elements.expressions.git.status.*;
import io.github.ryanpeng69.skgit.elements.expressions.gitcommit.*;
import io.github.ryanpeng69.skgit.elements.expressions.personident.*;
import io.github.ryanpeng69.skgit.elements.expressions.repository.*;
import io.github.ryanpeng69.skgit.elements.Types;

public class SkGitRegistration {
    public static void register(Registration registration) {
        // Conditions
        CondGitIsClean.register(registration);

        // Effects
            // git
                EffGitAdd.register(registration);
                EffGitCheckout.register(registration);
                EffGitCommit.register(registration);
                EffGitFetch.register(registration);
                EffGitPull.register(registration);
                EffGitPush.register(registration);
                EffGitRemove.register(registration);
                EffGitReset.register(registration);
            EffGitClone.register(registration);
            EffGitInit.register(registration);

        // Expressions
            // git
                // status
                    ExprGitStatusAdded.register(registration);
                    ExprGitStatusChanged.register(registration);
                    ExprGitStatusConflicting.register(registration);
                    ExprGitStatusIgnored.register(registration);
                    ExprGitStatusMissing.register(registration);
                    ExprGitStatusRemoved.register(registration);
                    ExprGitStatusUntracked.register(registration);
                ExprGitCommits.register(registration);
                ExprGitRemotes.register(registration);
                ExprGitBranches.register(registration);
                ExprGitRepo.register(registration);
            // gitcommit
                ExprGitCommitAuthor.register(registration);
                ExprGitCommitMessage.register(registration);
                ExprGitCommitTime.register(registration);
            // personident
                ExprPersonIdentEmail.register(registration);
                ExprPersonIdentTimeZoneOffset.register(registration);
            // repository
                ExprRepoGitInstance.register(registration);
                ExprRepoHead.register(registration);
            ExprGitRepositoryAt.register(registration);
            ExprLastInitializedGitRepository.register(registration);
            ExprTypeGitRemote.register(registration);

        // Final
        Types.register(registration);
        registration.finalizeRegistration();
    }
}