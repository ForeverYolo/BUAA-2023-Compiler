package SymbolTables;

import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;

import java.util.Optional;

public abstract class PrimaryTuple {
    private String identName;
    private int Offset;
    private PrimaryOperand primaryOperand;
    private boolean Vaild;

    public PrimaryOperand getPrimaryOperand() {
        return primaryOperand;
    }

    public void setPrimaryOperand(PrimaryOperand primaryOperand) {
        this.primaryOperand = primaryOperand;
        this.Vaild = true;
    }

    public PrimaryTuple(String identName, int offset) {
        this.identName = identName;
        this.Offset = offset;
        this.Vaild = true;
    }

    public boolean isVaild() {
        return Vaild;
    }

    public void setVaild(boolean vaild) {
        Vaild = vaild;
    }

    public String getIdentName() {
        return identName;
    }

    public abstract boolean isConst();
}
