package fse.eclipse.branchrecorder.commit;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jgit.events.RefsChangedEvent;
import org.eclipse.jgit.events.RefsChangedListener;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.jtool.changerecorder.history.OperationHistory;
import org.jtool.changerecorder.util.Time;

import fse.eclipse.branchrecorder.commit.history.XmlCommitWriter;
import fse.eclipse.branchrecorder.commit.operation.CommitOperation;

public class CommitListener implements RefsChangedListener {

    private static CommitListener instance;

    private CommitListener() {
    }

    public static CommitListener getInstance() {
        if (instance == null) {
            instance = new CommitListener();
        }
        return instance;
    }

    public void start() {
        Repository.getGlobalListenerList().addRefsChangedListener(this);
    }

    @Override
    public void onRefsChanged(RefsChangedEvent event) {
        Repository repository = event.getRepository();
        String path = repository.getDirectory().toString();
        // pluginディレクトリ下でのcommitは無視 (MergeHelper用)
        if (path.indexOf(".metadata") != -1) {
            return;
        }

        // 現在の時刻(リスナーが呼ばれた時刻)
        long currentEventTime = Time.getCurrentTime();

        if (CommitChecker.isCommited(repository, currentEventTime)) {
            // リスナー呼び出しがコミットによるものなら，xmlファイルを出力する
            String id = CommitChecker.getNewCommitId(repository).name();

            ObjectId pOId = CommitChecker.getOneFrontCommitId(repository);
            String parentId;
            if (pOId != null) {
                parentId = pOId.name();
            } else {
                parentId = "null";
            }

            try {
                generateCommitOperation(repository, currentEventTime, id, parentId);
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateCommitOperation(Repository repository, long time, String id, String parentId) throws CoreException {
        Map<String, String> codeMap = CodeMapManager.generateCodeMap(repository);
        if (codeMap == null || codeMap.size() == 0) {
            return;
        }

        OperationHistory commitHistory = new OperationHistory();
        for (Entry<String, String> entry : codeMap.entrySet()) {
            String path = entry.getKey();
            String code = codeMap.get(path);
            commitHistory.add(new CommitOperation(time, path, "", code, id, parentId));
        }

        try {
            XmlCommitWriter.writeCommitHistory(repository, commitHistory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
