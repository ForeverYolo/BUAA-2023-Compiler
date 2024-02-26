package IntermediateCode.Elements;

import IntermediateCode.Container.PrimaryBlock;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import ObjectCode.Assembly;

import java.util.ArrayList;

public abstract class PrimaryElement {
    protected String operatorName;
    protected PrimaryBlock normalBlock;
    protected int normalLineIndex;

    public int getNormalLineIndex() {
        return normalLineIndex;
    }

    public PrimaryElement(String name) {
        this.operatorName = name;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public abstract void setPlaceMessage(PrimaryBlock block,int Index);

    public abstract void setAddress(int address);
    public abstract int getSpace();
    public abstract void ToAssembly(Assembly assembly);
    public abstract ArrayList<VariableOperand> getUsedVariable();
    public abstract ArrayList<PrimaryOperand> getUsed();
    public abstract ArrayList<VariableOperand> getDefineVariable();
    public abstract ArrayList<PrimaryOperand> getDefine();
    public abstract void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target);
    public abstract void ReplaceUseVariable(PrimaryOperand operand,PrimaryOperand target);
}
