package fse.eclipse.mergehelper.element;

public class MergePoint {

    private final String targetElement;

    private int s_point;
    private int s_fileId;

    private int d_point;
    private int d_fileId;

    public MergePoint(String targetElement) {
        this.targetElement = targetElement;
    }

    public String getTargetElement() {
        return targetElement;
    }

    public int getMergePoint(MergeType type) {
        if (MergeType.isMergeSrc(type)) {
            return s_point;
        } else {
            return d_point;
        }
    }

    public int getFileId(MergeType type) {
        if (MergeType.isMergeSrc(type)) {
            return s_fileId;
        } else {
            return d_fileId;
        }
    }

    public void setMergePoint(int s_point, int s_fileId, int d_point, int d_fileId) {
        this.s_point = s_point;
        this.s_fileId = s_fileId;

        this.d_point = d_point;
        this.d_fileId = d_fileId;
    }
}
