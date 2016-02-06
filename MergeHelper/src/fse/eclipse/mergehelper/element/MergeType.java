package fse.eclipse.mergehelper.element;

public enum MergeType {

    ACCEPT, JOIN;

    public static boolean isAccept(MergeType type) {
        if (type == null) {
            throw new Error("MergeType is Null");
        }
        return type == ACCEPT;
    }

    public static boolean isJoin(MergeType type) {
        if (type == null) {
            throw new Error("MergeType is Null");
        }
        return type == JOIN;
    }

    public static MergeType getAnotherType(MergeType type) {
        if (isAccept(type)) {
            return JOIN;
        } else {
            return ACCEPT;
        }
    }
}