package SymbolTables;

import IntermediateCode.Operands.VariableOperand;

import java.util.ArrayList;

public class FormalArrayTuple extends PrimaryTuple {
    private String VarType;
    private int Dimension;
    private ArrayList<Integer> PerDimensionSize;
    private int OccupySize;
    private boolean isConst;

    public FormalArrayTuple(String identName, int offset, String varType, boolean isConst) {
        super(identName, offset);
        this.VarType = varType;
        this.Dimension = 0;
        this.PerDimensionSize = new ArrayList<>();
        this.OccupySize = 4;
        this.isConst = isConst;
    }

    public int getDimension() {
        return Dimension;
    }

    public void SetPerDimensionSize(int number) {
        PerDimensionSize.add(number);
        this.Dimension++;
    }

    public ArrayList<Integer> getPerDimensionSize() {
        return PerDimensionSize;
    }

    @Override
    public boolean isConst() {
        return isConst;
    }
}
