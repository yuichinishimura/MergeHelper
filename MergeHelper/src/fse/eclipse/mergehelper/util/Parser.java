package fse.eclipse.mergehelper.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.parser.CodeRange;
import org.jtool.changerepository.parser.OpJavaElement;
import org.jtool.changerepository.parser.OpJavaVisitor;

public class Parser {
    public static int JLS_LEVEL = AST.JLS8;
    public static String JC_VERSION = JavaCore.VERSION_1_8;

    public static boolean collectElements(FileInfo fInfo, String code, List<OpJavaElement> elems) throws NullPointerException {
        CompilationUnit cu = createCompilationUnit(code);
        if (cu != null) {
            OpJavaVisitor visitor = new OpJavaVisitor(fInfo);
            cu.accept(visitor);

            elems.addAll(visitor.getJavaElements());
            return true;
        }
        return false;
    }

    public static OpJavaElement getElement(FileInfo fInfo, String code, String elemName) {
        List<OpJavaElement> elems = new ArrayList<>();
        boolean parseable = collectElements(fInfo, code, elems);
        if (!parseable) {
            return null;
        }

        for (OpJavaElement elem : elems) {
            if (elemName.equals(elem.getSimpleName())) {
                return elem;
            }
        }
        return null;
    }

    public static String getElementBody(FileInfo fInfo, String code, String elemName) {
        OpJavaElement elem = getElement(fInfo, code, elemName);
        if (elem == null) {
            return null;
        }

        CodeRange range = elem.getCodeRange();
        int start = range.getStart();
        int end = range.getEnd() + 1;
        return code.substring(start, end);
    }

    @SuppressWarnings("unchecked")
    private static CompilationUnit createCompilationUnit(String code) {
        ASTParser parser = ASTParser.newParser(JLS_LEVEL);
        Hashtable<String, String> options = (Hashtable<String, String>) JavaCore.getOptions();
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JC_VERSION);
        options.put(JavaCore.COMPILER_SOURCE, JC_VERSION);
        options.put(JavaCore.COMPILER_COMPLIANCE, JC_VERSION);
        parser.setCompilerOptions(options);

        parser.setSource(code.toCharArray());
        parser.setResolveBindings(false);

        ASTNode node = parser.createAST(null);
        if (node instanceof CompilationUnit) {
            return (CompilationUnit) node;
        } else {
            return null;
        }
    }
}
