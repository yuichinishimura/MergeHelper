package fse.eclipse.mergehelper.detecter;

import java.util.ArrayList;
import java.util.List;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerepository.data.RepositoryManager;
import org.jtool.changerepository.data.WorkspaceInfo;
import org.jtool.changerepository.dependencygraph.FileOpDepGraph;
import org.jtool.changerepository.dependencygraph.OpDepGraph;
import org.jtool.changerepository.dependencygraph.OpDepGraphInfo;
import org.jtool.changerepository.dependencygraph.OpDepGraphNode;
import org.jtool.changerepository.parser.CodeRange;
import org.jtool.changerepository.parser.OpJavaElement;
import org.jtool.changeslicereplayer.slicer.CodeSnippet;
import org.jtool.changeslicereplayer.slicer.OpGraphSlicer;
import org.jtool.changeslicereplayer.slicer.Slice;
import org.jtool.changeslicereplayer.slicer.SliceCriterion;

import fse.eclipse.mergehelper.Activator;
import fse.eclipse.mergehelper.element.BranchInfo;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.ElementSlice;
import fse.eclipse.mergehelper.element.ElementSliceCriterion;
import fse.eclipse.mergehelper.element.MergeType;
import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;
import fse.eclipse.mergehelper.util.Parser;
import fse.eclipse.mergehelper.util.RepositoryElementInfoUtil;

public class SliceManager extends AbstractDetector {

    private static final String MESSAGE = "Create Operation History Slice ...";
    private static AbstractDetector instance = new SliceManager();

    private SliceManager() {
    }

    public static AbstractDetector getInstance() {
        return instance;
    }

    @Override
    public void execute(ConflictDetectingDialog dialog) {
        RepositoryManager rManager = RepositoryManager.getInstance();
        rManager.collectOperationsInRepository(Activator.getWorkingDirPath());
        WorkspaceInfo wInfo = rManager.getWorkspaceInfo();

        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        BranchInfo srcInfo = rootInfo.getBranchInfo(MergeType.SRC);
        BranchInfo destInfo = rootInfo.getBranchInfo(MergeType.DEST);

        List<ProjectInfo> pInfos = wInfo.getAllProjectInfo();
        for (ProjectInfo pInfo : pInfos) {
            String name = RepositoryElementInfoUtil.getBranchName(pInfo);

            if (name.equals(srcInfo.getName())) {
                srcInfo.setProjectInfo(pInfo);
                addBranchFileInfo(srcInfo, pInfo);
            } else if (name.equals(destInfo.getName())) {
                destInfo.setProjectInfo(pInfo);
                addBranchFileInfo(destInfo, pInfo);
            } else {
                System.err.println("not found project name: '" + pInfo.getQualifiedName() + "'");
            }
        }
    }

    private void addBranchFileInfo(BranchInfo bInfo, ProjectInfo pInfo) {
        OpDepGraph graph = OpDepGraphInfo.createGraph(pInfo);
        List<FileOpDepGraph> fGraphs = graph.getFileGraphs();
        for (FileOpDepGraph fGraph : fGraphs) {
            List<ElementSlice> slices = new ArrayList<ElementSlice>();
            collectSlice(fGraph, slices);

            if (slices != null && slices.size() > 0) {
                bInfo.addBranchFileInfo(fGraph.getFileInfo(), slices);
            }
        }
    }

    private void collectSlice(FileOpDepGraph fGraph, List<ElementSlice> slices) {
        FileInfo fInfo = fGraph.getFileInfo();

        int idx = fInfo.getOperationNumber() - 1;
        String code = fInfo.getCode(idx);
        List<OpJavaElement> elems = new ArrayList<OpJavaElement>();
        boolean parseable = Parser.collectElements(fInfo, code, elems);
        if (!parseable) {
            return;
        }

        long time = fInfo.getOperation(idx).getTime();
        for (OpJavaElement elem : elems) {
            List<CodeSnippet> snippets = new ArrayList<CodeSnippet>();
            for (CodeRange range : elem.getRanges()) {
                int start = range.getStart();
                int end = range.getEnd();
                String text = code.substring(start, end);
                snippets.add(new CodeSnippet(start, end, idx, time, text));
            }

            ElementSliceCriterion criterion = new ElementSliceCriterion(fGraph, elem, snippets);
            if (hasTextChangedOperationNode(criterion)) {
                Slice slice = OpGraphSlicer.constructBackwardSlice(criterion);
                List<OpDepGraphNode> nodes = slice.getNodes();
                if (nodes.size() > 0) {
                    ElementSlice eSlice = new ElementSlice(slice);
                    slices.add(eSlice);
                }
            }
        }
    }

    private boolean hasTextChangedOperationNode(SliceCriterion criterion) {
        List<OpDepGraphNode> nodes = criterion.getNodes();
        if (nodes.size() == 0) {
            return false;
        }

        for (OpDepGraphNode node : nodes) {
            if (node.getOperation().isTextChangedOperation()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String getMessage() {
        return MESSAGE;
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

    @Override
    protected void nextState(ConflictDetectingDialog dialog) {
        ConflictDetector.getInstance().detect(dialog);
    }
}
