package ObjectCode.Instruction;

public class PrimaryOther extends PrimaryInstruction{

    @FunctionalInterface
    protected interface DisplayAction {
        String accept(String name, int dst,String tag);
    }

    protected String tag;
    protected int dst;
    private final DisplayAction displayAction;

    public PrimaryOther(String name, int deep,int dst,String tag,DisplayAction displayAction) {
        super(name, deep);
        this.tag = tag;
        this.dst = dst;
        this.displayAction = displayAction;
    }

    @Override
    public String toString() {
        return displayAction.accept(name,dst,tag);
    }
}
