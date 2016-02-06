package fse.eclipse.mergehelper.element;

public class MergePoint {

    private final String targetElement;

    private int a_point;
    private int j_point;

    public MergePoint(String targetElement) {
        this.targetElement = targetElement;
    }

    public String getTargetElement() {
        return targetElement;
    }

    public int getMergePoint(MergeType type) {
        if (MergeType.isAccept(type)) {
            return a_point;
        } else {
            return j_point;
        }
    }

    public void setMergePoint(int s_point, int d_point) {
        this.a_point = s_point;
        this.j_point = d_point;
    }
}
