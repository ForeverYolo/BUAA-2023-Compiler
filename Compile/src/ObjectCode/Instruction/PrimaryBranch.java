package ObjectCode.Instruction;

public class PrimaryBranch extends PrimaryInstruction{
    private final int src1;
    private final int src2;
    private final String tag;

    public PrimaryBranch(String name, int deep, int src1, int src2, String tag) {
        super(name,deep);
        this.src1 = src1;
        this.src2 = src2;
        this.tag = tag;
    }

    public PrimaryBranch(String name, int deep, int src1, String tag) {
        super(name,deep);
        this.src1 = src1;
        this.src2 = -1;
        this.tag = tag;
    }

    @Override
    public String toString() {
        if (src2 < 0) {
            return this.name + " " + "$" + this.src1 + "," + tag;
        } else {
            return this.name + " " + "$" + this.src1 + "," + "$" + this.src2 + "," + tag;
        }
    }
}
