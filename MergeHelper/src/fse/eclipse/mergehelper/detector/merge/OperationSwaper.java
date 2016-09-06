package fse.eclipse.mergehelper.detector.merge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jtool.changerecorder.operation.NormalOperation;
import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.data.ProjectInfo;
import org.jtool.changerepository.operation.UnifiedOperation;

import fse.eclipse.mergehelper.detector.AbstractDetector;
import fse.eclipse.mergehelper.element.BranchInfo;
import fse.eclipse.mergehelper.element.BranchJavaElement;
import fse.eclipse.mergehelper.element.BranchRootInfo;
import fse.eclipse.mergehelper.element.ConflictInfo;
import fse.eclipse.mergehelper.element.MergeType;
import fse.eclipse.mergehelper.ui.dialog.ConflictDetectingDialog;

public class OperationSwaper extends AbstractDetector {
    private static final String MESSAGE = "Swap Operation ...";
    private static final String ERROR_MESSAGE = "NULL";
    private static AbstractDetector instance = new OperationSwaper();

    private static final String UNKNOWN = "UNKNOWN";
    private static final String CLOSE = "CLOSE";

    private OperationSwaper() {
    }

    public static AbstractDetector getInstance() {
        return instance;
    }

    @Override
    protected void execute() {
        BranchRootInfo rootInfo = BranchRootInfo.getInstance();
        BranchInfo a_bInfo = rootInfo.getBranchInfo(MergeType.ACCEPT);
        BranchInfo j_bInfo = rootInfo.getBranchInfo(MergeType.JOIN);
        ConflictInfo cInfo = rootInfo.getConflictInfo();

        List<OperationHunk> a_hunks = createOpertationHunk(a_bInfo, cInfo);
        swapHunk(a_bInfo.getProjectInfo(), a_hunks);

        List<OperationHunk> j_hunks = createOpertationHunk(j_bInfo, cInfo);
        swapHunk(j_bInfo.getProjectInfo(), j_hunks);
    }

    private List<OperationHunk> createOpertationHunk(BranchInfo bInfo, ConflictInfo cInfo) {
        // TODO String[]: 複数の要素への対応
        Map<Integer, String[]> map = new TreeMap<Integer, String[]>();

        List<BranchJavaElement> confElems = cInfo.getAllBranchJavaElement(bInfo.getType());
        List<String> confElemNames = new ArrayList<>();
        confElems.forEach(elem -> confElemNames.add(elem.getFullName()));

        ProjectInfo pInfo = bInfo.getProjectInfo();
        List<UnifiedOperation> ops = pInfo.getOperations();
        int size = ops.size() - 1;
        for (int i = 0; i < size; i++) {
            UnifiedOperation op = ops.get(i);
            map.put(op.getId(), new String[] { UNKNOWN });
        }
        UnifiedOperation lastOp = ops.get(size);
        int lastCloseId = -1;
        if (lastOp.isFileCloseOperation()) {
            lastCloseId = lastOp.getId();
            map.put(lastCloseId, new String[] { CLOSE });
        } else {
            map.put(lastOp.getId(), new String[] { UNKNOWN });
        }

        List<BranchJavaElement> elems = bInfo.getAllBranchJavaElement();
        for (BranchJavaElement elem : elems) {
            String name = elem.getFullName();
            ops = elem.getOperations();
            size = ops.size();
            for (int i = 0; i < size; i++) {
                int id = ops.get(i).getId();
                if (id == lastCloseId) {
                    break;
                }

                String[] value = map.get(id);
                if (isUnknownValue(value)) {
                    map.put(id, new String[] { name });
                } else {
                    int length = value.length;
                    String[] newValue = new String[length + 1];
                    System.arraycopy(value, 0, newValue, 0, length);
                    newValue[length] = name;
                    map.put(id, newValue);
                }
            }
        }

        List<OperationHunk> opHunks = new LinkedList<>();
        OperationHunk hunk = null;
        boolean isConflictElement;
        for (Entry<Integer, String[]> entry : map.entrySet()) {
            int id = entry.getKey();
            String[] value = map.get(id);
            if (value.length >= 2) {
                continue;
            }

            if (hunk != null && !hunk.name.equals(value[0])) {
                opHunks.add(hunk);
                isConflictElement = confElemNames.contains(value[0]);
                hunk = new OperationHunk(value[0], id, isConflictElement);
            } else if (hunk == null) {
                isConflictElement = confElemNames.contains(value[0]);
                hunk = new OperationHunk(value[0], id, isConflictElement);
            }
            hunk.lastId = id;
        }
        if (hunk != null) {
            opHunks.add(hunk);
        }

        return opHunks;
    }

    private void swapHunk(ProjectInfo pInfo, List<OperationHunk> hunks) {
        // TODO 複数の要素への対応
        List<OperationHunk> tmphunks;
        do {
            tmphunks = new ArrayList<>(hunks);
            int start = 0;
            int size = hunks.size() - 1;
            if (hunks.get(size).name.equals(CLOSE)) {
                size--;
            }

            for (int i = start; i < size; i++) {
                OperationHunk hunk1 = hunks.get(i);
                if (hunk1.isConflictElement) {
                    OperationHunk hunk2 = hunks.get(i + 1);
                    if (!hunk2.isConflictElement) {
                        if (isSwap()) {
                            swapOperation(pInfo, hunk1, hunk2);
                            Collections.swap(hunks, i, i + 1);
                        }
                    }
                }
            }
        } while (!isEqualHunk(hunks, tmphunks));

        adaptFileInfoOperation(pInfo);
    }

    private void swapOperation(ProjectInfo pInfo, OperationHunk hunk1, OperationHunk hunk2) {
        int f1 = hunk1.firstId;
        int l1 = hunk1.lastId;
        int f2 = hunk2.firstId;
        int l2 = hunk2.lastId;

        List<UnifiedOperation> ops = pInfo.getOperations();
        for (int i = l1; i >= f1; i--) {
            for (int j = f2; j <= l2; j++) {
                UnifiedOperation op1 = pInfo.getOperationById(i);
                if (!op1.isNormalOperation()) {
                    continue;
                }

                UnifiedOperation op2 = pInfo.getOperationById(j);
                if (!op2.isNormalOperation()) {
                    continue;
                }

                NormalOperation nop1 = (NormalOperation) op1.getIOperation();
                NormalOperation nop2 = (NormalOperation) op2.getIOperation();

                int s1 = nop1.getStart();
                int s2 = nop2.getStart();
                if (s1 != s2) {
                    boolean isForwardOp1 = s1 < s2;
                    if (isForwardOp1) {
                        if (nop1.isInsertion()) {
                            if (nop2.isInsertion()) {
                                op2.setStart(s2 - op1.maxInsertedTextLength());
                            } else if (nop2.isDeletion()) {
                                op2.setStart(s2 - op1.maxInsertedTextLength());
                            }
                        } else if (nop1.isDeletion()) {
                            if (nop2.isInsertion()) {
                                op2.setStart(s2 + op1.maxDeletedOrCopiedTextLength());
                            } else if (nop2.isDeletion()) {
                                op2.setStart(s2 + op1.maxDeletedOrCopiedTextLength());
                            }
                        }

                        if (nop1.isReplace()) {
                            int iLength = op1.maxInsertedTextLength();
                            int dLength = op1.maxDeletedOrCopiedTextLength();
                            if (iLength >= dLength) {
                                op2.setStart(op2.getStart() + dLength);
                            } else {
                                op2.setStart(op2.getStart() - iLength);
                            }
                        }
                    } else {
                        if (nop1.isInsertion()) {
                            if (nop2.isInsertion()) {
                                op1.setStart(s1 + op2.maxInsertedTextLength());
                            } else if (nop2.isDeletion()) {
                                op1.setStart(s1 - op2.maxDeletedOrCopiedTextLength());
                            }
                        } else if (nop1.isDeletion()) {
                            if (nop2.isInsertion()) {
                                op1.setStart(s1 + op2.maxInsertedTextLength());
                            } else if (nop2.isDeletion()) {
                                op1.setStart(s1 - op2.maxDeletedOrCopiedTextLength());
                            }
                        }

                        if (nop2.isReplace()) {
                            int iLength = op2.maxInsertedTextLength();
                            int dLength = op2.maxDeletedOrCopiedTextLength();
                            if (iLength >= dLength) {
                                op1.setStart(op1.getStart() - dLength);
                            } else {
                                op1.setStart(op1.getStart() + iLength);
                            }
                        }
                    }
                }

                long tmpTime = op2.getTime();
                op2.setTime(op1.getTime());
                op1.setTime(tmpTime);

                int a = ops.indexOf(op1);
                int b = ops.indexOf(op2);
                Collections.swap(ops, a, b);
            }
        }
    }

    private int searchOperationIndex(List<UnifiedOperation> ops, int id) {
        int idx = 0;
        for (UnifiedOperation op : ops) {
            if (op.getId() == id) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

    private void adaptFileInfoOperation(ProjectInfo pInfo) {
        List<UnifiedOperation> ops = pInfo.getOperations();
        Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
        for (UnifiedOperation op : ops) {
            String name = op.getFileInfo().getQualifiedName();
            if (map.containsKey(name)) {
                map.get(name).add(op.getId());
            } else {
                List<Integer> ids = new ArrayList<Integer>();
                ids.add(op.getId());
                map.put(name, ids);
            }
        }

        List<FileInfo> fInfos = pInfo.getAllFileInfo();
        for (FileInfo fInfo : fInfos) {
            List<Integer> ids = map.get(fInfo.getQualifiedName());
            List<UnifiedOperation> new_ops = new ArrayList<>();
            for (int id : ids) {
                int idx = searchOperationIndex(ops, id);
                new_ops.add(ops.get(idx));
            }
            fInfo.setOperations(new_ops);
        }
    }

    private boolean isEqualHunk(List<OperationHunk> hunks, List<OperationHunk> hunks2) {
        int size = hunks.size();
        if (size != hunks2.size()) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            OperationHunk hunk = hunks.get(i);
            OperationHunk hunk2 = hunks2.get(i);
            if (!hunk.equals(hunk2)) {
                return false;
            }
        }
        return true;
    }

    private boolean isUnknownValue(String[] value) {
        if (value.length > 1) {
            return false;
        }
        return value[0].equals(UNKNOWN);
    }

    private boolean isSwap() {
        return true;
    }

    @Override
    protected String getMessage() {
        return MESSAGE;
    }

    @Override
    protected String getErrorMessage() {
        return ERROR_MESSAGE;
    }

    @Override
    protected void nextState(ConflictDetectingDialog dialog) {
        AMergePointSearcher.getInstance().detect(dialog);
    }

    private class OperationHunk {
        String name;
        int firstId, lastId;
        boolean isConflictElement;

        OperationHunk(String name, int firstId, boolean isConflictElement) {
            this.name = name;
            this.firstId = firstId;
            this.isConflictElement = isConflictElement;
        }

        boolean equals(OperationHunk hunk) {
            if (!this.name.equals(hunk.name)) {
                return false;
            }
            if (this.firstId != hunk.firstId) {
                return false;
            }
            if (this.lastId != hunk.lastId) {
                return false;
            }
            if (this.isConflictElement != hunk.isConflictElement) {
                return false;
            }
            return true;
        }
    }
}
