package ObjectCode.Instruction;

public class PrimaryRegOffset extends PrimaryInstruction {
    protected int dst;
    protected int src1;
    protected int offset;

    public PrimaryRegOffset(String name,int deep,int dst,int src1,int offset) {
        super(name, deep);
        this.dst = dst;
        this.src1 = src1;
        this.offset = offset;
    }
    public PrimaryRegOffset(String name,int deep,int dst,int offset) {
        super(name, deep);
        this.dst = dst;
        this.src1 = -1;
        this.offset = offset;
    }

    @Override
    public String toString() {
        if (src1 != -1) {
            return this.name + " $" + dst + ",$" + src1 + "," + offset;
        } else {
            return this.name + " $" + dst + "," + offset;
        }
    }
}