/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.operation;

import org.jtool.changerepository.Activator;
import org.jtool.changerepository.data.FileInfo;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Manages information on the operations for a file.
 * @author Katsuhisa Maruyama
 */
public class OperationManager {
    
    /**
     * The information on the file.
     */
    private FileInfo fileInfo;
    
    /**
     * The collection of all restoration points for the file.
     */
    private List<RestorationPoint> restorations;
    
    /**
     * Creates an instance managing all the operations for the file.
     * @param finfo the information on the file
     */
    public OperationManager(FileInfo finfo) {
        fileInfo = finfo;
    }
    
    /**
     * Creates information on the operations for the file.
     * @param ops the collection of the original operations
     * @return the collection of the fabricated operations
     */
    public List<UnifiedOperation> createOperationInfo(List<UnifiedOperation> ops) {
        List<UnifiedOperation> operations = getFabricatedOperations(ops);
        
        restorations = getRestorationPoints(operations);
        return operations;
    }
    
    /**
     * Fabricates operations stored in the history information.
     * @param ops the operations to be fabricated
     * @return the collection of the fabricated operations
     */
   private List<UnifiedOperation> getFabricatedOperations(List<UnifiedOperation> ops) {
        ops = OperationHistoryFabricator.fabricate(ops);
        
        if (Activator.mergeOperations()) {
            ops = OperationHistoryFabricator.merge(ops);
        }
        return ops;
    }
    
    /**
     * Obtains restoration points for respective operations.
     * @return the array list of the restoration points
     */
    private List<RestorationPoint> getRestorationPoints(List<UnifiedOperation> ops) {
        List<RestorationPoint> rests = new ArrayList<RestorationPoint>();
        for (int idx = 0; idx < ops.size(); idx++) {
            UnifiedOperation op = ops.get(idx);
            if (op.isFileNewOperation() ||
                op.isFileOpenOperation() ||
                op.isFileCloseOperation() ||
                op.isFileDeleteOperation() ||
                op.isCommitOpeartion()) {
                rests.add(new RestorationPoint(idx, op.getTime(), op.getCode()));
            }
        }
        return rests;
    }
    
    /**
     * Returns the number of the restoration points.
     * @return the number of the restoration points
     */
    public int getRestorationPointNumber() {
        return restorations.size();
    }
    
    /**
     * Obtains the restoration point immediately before the operation with the specified index.
     * @param idx the index of the operation
     * @return the restoration point, or <code>null</code> if the restoration point was not found
     */
    private RestorationPoint getFormerRestorationPoint(int idx) {
        if (restorations.size() == 0) {
            return null;
        }
        
        for (int i = 0; i < restorations.size(); i++) {
            RestorationPoint pt = restorations.get(i);
            if (pt.getIndex() > idx) {
                
                if (i == 0) {
                    return null;
                }else{
                    return restorations.get(i - 1);
                }
            }
        }
        return restorations.get(restorations.size() - 1);
    }
    
    /**
     * Obtains the restoration point at the operation with the specified index.
     * @param idx the index of the operation
     * @return the restoration point, or <code>null</code> if the restoration point was not found
     */
    private RestorationPoint getRestorationPoint(int idx) {
        for (int i = 0; i < restorations.size(); i++) {
            RestorationPoint pt = restorations.get(i);
            if (pt.getIndex() == idx) {
                return pt;
            }
        }
        return null;
    }
    
    /**
     * Returns the time when the first operation was performed immediately after the specified time.
     * @param time the specified time
     * @return the time for the first operation, or <code>-1</code> if none
     */
    public long getNextOperationTime(long time) {
        UnifiedOperation op = getNearestLaterOperation(0, fileInfo.getOperations().size() - 1, time);
        if (op != null) {
            return op.getTime();
        }
        return -1;
    }
    
    /**
     * Returns the time when the last operation was performed immediately before the specified time.
     * @param time the specified time
     * @return the time for the last operation, or <code>-1</code> if none
     */
    public long getPreviousOperationTime(long time) {
        UnifiedOperation op = getNearestFormerOperation(0, fileInfo.getOperations().size() - 1, time);
        if (op != null) {
            return op.getTime();
        }
        return -1;
    }
    
    /**
     * Retrieves the operation performed at the specified time and returns it.
     * @param time the specified time
     * @return the found operation, where the first matched one if multiple ones are matched by using the binary search
     */
    public UnifiedOperation getOperationByTime(long time) {
        return getOperationByTime(0, fileInfo.getOperations().size() - 1, time);
    }
    
    /**
     * Retrieves the operation performed at the specified time and returns the sequence number of it.
     * @param time the specified time
     * @return the sequence number of the found operation, where the first matched one if multiple ones are matched by using the binary search
     */
    public int getOperationIdxByTime(long time) {
        return fileInfo.getOperations().indexOf(getOperationByTime(time));
    }
    
    /**
     * Retrieves the first operation performed immediately after the specified time
     * within the time period defined by the specified two operations and returns it.
     * @param from the sequence number of the specified operation indicating the start point of the time period
     * @param to the sequence number of the specified operation indicating the end point of the time period
     * @param time the specified time
     * @return the found operation, where the first matched one if multiple ones are matched by using the binary search, or <code>null</code> if none
     */
    private UnifiedOperation getNearestLaterOperation(int from, int to, long time) {
        List<UnifiedOperation> ops = fileInfo.getOperations();
        
        if (ops.get(from).getTime() > time) {
            return ops.get(from);
        }
        if (ops.get(to).getTime() <= time) {
            return null;
        }
        
        if (from == to - 1) {
            return ops.get(to);
        }
        
        int mid = (from + to) / 2;
        if (ops.get(mid).getTime() > time) {
            return getNearestLaterOperation(from, mid, time);
            
        } else {
            return getNearestLaterOperation(mid + 1, to, time);
        }
    }
    
    /**
     * Retrieves the last operation performed immediately before the specified time
     * within the time period defined by the specified two operations and returns it.
     * @param from the sequence number of the specified operation indicating the start point of the time period
     * @param to the sequence number of the specified operation indicating the end point of the time period
     * @param time the specified time
     * @return the found operation, where the first matched one if multiple ones are matched by using the binary search, or <code>null</code> if none
     */
    private UnifiedOperation getNearestFormerOperation(int from, int to, long time) {
        List<UnifiedOperation> ops = fileInfo.getOperations();
        
        if (ops.get(from).getTime() >= time) {
            return null;
        }
        if (ops.get(to).getTime() < time) {
            return ops.get(to);
        }
        
        if (from + 1 == to) {
            return ops.get(from);
        }
        
        int mid = (from + to) / 2;
        if (ops.get(mid).getTime() >= time) {
            return getNearestFormerOperation(from, mid - 1, time);
            
        } else {
            return getNearestFormerOperation(mid, to, time);
        }
    }
    
    /**
     * Retrieves the operation performed at the specified time within the time period
     * defined by the specified two operations and returns it.
     * @param from the sequence number of the specified operation indicating the start point of the time period
     * @param to the sequence number of the specified operation indicating the end point of the time period
     * @param time the specified time
     * @return the found operation, where the first matched one if multiple ones are matched by using the binary search, or <code>null</code> if none
     */
    private UnifiedOperation getOperationByTime(int from, int to, long time) {
        return getOperationByTime(fileInfo.getOperations(), from, to, time);
    }
    
    /**
     * Retrieves the operation performed at the specified time within the time period
     * defined by the specified two operations and returns it.
     * @param ops the operations included in the history
     * @param time the specified time
     * @return the found operation, where the first matched one if multiple ones are matched by using the binary search, or <code>null</code> if none
     */
    public static UnifiedOperation getOperationByTime(List<UnifiedOperation> ops, long time) {
        return getOperationByTime(ops, 0, ops.size() - 1, time);
    }
    
    /**
     * Retrieves the operation performed at the specified time within the time period
     * defined by the specified two operations and returns it.
     * @param ops the operations included in the history
     * @param from the sequence number of the specified operation indicating the start point of the time period
     * @param to the sequence number of the specified operation indicating the end point of the time period
     * @param time the specified time
     * @return the found operation, where the first matched one if multiple ones are matched by using the binary search, or <code>null</code> if none
     */
    private static UnifiedOperation getOperationByTime(List<UnifiedOperation> ops, int from, int to, long time) {
        int mid = (from + to) / 2;
        long t = ops.get(mid).getTime();
        
        if (from > to) {
            return null;
        }
        
        if (t == time) {
            return ops.get(mid);
            
        } else if (t < time) {
            if (mid + 1 >= ops.size()) {
                return null;
            }
            return getOperationByTime(ops, mid + 1, to, time);
        
        } else {
            if (mid - 1 < 0) {
                return null;
            }
            return getOperationByTime(ops, from, mid - 1, time);
        }
    }
    
    /**
     * Obtains the content of source code restored at the time when the operation with the specified sequence number was performed.
     * @param idx the sequence number of the specified operation indicating the restoration time
     * @return the content of restored source code, <code>null</code> if the restoration fails
     */
    public String restore(int idx) {
        List<UnifiedOperation> ops = fileInfo.getOperations();
        
        if (idx < 0) {
            return "";
        }
        
        if (ops.size() <= idx) {
            idx = ops.size() - 1;
        }
        
        if (ops.get(idx).isFileNewOperation() || ops.get(idx).isFileOpenOperation()) {
            RestorationPoint pt = getRestorationPoint(idx);
            if (pt != null) {
                return pt.getCode();
            } else {
                return null;
            }
        }
        
        RestorationPoint pt = getFormerRestorationPoint(idx);
        if (pt == null) { 
            System.err.print("Not found restartaion point: " + idx);
            return null;
        }
        
        try {
            String code = applyOperations(pt.getCode(), pt.getIndex() - 1, idx);
            return code;
        } catch (Exception e) {
            System.err.print(e.getMessage());
        }
        return null;
    }
    
    /**
     * Obtains the content of source code restored at the time when the operation with the specified sequence number was performed.
     * @param curCode the code for the the sequence number of the current operation
     * @param curIdx the sequence number of the current operation
     * @param idx the sequence number of the specified operation indicating the restoration time
     * @return the content of restored source code, <code>null</code> if the restoration fails
     */
    public String restore(String curCode, int curIdx, int idx) {
        if (curIdx < 0) {
            curIdx = 0;
        }
        if (idx < 0) {
            idx = 0;
        }
        
        List<UnifiedOperation> ops = fileInfo.getOperations();
        if (ops.size() <= curIdx) {
            curIdx = ops.size() - 1;
        }
        if (ops.size() <= idx) {
            idx = ops.size() - 1;
        }
        
        if (ops.get(idx).isFileOpenOperation()) {
            RestorationPoint pt = getRestorationPoint(idx);
            if (pt != null) {
                return pt.getCode();
            } else {
                return null;
            }
        }
        
        String code = applyOperations(curCode, curIdx, idx);
        return code;
    }
    
    /**
     * Applies operations within the time range defined by the specified two operations to code. 
     * @param code the original source code
     * @param from the sequence number of the specified operation indicating the start point of the time range
     * @param to the sequence number of the specified operation indicating the end point of the time range
     * @return the content of the restored code, or the original code if the time range is invalid
     */
    private String applyOperations(String code, int from, int to) {
        if (from == to) {
            return code;
        }
        
        List<UnifiedOperation> ops = fileInfo.getOperations();
        OperationRestorer enforcer = new OperationRestorer();
        if (from < to) {
            for (int idx = from + 1; idx <= to && code != null; idx++) {
                UnifiedOperation op = ops.get(idx);
                
                if (op.isTextChangedOperation()) {
                    code = enforcer.applyOperation(code, op);
                    if (code == null) {
                        System.err.print(enforcer.getErrorStatus());
                    }
                }
            }
            
        } else {
            for (int idx = from; idx > to && code != null; idx--) {
                UnifiedOperation op = ops.get(idx);
                
                if (op.isTextChangedOperation()) {
                    code = enforcer.applyOperationReversely(code, op);
                    if (code == null) {
                        System.err.print(enforcer.getErrorStatus());
                    }
                }
            }
            
        }
        
        return code;
    }
    
    /**
     * Retrieves a developer who edits source code at the specified time and returns his/her name.
     * @param time the specified time
     * @return the developer name, or <code>null</code> if none
     */
    public String getAuthor(long time) {
        for (UnifiedOperation op : fileInfo.getOperations()) {
            if (op.getTime() == time) {
                return op.getAuthor();
            }
        }
        return null;
    }
    
    /**
     * Retrieves the operation with a given identification number and returns its sequence number.
     * @param ops the operations to be retrieved
     * @param id the identification number of the operation to be retrieved
     * @return the sequence number of the found operation, or <code>-1</code> if none
     */
    public static int getOperation(List<UnifiedOperation> ops, int id) {
        if (ops == null || ops.size() == 0) {
            return -1;
        }
        
        for (int idx = 0; idx < ops.size(); idx++) {
            if (ops.get(idx).getId() == id) {
                return idx;
            }
        }
        return -1;
    }
    
    /**
     * Tests if the operation performed at the specified time exists in the operation history
     * @param ops the operations included in the history
     * @param time the specified time
     * @return <code>true</code> if the operation exists, otherwise <code>false</code>
     */
    public static boolean existOperationInHistory(List<UnifiedOperation> ops, long time) {
        if (ops == null || ops.size() == 0) {
            return false;
        }
        
        if (ops.get(0).getTime() > time) {
            return false;
        }
        
        if (ops.get(ops.size() - 1).getTime() < time) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Retrieves the latest operation performed at the specified time or before, and returns its sequence number.
     * @param ops the operations to be checked
     * @param time the specified time
     * @return the sequence number of the found operation, or <code>-1</code> if none
     */
    public static int getLatestOperationBefore(List<UnifiedOperation> ops, long time) {
        if (ops == null || ops.size() == 0) {
            return -1;
        }
        
        if (ops.get(0).getTime() > time) {
            return -1;
        }
        
        for (int idx = ops.size() - 1; idx >= 0; idx--) {
            if (ops.get(idx).getTime() <= time) {
                return idx;
            }
        }
        return -1;
    }
    
    /**
     * Retrieves the latest operation performed before the specified time and returns its sequence number.
     * @param ops the operations to be checked
     * @param time the specified time
     * @return the sequence number of the found operation, or <code>-1</code> if none
     */
    public static int getLatestOperation(List<UnifiedOperation> ops, long time) {
        if (ops == null || ops.size() == 0) {
            return -1;
        }
        
        if (ops.get(0).getTime() > time) {
            return -1;
        }
        
        for (int idx = ops.size() - 1; idx >= 0; idx--) {
            if (ops.get(idx).getTime() < time) {
                return idx;
            }
        }
        return -1;
    }
    
    /**
     * Retrieves the earliest operation performed at the specified time or after the specified time and returns its sequence number.
     * @param ops the operations to be checked
     * @param time the specified time
     * @return the sequence number of the found operation, or <code>-1</code> if none
     */
    public static int getEarliestOperationAfter(List<UnifiedOperation> ops, long time) {
        if (ops == null || ops.size() == 0) {
            return -1;
        }
        
        if (ops.get(ops.size() - 1).getTime() < time) {
            return -1;
        }
        
        for (int idx = 0; idx < ops.size(); idx++) {
            if (ops.get(idx).getTime() >= time) {
                return idx;
            }
        }
        return -1;
    }
    
    /**
     * Retrieves the earliest operation performed after the specified time and returns its sequence number.
     * @param ops the operations to be checked
     * @param time the specified time
     * @return the sequence number of the found operation, or <code>-1</code> if none
     */
    public static int getEarliestOperation(List<UnifiedOperation> ops, long time) {
        if (ops == null || ops.size() == 0) {
            return -1;
        }
        
        if (ops.get(ops.size() - 1).getTime() < time) {
            return -1;
        }
        
        for (int idx = 0; idx < ops.size(); idx++) {
            if (ops.get(idx).getTime() > time) {
                return idx;
            }
        }
        return -1;
    }
    
    /**
     * Retrieves the latest file operation performed at the specified time and returns its sequence number.
     * @param ops the operations to be checked
     * @param idx the sequence number indicating the time when its operation was performed 
     * @return the sequence number of the found file operation, or <code>-1</code> if none
     */
    public static int getBeforeFileOpen(List<UnifiedOperation> ops, int idx0) {
        if (ops == null || ops.size() == 0) {
            return -1;
        }
        if (idx0 < 0) {
            idx0 = 0;
            
        }
        if (idx0 >= ops.size()) {
            idx0 = ops.size() - 1;
        }
        
        for (int idx = idx0; idx >= 0; idx--) {
            UnifiedOperation op = ops.get(idx);
            if (op.isFileOpenOperation()) {
                return idx;
            }
        }
        return -1;
    }
    
    /**
     * Sorts the operations in time order.
     * @param ops the operations to be sorted
     */
    public static void sort(List<UnifiedOperation> ops) {
        Collections.sort(ops, new Comparator<UnifiedOperation>() {
            
            public int compare(UnifiedOperation o1, UnifiedOperation o2) {
                long time1 = o1.getTime();
                long time2 = o2.getTime();
                
                if (time1 > time2) {
                    return 1;
                    
                } else if (time1 < time2) {
                    return -1;
                
                } else {
                    int seq1 = o1.getSequenceNumber();
                    int seq2 = o2.getSequenceNumber();
                    
                    if (seq1 > seq2) {
                        return 1;
                    } else if (seq1 < seq2) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        });
    }
}
