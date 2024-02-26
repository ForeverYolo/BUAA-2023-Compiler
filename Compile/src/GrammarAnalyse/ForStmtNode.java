package GrammarAnalyse;


import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

public class ForStmtNode extends TreeNode{
    private LvalNode lvalChildren;
    private ExpNode expChildren;
    public ForStmtNode(TreeNode Father,int deep) {
        super(Father,deep);
        lvalChildren = null;
        expChildren = null;
    }

    public void setExpChildren(ExpNode expChildren) {
        this.expChildren = expChildren;
    }

    public void setLvalChildren(LvalNode lvalChildren) {
        this.lvalChildren = lvalChildren;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        lvalChildren.treePrint(stringBuilder);
        stringBuilder.append(" = ");
        expChildren.treePrint(stringBuilder);
    }

    @Override
    public Word getThisNodeLine() {
        return lvalChildren.getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        lvalChildren.RunSymbolBuilder(symbolTable,null);
        expChildren.RunSymbolBuilder(symbolTable,null);
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        expChildren.ToIntermediate(intermediateBuilder,symbolTable);
        VariableOperand variableOperand = (VariableOperand) expChildren.getDst();
        lvalChildren.UpdateValueForIntermediate(variableOperand,intermediateBuilder,symbolTable);
    }

    public void RunForStmtParser() {
        LvalNode lvalNode = new LvalNode(this,printDeep);
        lvalNode.RunLvalParser();
        setLvalChildren(lvalNode);
        WordProvider.CheckEndSign(null,this.getClass(),-1,"ASSIGN");
        ExpNode expNode = new ExpNode(this,printDeep);
        expNode.RunExpParser();
        setExpChildren(expNode);
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<ForStmt>");

    }
}
