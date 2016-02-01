/*
 *  Copyright 2015
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changereplayer.ui;

import org.jtool.changerepository.data.FileInfo;
import org.jtool.changerepository.operation.UnifiedOperation;
import java.util.List;

/**
 * Checks if the operations for a file are replay-able.
 * @author Katsuhisa Maruyama
 */
public class OperationChecker {
    
    /**
     * The view that displays differences between codes containing errors.
     */
    private OperationCheckView operationCheckView;
    
    /**
     * Creates an operation checker.
     */
    public OperationChecker() {
        operationCheckView = new OperationCheckView();
    }
    
    /**
     * Opens check view.
     */
    public void open() {
        operationCheckView.open();
    }
    
    /**
     * Checks the restoration of source code.
     * @param finfo the source file information
     */
    public void checkCode(FileInfo finfo) {
        boolean errflag = false;
        List<UnifiedOperation> ops = finfo.getOperations();
        
        for (int i = 0; i < ops.size(); i++) {
            UnifiedOperation op = ops.get(i);
            
            if (op.isNormalOperation()) {
                String code = finfo.getCode(i);
                
                if (code == null) {
                    System.out.print("-- ERROR IN " + finfo.getFilePath() + " " + String.valueOf(i + 1));
                    i = findRestartPoint(ops, i);
                    
                    errflag = true;
                    
                } else {
                    UnifiedOperation o = skipOperation(ops, i + 1);
                    
                    if (o != null && o.isFileOperation()) {
                        String nextCode = o.getCode();
                        boolean result = checkCodes(code, nextCode, o);
                        /*
                        if (result) {
                            i = findRestartPoint(ops, i);
                        }
                        */
                        
                        errflag = errflag || result;
                        
                        if (errflag) {
                            break;
                        }
                    }
                }
                
            } else if (op.isFileOperation()) {
                String code = op.getCode();
                if (code != null) {
                    
                    if (i > 0) {
                        String prevCode = finfo.getCode(i - 1);
                        boolean result = checkCodes(prevCode, code, op);
                        /*
                        if (result) {
                            i = findRestartPoint(ops, i);
                        }
                        */
                        
                        errflag = errflag || result;
                        
                        if (errflag) {
                            break;
                        }
                    }
                    
                    if (!errflag) {
                        UnifiedOperation o = skipOperation(ops, i + 1);
                        
                        if (o != null && o.isFileOperation()) {
                            String nextCode = o.getCode();
                            boolean result = checkCodes(code, nextCode, o);
                            /*
                            if (result) {
                                  i = findRestartPoint(ops, i);
                            }
                            */
                            
                            errflag = errflag || result;
                            
                            if (errflag) {
                                break;
                            }
                        }
                    }
                }
                
            } else if (op.isCopyOperation()) {
                String code = finfo.getCode(i);
                boolean result = checkCodeOnCopy(code, op);
                /*
                if (result) {
                    i = findRestartPoint(ops, i);
                }
                */
                
                errflag = errflag || result;
                
                if (errflag) {
                    break;
                }
            }
        }
        
        if (!errflag) {
            System.out.println("NOT FOUND ERROR IN " + finfo.getFilePath());
        }
    }
    
    /**
     * Finds a next operation that restarts the consistency check.
     * @param ops the collection of all operations
     * @param j the index number for the inconsistent operation
     * @return the index number for the next file operation
     */
    private int findRestartPoint(List<UnifiedOperation> ops, int j) {
        for (j++ ; j < ops.size(); j++) {
            UnifiedOperation o = ops.get(j);
            if (o.isFileOperation()) {
                break;
            }
        }
        
        if (j == ops.size()) {
            System.out.println(" ... CANNOT RESTART");
        } else {
            System.out.println(" ... RESTART FROM FILE OPERATION " + String.valueOf(j + 1));
        }
        
        return j;
    }
    
    /**
     * Skips operations not affecting the change of the current code.
     * @param ops the collection of all operations
     * @param j the index number that denotes the current operation
     * @return the next operation affecting the change of the current code, <code>null</code> such operation was not detected
     */
    private UnifiedOperation skipOperation(List<UnifiedOperation> ops, int j) {
        UnifiedOperation op = null;
        if (j < ops.size()) {
            op = ops.get(j);
            while (op.isCopyOperation() || op.isMenuOperation()) {
                j++;
                if (j == ops.size()) {
                    return null;
                }
                op = ops.get(j);
            }
        }
        return op;
    }
    
    /**
     * Tests if the contents of two codes are are consistent with each other.
     * @param code1 the contents of code
     * @param code2 the contents of code
     * @param op the operation that restores the code2
     * @return <code>true</code> if the inconsistent was detected, otherwise <code>false</code>
     */
    private boolean checkCodes(String code1, String code2, UnifiedOperation op) {
        if (code1 != null && code2 != null && code1.compareTo(code2) != 0) {
            System.out.println("-- FILE MISMATCH: " + op.getTimeString() + " " + op.toString());
            operationCheckView.showDiff(code1, code2);
            
            /*
            System.out.println(code1);
            System.out.println("---");
            System.out.println(code2);
            */
            
            /*
            for (int s = 0; s < code1.length() && s < code2.length(); s++) {
                if (code1.charAt(s) != code2.charAt(s)) {
                    System.out.print("CHAR AT = " + s + " ");
                    System.out.print(Integer.toHexString((int)code1.charAt(s)) + "!=");
                    System.out.println(Integer.toHexString((int)code2.charAt(s)));
                }
            }
            */
            
            return true;
        }
        return false;
    }
    
    /**
     * Tests if the contents of a given copy operation are consistent with the contents of a given code.
     * @param code the code to be checked
     * @param op the copy operation
     * @return <code>true</code> if the inconsistent was detected, otherwise <code>false</code>
     */
    private boolean checkCodeOnCopy(String code, UnifiedOperation op) {
        if (code != null) {
            String text = op.getCutCopiedText();
            if (op.getDeletedText().length() > 0) {
                int start = op.getStart();
                int end = start + text.length();
                String copiedText = code.substring(start, end);
                
                if (text.compareTo(copiedText) != 0) {
                    System.out.print("-- COPY MISMATCH: " + op.getTimeString() + " " + op.toString());
                    return true;
                }
            }
        }
        return false;
    }
}
