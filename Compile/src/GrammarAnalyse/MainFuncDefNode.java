package GrammarAnalyse;


import IntermediateCode.IntermediateBuilder;
import SymbolTables.FuncDefTuple;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.ErrorMessage;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class MainFuncDefNode extends TreeNode{
    private FuncDefTuple funcDefTuple;
    private Word INTTKWord;
    private SymbolTable symbolTable;
    private BlockNode blockNode;
    public MainFuncDefNode(TreeNode Father,int deep) {
        super(Father,deep);
        blockNode = null;
        INTTKWord = null;
    }

    public void setBlockNode(BlockNode blockNode) {
        this.blockNode = blockNode;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        stringBuilder.append("int main()\n");
        blockNode.treePrint(stringBuilder);
    }

    @Override
    public Word getThisNodeLine() {
        return INTTKWord;
    }

    public FuncDefTuple getFuncDefTuple() {
        return funcDefTuple;
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        FuncDefTuple funcDefTuple = new FuncDefTuple("main",0,"int",null);
        this.funcDefTuple = funcDefTuple;
        funcDefTuple.setReturnType("int");
        symbolTable.addTuple("main",funcDefTuple);
        this.symbolTable = new SymbolTable(symbolTable);
        symbolTable.AddChildren(this.symbolTable);
        blockNode.RunSymbolBuilder(this.symbolTable,tuples);
        ReturnNode returnNode = blockNode.SearchReturnNode();
        if (returnNode == null) {
            ErrorMessage.handleError(this.getClass(),0,blockNode.getEndLBraceWord());
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        intermediateBuilder.putFuncOperandAndReturn("main_func",new ArrayList<>(),intermediateBuilder.putReturnVariableAndReturn(false),true);
        blockNode.ToIntermediate(intermediateBuilder,this.symbolTable);
    }

    public void RunMainFuncDefParser() {
        INTTKWord = WordProvider.CheckEndSign(null,this.getClass(),-1,"INTTK");
        WordProvider.CheckEndSign(null,this.getClass(),-1,"MAINTK");
        WordProvider.CheckEndSign(null,this.getClass(),-1,"LPARENT");
        WordProvider.CheckEndSign(this.getThisNodeLine(),this.getClass(),1,"RPARENT");
        BlockNode blockNode = new BlockNode(this,printDeep + 1);
        blockNode.RunBlockParser();
        setBlockNode(blockNode);
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<MainFuncDef>");
    }


}
