package ObjectCode.Instruction;

public class PrimaryMemory extends PrimaryInstruction{
    protected int dst;
    protected int offset;
    protected int addr;

    public PrimaryMemory(String name,int deep,int dst,int offset,int addr) {
        super(name, deep);
        this.dst = dst;
        this.offset = offset;
        this.addr = addr;
    }

    @Override
    public String toString() {
        return this.name + " " + "$" + dst + "," + offset + "($" + addr + ")";
    }
}
