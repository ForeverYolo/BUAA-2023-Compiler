package SymbolTables;

import IntermediateCode.Operands.VariableOperand;

import java.util.ArrayList;

public class RealArrayTuple extends PrimaryTuple{
    private String VarType;
    private int Dimension;
    private ArrayList<Integer> PerDimensionSize;
    private ArrayList<Integer> ValueArray;
    private int OccupySize;
    private boolean IsConst;

    public RealArrayTuple(String identName, int offset, String varType, boolean isConst) {
        super(identName, offset);
        this.VarType = varType;
        this.Dimension = 0;
        this.PerDimensionSize = new ArrayList<>();
        this.IsConst = isConst;
        this.ValueArray = new ArrayList<>();
    }

    public void SetPerDimensionSize(int number) {
        PerDimensionSize.add(number);
        this.Dimension++;
    }

    public void addValueArray(int value) {
        ValueArray.add(value);
    }

    public int CalculateOccupySize() {
        int result = 4;
        for (int size:PerDimensionSize) {
            result = result * size;
        }
        OccupySize = result;
        return result;
    }

    public int getElementNumber() {
        int result = 1;
        for (int num:PerDimensionSize) {
            result = result * num;
        }
        return result;
    }

    public int getDimension() {
        return Dimension;
    }

    public int queryArrayValue(ArrayList<Integer> index) {
        if (this.Dimension == 1) {
            int target = index.get(0);
            return ValueArray.size() > target ? ValueArray.get(target) : 0;
        } else if(this.Dimension == 2) {
            int target = index.get(0)*PerDimensionSize.get(1) + index.get(1);
            return ValueArray.size() > target ? ValueArray.get(target) : 0;
        } else {
            int target = index.get(0)*PerDimensionSize.get(1)*PerDimensionSize.get(2) + index.get(1)*PerDimensionSize.get(2) + index.get(2);
            return ValueArray.size() > target ? ValueArray.get(target) : 0;
        }
    }

    public ArrayList<Integer> getValueArray() {
        return ValueArray;
    }

    public ArrayList<Integer> getPerDimensionSize() {
        return PerDimensionSize;
    }

    @Override
    public boolean isConst() {
        return this.IsConst;
    }
}
