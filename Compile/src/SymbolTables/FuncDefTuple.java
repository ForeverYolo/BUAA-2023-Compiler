package SymbolTables;

import GrammarAnalyse.FuncDefNode;
import IntermediateCode.Operands.PrimaryOperand;

import java.util.ArrayList;

public class FuncDefTuple extends PrimaryTuple{
    private int FuncFParamsCount;
    private ArrayList<PrimaryTuple> FuncFParamsType;
    private String returnType;
    private boolean isConst;
    private PrimaryOperand FormalReturnOperand;
    private PrimaryOperand RealReturnOperand;
    private String FuncType;
    private final FuncDefNode funcDefNode;

    public FuncDefTuple(String identName, int offset,String FuncType, FuncDefNode funcDefNode) {
        super(identName, offset);
        FuncFParamsType = new ArrayList<>();
        FuncFParamsCount = 0;
        this.FuncType = FuncType;
        this.funcDefNode = funcDefNode;
    }

    public FuncDefNode getFuncDefNode() {
        return funcDefNode;
    }

    public PrimaryOperand getFormalReturnOperand() {
        return FormalReturnOperand;
    }

    public void setFormalReturnOperand(PrimaryOperand returnOperand) {
        this.FormalReturnOperand = returnOperand;
    }

    public PrimaryOperand getRealReturnOperand() {
        return RealReturnOperand;
    }

    public void setRealReturnOperand(PrimaryOperand realReturnOperand) {
        RealReturnOperand = realReturnOperand;
    }

    @Override
    public boolean isConst() {
        return isConst;
    }

    public ArrayList<PrimaryTuple> getFuncFParamsType() {
        return FuncFParamsType;
    }

    public void AddFuncFParams(PrimaryTuple define) {
        FuncFParamsType.add(define);
        FuncFParamsCount++;
    }

    public int getFuncFParamsCount() {
        return FuncFParamsCount;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getFuncType() {
        return FuncType;
    }
}
