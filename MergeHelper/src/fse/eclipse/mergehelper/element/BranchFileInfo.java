package fse.eclipse.mergehelper.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.operation.UnifiedOperation;

import fse.eclipse.mergehelper.util.RepositoryElementInfoUtil;

public class BranchFileInfo {

    private final String name;
    private final FileInfo fInfo;
    private final List<ElementSlice> slices;

    private Map<Integer, MergedResult> mergedResult;

    BranchFileInfo(FileInfo fInfo, List<ElementSlice> slices) {
        name = RepositoryElementInfoUtil.getFileNameExceptExtension(fInfo);
        this.fInfo = fInfo;
        this.slices = slices;

        mergedResult = new HashMap<Integer, MergedResult>();
    }

    public String getName() {
        return name;
    }

    public String getExtensionName() {
        return fInfo.getName();
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(fInfo.getPackageInfo().getName());
        sb.append("#").append(getExtensionName());
        return sb.toString();
    }

    public List<ElementSlice> getAllSlice() {
        return Collections.unmodifiableList(slices);
    }

    public List<ElementSlice> getSlices(UnifiedOperation op) {
        String fileName = op.getFileInfo().getName();
        if (!fileName.equals(getExtensionName())) {
            return null;
        }

        List<ElementSlice> returnSlices = new ArrayList<ElementSlice>();
        for (ElementSlice slice : slices) {
            if (slice.contains(op)) {
                returnSlices.add(slice);
            }
        }
        return returnSlices;
    }

    public Map<Integer, MergedResult> getMergedResultMap() {
        return Collections.unmodifiableMap(mergedResult);
    }

    public void addMergedResult(int idx, String mergedCode, int opOffset) {
        mergedResult.put(idx, new MergedResult(mergedCode, opOffset));
    }

    public boolean equals(FileInfo fInfo2) {
        String projName = fInfo.getProjectInfo().getName();
        String projName2 = fInfo2.getProjectInfo().getName();

        StringBuilder sb = new StringBuilder();
        sb.append(projName).append("#").append(getFullName());

        StringBuilder sb2 = new StringBuilder();
        sb2.append(projName2).append("#");
        sb2.append(fInfo2.getPackageInfo().getName()).append("#").append(fInfo2.getName());

        return sb.toString().equals(sb2.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getFullName()).append(" slice[ ");
        for (ElementSlice slice : slices) {
            sb.append(slice.getName()).append(":").append(slice.size()).append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

    public class MergedResult {
        private final String mergedCode;
        private final int opOffset;

        private MergedResult(String mergedCode, int opOffset) {
            this.mergedCode = mergedCode;
            this.opOffset = opOffset;
        }

        public String getCode() {
            return mergedCode;
        }

        public int getOperationOffset() {
            return opOffset;
        }
    }
}