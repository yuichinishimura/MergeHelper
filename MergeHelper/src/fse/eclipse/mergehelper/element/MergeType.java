package fse.eclipse.mergehelper.element;

public enum MergeType {

    SRC, DEST;

    public static boolean isMergeSrc(MergeType type) {
        if (type == null) {
            throw new Error("MergeType is Null");
        }
        return type == SRC;
    }

    public static boolean isMergeDest(MergeType type) {
        if (type == null) {
            throw new Error("MergeType is Null");
        }
        return type == DEST;
    }

    public static MergeType getAnotherType(MergeType type) {
        if (isMergeSrc(type)) {
            return DEST;
        } else {
            return SRC;
        }
    }
}