/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changerepository.operation;

/**
 * Enforces to apply the editing operation to the source code.
 * @author Katsuhisa Maruyama
 */
public class OperationRestorer {
    
    /**
     * The string that contains information on the cause of a occurring error.
     */
    private String errorStatus = "";
    
    /**
     * Obtains the causes for the error in debugging.
     * @return the string containing information on the causes of the error.
     */
    public String getErrorStatus() {
        return errorStatus;
    }
    
    /**
     * Obtains the results of application of the specified operation into the code.
     * @param code the code that will be applied the operation into
     * @param op the operation to be applied
     * @return the resulting code after the application
     */
    public String applyOperation(String code, UnifiedOperation op) {
        if (op.isNormalOperation()) {
            return applyNormalOperation(code, op);
        }
        return code;
    }
    
    /**
     * Applies a specified normal operation into a given code.
     * @param code the code that will be applied the operation into
     * @param op the operation to be applied
     * @return the resulting code after the application
     */
    private String applyNormalOperation(String code, UnifiedOperation op) {
        StringBuffer buf = new StringBuffer(code);
        try {
            int start = op.getStart();
            int end = start + op.getDeletedText().length();
            
            if (op.getDeletedText().length() > 0) {
                String replacedText = buf.substring(start, end);
                
                if (replacedText != null && replacedText.compareTo(op.getDeletedText()) != 0) {
                    
                    /*
                    System.out.println(replacedText.length() + " " + op.getDeletedText().length());
                    for (int i = 0; i < replacedText.length(); i++) {
                        if (replacedText.charAt(i) == op.getDeletedText().charAt(i)) {
                            System.out.println(((int)replacedText.charAt(i)) + " == " + ((int)op.getDeletedText().charAt(i)));
                        } else {
                            System.out.println(((int)replacedText.charAt(i)) + " != " + ((int)op.getDeletedText().charAt(i)));
                        }
                    }
                    */
                    
                    errorStatus = "Mismatch found:" +  op.getTime() +
                                  "[" + op.getDeletedText() + "] to be replaced with [" + replacedText + "]";
                    return null;
                }
            }
            
            buf.replace(start, end, op.getInsertedText());
            
        } catch (StringIndexOutOfBoundsException e) {
            errorStatus = "Out of the text range with: " + op.toString();
            return null;
        }
        return buf.toString();
    }
    
    /**
     * Obtains the results of reverse application of the specified operation into the code.
     * @param code the code that will be applied the operation into
     * @param op the operation to be applied
     * @return the resulting code after the reverse application
     */
    public String applyOperationReversely(String code, UnifiedOperation op) {
        if (op.isNormalOperation()) {
            return applyNormalOperationReversely(code, op);
        }
        return code;
    }
    
    /**
     * Applies a specified normal operation into a given code reversely.
     * @param code the code that will be applied the operation into
     * @param op the operation to be applied
     * @return the resulting code after the reverse application
     */
    private String applyNormalOperationReversely(String code, UnifiedOperation op) {
        StringBuffer buf = new StringBuffer(code);
        try {
            int start = op.getStart();
            int end = start + op.getInsertedText().length();
            
            if (op.getInsertedText().length() > 0) {
                String replacedText = buf.substring(start, end);
                
                if (replacedText != null && replacedText.compareTo(op.getInsertedText()) != 0) {
                    
                    /*
                    System.out.println(replacedText.length() + " " + op.getInsertedText().length());
                    for (int i = 0; i < replacedText.length(); i++) {
                        if (replacedText.charAt(i) == op.getInsertedText().charAt(i)) {
                            System.out.println(((int)replacedText.charAt(i)) + " == " + ((int)op.getInsertedText().charAt(i)));
                        } else {
                            System.out.println(((int)replacedText.charAt(i)) + " != " + ((int)op.getInsertedText().charAt(i)));
                        }
                    }
                    */
                    
                    errorStatus = "Mismatch found:" +  op.getTime() +
                            "[" + op.getInsertedText() + "] to be replaced with [" + replacedText + "]";
                    return null;
                }
            }
            
            buf.replace(start, end, op.getDeletedText());
        
        } catch (StringIndexOutOfBoundsException e) {
            errorStatus = "Out of the text range with: " + op.toString();
            return null;
        }
        return buf.toString();
    }
}
