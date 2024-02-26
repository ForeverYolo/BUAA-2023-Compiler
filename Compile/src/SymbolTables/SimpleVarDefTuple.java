package SymbolTables;

public class SimpleVarDefTuple extends PrimaryTuple{
    private String VarType;
    private int VarSize;
    private boolean IsConst;
    private int Value;

    public SimpleVarDefTuple(String identName,int offset,String varType,boolean isConst) {
        super(identName,offset);
        this.VarType = varType;
        this.VarSize = 4;
        this.IsConst = isConst;
        this.Value = 0;
    }

    public void setConstVarValue(int varValue) {
        this.Value = varValue;
    }

    public int queryConstVarValue() {
        return Value;
    }


    @Override
    public boolean isConst() {
        return this.IsConst;
    }

    public String getVarType() {
        return VarType;
    }
}
