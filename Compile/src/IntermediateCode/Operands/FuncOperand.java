package IntermediateCode.Operands;

import java.util.ArrayList;

public class FuncOperand extends PrimaryOperand{
    private ArrayList<PrimaryOperand> FormalParams;
    private PrimaryOperand RealReturnVariable;
    private PrimaryOperand FormalReturnVariable;
    private int space;
    private boolean isMain;

    public boolean isMain() {
        return isMain;
    }

    public void ReplaceFormalParams(PrimaryOperand OldFormalParam,PrimaryOperand NewFormalParam) {
        if (FormalParams.contains(OldFormalParam)) {
            FormalParams.set(FormalParams.indexOf(OldFormalParam),NewFormalParam);
        }
    }


    public void setFormalReturnVariable(PrimaryOperand formalReturnVariable) {
        FormalReturnVariable = formalReturnVariable;
    }

    public PrimaryOperand getFormalReturnVariable() {
        return FormalReturnVariable;
    }

    public FuncOperand(String name, ArrayList<PrimaryOperand> FormalParams, PrimaryOperand RealReturnVariable, boolean isMain) {
        super(name);
        this.FormalParams = FormalParams;
        this.RealReturnVariable = RealReturnVariable;
        this.isMain = isMain;
    }

    public FuncOperand(String name) {
        super(name);
        this.FormalParams = new ArrayList<>();
        this.RealReturnVariable = null;
    }


    public void setRealReturnVariable(PrimaryOperand src) {
        this.RealReturnVariable = src;
    }

    public PrimaryOperand getRealReturnVariable() {
        return RealReturnVariable;
    }

    public ArrayList<PrimaryOperand> getFormalParams() {
        return FormalParams;
    }

    public void setFormalParams(ArrayList<PrimaryOperand> formalParams) {
        FormalParams = formalParams;
    }

    public void AddFormalParams(VariableOperand variableOperand) {
        FormalParams.add(variableOperand);
    }

    @Override
    public int hashCode() {
        return this.OperandName.hashCode();
    }

    @Override
    public int getSpace() {
        return space;
    }

    public void setSpace(int space) {
        this.space = space;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FuncOperand)) {
            return false;
        } else {
            return this.OperandName.equals(((FuncOperand) obj).OperandName);
        }
    }
}
