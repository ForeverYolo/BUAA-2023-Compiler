package ObjectCode.Instruction;

public class PrimaryPureReg extends PrimaryInstruction {
    protected int dst;
    protected int src1;
    protected int src2;
    public PrimaryPureReg(String name, int deep, int dst, int src1, int src2) {
        super(name, deep);
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
    }

    public PrimaryPureReg(String name, int deep, int dst, int src1) {
        super(name, deep);
        this.dst = dst;
        this.src1 = src1;
        this.src2 = -1;
    }

    public PrimaryPureReg(String name, int deep, int dst) {
        super(name, deep);
        this.dst = dst;
        this.src1 = -1;
        this.src2 = -1;
    }

    @Override
    public String toString() {
        if (src1 != -1 && src2 != -1) {
            return this.name + " " + "$" + dst + ",$" + src1 + ",$" + src2;
        } else if (src1 != -1) {
            return this.name + " " + "$" + dst + ",$" + src1;
        } else {
            return this.name + " " + "$" + dst;
        }
    }
}
