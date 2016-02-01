package fse.eclipse.mergehelper.element;

import java.util.List;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.dependencygraph.FileOpDepGraph;
import org.jtool.changerepository.parser.OpJavaElement;
import org.jtool.changeslicereplayer.slicer.CodeSnippet;
import org.jtool.changeslicereplayer.slicer.SliceCriterion;

import fse.eclipse.mergehelper.util.RepositoryElementInfoUtil;

public class ElementSliceCriterion extends SliceCriterion {

    private final FileInfo fInfo;
    private final OpJavaElement elem;

    public ElementSliceCriterion(FileOpDepGraph graph, OpJavaElement elem, CodeSnippet snippet) {
        super(graph, elem.getName(), snippet);
        fInfo = graph.getFileInfo();
        this.elem = elem;
    }

    public ElementSliceCriterion(FileOpDepGraph graph, OpJavaElement elem, List<CodeSnippet> snippets) {
        super(graph, elem.getName(), snippets);
        fInfo = graph.getFileInfo();
        this.elem = elem;
    }

    @Override
    public String getName() {
        return elem.getSimpleName();
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPackageName()).append(".");
        sb.append(getFileName()).append("#");
        sb.append(getName());
        return sb.toString();
    }

    public String getFileName() {
        return RepositoryElementInfoUtil.getFileNameExceptExtension(fInfo);
    }

    public String getExtensionFileName() {
        return fInfo.getName();
    }

    public String getPackageName() {
        return fInfo.getPackageInfo().getName();
    }
}
