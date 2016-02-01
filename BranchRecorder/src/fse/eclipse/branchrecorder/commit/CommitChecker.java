package fse.eclipse.branchrecorder.commit;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.DepthWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class CommitChecker {

    // 変更可
    public static long threshold = 1200L;

    private static ObjectId chacheId;

    public static boolean isCommited(Repository repo, long time) {
        ObjectId newId = getNewCommitId(repo);
        if (!isNewCommitId(newId)) {
            return false;
        }
        chacheId = newId;

        // 最新のコミット時刻とリスナー呼び出し時刻が閾値以内ならば，commitと判定する
        RevWalk rWalk = new DepthWalk.RevWalk(repo, 0);
        try {
            RevCommit commit = rWalk.parseCommit(newId);

            return checkThreshold(commit, time);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static ObjectId getNewCommitId(Repository repo) {
        Git git = new Git(repo);
        try {
            return git.log().setMaxCount(1).call().iterator().next();
        } catch (NoHeadException | NoSuchElementException n) {
            return null;
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ObjectId getOneFrontCommitId(Repository repo) {
        Git git = new Git(repo);
        try {
            return git.log().setSkip(1).call().iterator().next();
        } catch (NoHeadException | NoSuchElementException n) {
            return null;
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isNewCommitId(ObjectId newId) {
        if (newId == null) {
            return false;
        } else if (newId != null && chacheId == null) {
            return true;
        } else {
            return !newId.name().equals(chacheId.name());
        }
    }

    private static boolean checkThreshold(RevCommit commit, long time) {
        long commitTime = formatCommitTime(commit.getCommitTime());
        long diff = time - commitTime;
        if (diff < 0) {
            return false;
        }

        return Long.compare(diff, threshold) <= 0;
    }

    /**
     * jgitの仕様で(?)コミットの時刻が10桁のint型のため、比較用に桁数を増やす
     * @param commitTime コミット時刻 <code>int</code>
     * @return フォーマット後のコミット時刻
     */
    private static long formatCommitTime(int commitTime) {
        return ((long) commitTime) * 1000;
    }
}
