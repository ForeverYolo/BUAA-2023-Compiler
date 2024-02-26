package IntermediateCode.Operands;

public class ConstValue extends PrimaryOperand {
    private int Value;

    public ConstValue(int Value) {
        super(TypeToString.get(VariableType.CONST) + Value);
        this.Value = Value;
    }

    @Override
    public int getSpace() {
        return 0;
    }

    public int getValue() {
        return Value;
    }
}
