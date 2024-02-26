package GrammarAnalyse;

import IntermediateCode.IntermediateBuilder;
import SymbolTables.FuncDefTuple;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

public class FuncTypeNode extends TreeNode{
    private Word funcType;

    public FuncTypeNode(TreeNode Father,int deep) {
        super(Father,deep);
    }

    public void setFuncType(Word funcType) {
        this.funcType = funcType;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        stringBuilder.append(funcType.OriginWord);
    }

    @Override
    public Word getThisNodeLine() {
        return funcType;
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        FuncDefTuple funcDefTuple = (FuncDefTuple) tuples;
        funcDefTuple.setReturnType(funcType.OriginWord);
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        /* No Implement */
    }

    public void RunFuncTypeParser() {
        setFuncType(WordProvider.CheckEndSign(null,this.getClass(),-1,"INTTK","VOIDTK"));
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<FuncType>");
    }

    public Word getType() {
        return funcType;
    }
}
