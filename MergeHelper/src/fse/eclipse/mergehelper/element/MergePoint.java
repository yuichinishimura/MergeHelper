package fse.eclipse.mergehelper.element;

public class MergePoint {

    private final String targetElement;

    private int s_point;
    private int d_point;

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

    public void setMergePoint(int s_point, int d_point) {
        this.s_point = s_point;
        this.d_point = d_point;
    }
}
