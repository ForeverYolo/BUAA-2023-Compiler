package ObjectCode.Instruction;

public class PrimaryInstruction {
    protected int deep;
    protected String name;

    public PrimaryInstruction(String name,int deep) {
        this.deep = deep;
        this.name = name;
    }



    public static PrimaryOther createComment(String Comment,int deep) {
        return new PrimaryOther("#",deep,-1,Comment,(x,y,z)-> x + " " + z);
    }

    public static PrimaryOther createSyscall() {
        return new PrimaryOther("syscall",0,-1,null,(x,y,z)->x);
    }

    public static PrimaryOther createTag(String tag) {
        return new PrimaryOther(tag,0,-1,":",(x,y,z)->x + z);
    }

    public static PrimaryOther createInitStr(String tag) {
        return new PrimaryOther(tag,0,-1,"",(x,y,z)->x);
    }

    public static PrimaryOther createSpace(int num) {
        String s = "" + num;
        return new PrimaryOther(".space",0,-1,s,(x,y,z)->x + " " + z);
    }


    public static PrimaryOther createSegment(String tag,int deep) {
        return new PrimaryOther("Segment",deep,-1,tag,(x,y,z)->z);
    }


    public static PrimaryRegOffset createLui(int dst,int imm) {
        return new PrimaryRegOffset("lui",0,dst,imm);
    }

    public static PrimaryOther createJump(String tag) {
        return new PrimaryOther("j",0,-1,tag,(x,y,z)->x + " " + z);
    }

    public static PrimaryOther createLa(String tag,int dst) {
        return new PrimaryOther("la",0,dst,tag,(x,y,z)->x + " $" + y + "," + z);
    }

    public static PrimaryOther createJal(String tag) {
        return new PrimaryOther("jal",0,-1,tag,(name,dst,Atag)->name + " " + Atag);
    }

    public static PrimaryPureReg createJr(int dst) {
        return new PrimaryPureReg("jr",0,dst);
    }

    public static PrimaryBranch createBne(int src1,int src2,String tag) {
        return new PrimaryBranch("bne",0,src1,src2,tag);
    }

    public static PrimaryBranch createBnz(int src,String tag) {
        return new PrimaryBranch("bnez",0,src,tag);
    }

    public static PrimaryBranch createBeq(int src1,int src2,String tag) {
        return new PrimaryBranch("beq",0,src1,src2,tag);
    }

    public static PrimaryBranch createBeqz(int src1,String tag) {
        return new PrimaryBranch("beqz",0,src1,tag);
    }

    public static PrimaryBranch createBgez(int src1,String tag) {
        return new PrimaryBranch("bgez",0,src1,tag);
    }

    public static PrimaryBranch createBgtz(int src1,String tag) {
        return new PrimaryBranch("bgtz",0,src1,tag);
    }

    public static PrimaryBranch createBlez(int src1,String tag) {
        return new PrimaryBranch("blez",0,src1,tag);
    }

    public static PrimaryBranch createBltz(int src1,String tag) {
        return new PrimaryBranch("bltz",0,src1,tag);
    }

    public static PrimaryPureReg createAddu(int dst,int src1,int src2) {
        return new PrimaryPureReg("addu",0,dst,src1,src2);
    }

    public static PrimaryPureReg createAnd(int dst,int src1,int src2) {
        return new PrimaryPureReg("and",0,dst,src1,src2);
    }

    public static PrimaryPureReg createSeq(int dst,int src1,int src2) {
        return new PrimaryPureReg("seq",0,dst,src1,src2);
    }

    public static PrimaryPureReg createSne(int dst,int src1,int src2) {
        return new PrimaryPureReg("sne",0,dst,src1,src2);
    }

    public static PrimaryPureReg createSle(int dst,int src1,int src2) {
        return new PrimaryPureReg("sle",0,dst,src1,src2);
    }

    public static PrimaryPureReg createSge(int dst,int src1,int src2) {
        return new PrimaryPureReg("sge",0,dst,src1,src2);
    }

    public static PrimaryPureReg createSgt(int dst,int src1,int src2) {
        return new PrimaryPureReg("sgt",0,dst,src1,src2);
    }

    public static PrimaryPureReg createSubu(int dst,int src1,int src2) {
        return new PrimaryPureReg("subu",0,dst,src1,src2);
    }

    public static PrimaryPureReg createOr(int dst,int src1,int src2) {
        return new PrimaryPureReg("or",0,dst,src1,src2);
    }

    public static PrimaryPureReg createSlt(int dst,int src1,int src2) {
        return new PrimaryPureReg("slt",0,dst,src1,src2);
    }

    public static PrimaryPureReg createSltu(int dst,int src1,int src2) {
        return new PrimaryPureReg("sltu",0,dst,src1,src2);
    }

    public static PrimaryPureReg createXor(int dst,int src1,int src2) {
        return new PrimaryPureReg("xor",0,dst,src1,src2);
    }

    public static PrimaryRegOffset createAddiu(int dst,int src1,int imm) {
        return new PrimaryRegOffset("addiu",0,dst,src1,imm);
    }

    public static PrimaryRegOffset createAndi(int dst,int src1,int imm) {
        return new PrimaryRegOffset("andi",0,dst,src1,imm);
    }

    public static PrimaryRegOffset createSll(int dst,int src1,int imm) {
        return new PrimaryRegOffset("sll",0,dst,src1,imm);
    }

    public static PrimaryRegOffset createSrl(int dst,int src1,int imm) {
        return new PrimaryRegOffset("srl",0,dst,src1,imm);
    }

    public static PrimaryRegOffset createSra(int dst,int src1,int imm) {
        return new PrimaryRegOffset("sra",0,dst,src1,imm);
    }

    public static PrimaryRegOffset createSlti(int dst,int src1,int imm) {
        return new PrimaryRegOffset("slti",0,dst,src1,imm);
    }

    public static PrimaryRegOffset createSltiu(int dst,int src1,int imm) {
        return new PrimaryRegOffset("sltiu",0,dst,src1,imm);
    }

    public static PrimaryRegOffset createOri(int dst,int src1,int imm) {
        return new PrimaryRegOffset("ori",0,dst,src1,imm);
    }

    public static PrimaryRegOffset createXori(int dst,int src1,int imm) {
        return new PrimaryRegOffset("xori",0,dst,src1,imm);
    }

    public static PrimaryPureReg createSllv(int dst,int src1,int src2) {
        return new PrimaryPureReg("sllv",0,dst,src1,src2);
    }

    public static PrimaryPureReg createMult(int src1,int src2) {
        return new PrimaryPureReg("mult",0,src1,src2);
    }

    public static PrimaryPureReg createDiv(int src1,int src2) {
        return new PrimaryPureReg("div",0,src1,src2);
    }

    public static PrimaryPureReg createMfhi(int src1) {
        return new PrimaryPureReg("mfhi",0,src1);
    }

    public static PrimaryPureReg createMflo(int src1) {
        return new PrimaryPureReg("mflo",0,src1);
    }

    public static PrimaryMemory createLw(int dst,int offset,int addr) {
        return new PrimaryMemory("lw",0,dst,offset,addr);
    }

    public static PrimaryMemory createSw(int dst,int offset,int addr) {
        return new PrimaryMemory("sw",0,dst,offset,addr);
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }

    public int getDeep() {
        return deep;
    }

    public String getName() {
        return name;
    }
}
