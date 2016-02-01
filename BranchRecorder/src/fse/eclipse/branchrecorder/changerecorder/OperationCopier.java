package fse.eclipse.branchrecorder.changerecorder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.jtool.changerecorder.history.OperationHistory;
import org.jtool.changerecorder.history.Xml2Operation;
import org.jtool.changerecorder.operation.IOperation;
import org.jtool.changerecorder.util.XmlFileStream;
import org.w3c.dom.Document;

import fse.eclipse.branchrecorder.util.PathUtil;

public class OperationCopier {
    private final String parentDirPath;

    /**
     * コンストラクタ(親ディレクトリ)
     * @param parentDirPath
     */
    OperationCopier(String parentDirPath) {
        this.parentDirPath = parentDirPath;
    }

    /**
     * Historyファイルから，そのプロジェクトがリポジトリかどうか リポジトリならHistoryファイルをコピーする
     * @param xmlFile Historyファイル
     * @throws InterruptedException
     */
    public void copy(Path xmlFile) throws InterruptedException {
        String filePath = validateXmlFilePath(xmlFile);
        List<IOperation> operations = readOperations(filePath);
        String projName = readProjectName(operations);

        ProjectChache pChache = ProjectChache.getInstance();
        if (shouldCopy(pChache, projName)) {
            String branchName = pChache.getBranchName();
            copyHistoryFile(filePath, pChache.getProject(), branchName);
            // プロジェクトをrefreshして，コピーしたファイルを即時反映する
            pChache.refreshIProject();
        }
    }

    private boolean shouldCopy(ProjectChache pChache, String projectName) {
        if (projectName.length() == 0) {
            return false;
        }

        if (pChache == null || !pChache.equals(projectName)) {
            pChache.init(projectName);
        }
        return pChache.isGitProject();
    }

    private String validateXmlFilePath(Path xmlFile) {
        String fileName = xmlFile.toString();

        StringBuilder sb = new StringBuilder();
        sb.append(parentDirPath).append(File.separator).append(fileName);
        return sb.toString();
    }

    /**
     * Historyファイルから全Operationを取り出す
     * @param filePath Historyファイル
     * @return operations 全Operation
     * @throws InterruptedException
     */
    private List<IOperation> readOperations(String filePath) throws InterruptedException {
        // ファイルが作成された瞬間に飛んでくるため(?)，読み込みに失敗することがある(NullPointerException)
        try {
            Document doc = XmlFileStream.read(filePath);
            OperationHistory history = Xml2Operation.convert(doc);
            return history.getOperations();
        } catch (NullPointerException n) {
            // 読み込みに失敗した際，一瞬間をおき再度処理
            Thread.sleep(100);
            return readOperations(filePath);
        }
    }

    /**
     * Operationからプロジェクト名を抜き取る
     * @param operations
     * @return プロジェクト名
     */
    private String readProjectName(List<IOperation> operations) {
        for (IOperation operation : operations) {
            String projPath = operation.getFilePath();
            if (!isNullPath(projPath)) {
                return cutoutProjectName(projPath);
            }
        }
        return "";
    }

    /**
     * Operationから読み込んだファイルパスが使用不可能なパスかどうか
     * @param projPath ファイルパス
     * @return 結果
     */
    private boolean isNullPath(String projPath) {
        return projPath == null || projPath.length() == 0 || projPath.equals("null");
    }

    /**
     * Operationのファイルパスからプロジェクト名を抜き取る
     * @param projPath Operationのファイルパス
     * @return プロジェクト名
     */
    private String cutoutProjectName(String projPath) {
        int firstIndexOf = projPath.indexOf("/") + 1;
        int secondIndexOf = projPath.indexOf("/", firstIndexOf);
        return projPath.substring(firstIndexOf, secondIndexOf);
    }

    /**
     * Historyファイルを プロジェクトパス/#history/ブランチ名 へコピーする
     * @param filePath コピー元Historyファイル
     * @param projName プロジェクト名
     * @param branchName ブランチ名
     */
    private void copyHistoryFile(String filePath, IProject project, String branchName) {
        File srcFile = new File(filePath);
        File destDir = new File(PathUtil.getProjectBranchPath(project, branchName));
        if (!destDir.exists()) {
            try {
                FileUtils.forceMkdir(destDir.getParentFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileUtils.copyFileToDirectory(srcFile, destDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
