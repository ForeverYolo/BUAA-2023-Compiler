package GrammarAnalyse;


import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.ErrorMessage;
import Tools.WordProvider;
import WordAnalyse.Word;

public class StmtLvalExpNode extends TreeNode{
    //对应stmt规则一 这是一个虚拟的非终结点
    private LvalNode lvalChildren;
    private ExpNode expChilren;

    public StmtLvalExpNode(TreeNode Father,int deep) {
        super(Father,deep);
        lvalChildren = null;
        expChilren = null;
    }

    public void setLvalChildren(LvalNode lvalChildren) {
        this.lvalChildren = lvalChildren;
    }

    public void setExpChilren(ExpNode expChilren) {
        this.expChilren = expChilren;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        lvalChildren.treePrint(stringBuilder);
        stringBuilder.append(" = ");
        expChilren.treePrint(stringBuilder);
        stringBuilder.append(";");
    }

    @Override
    public Word getThisNodeLine() {
        return lvalChildren.getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        PrimaryTuple primaryTuple = symbolTable.queryTupleFromAllRelativeSymbolTable(lvalChildren.getIdent().OriginWord,false);
        if (primaryTuple != null && primaryTuple.isConst()) {
            ErrorMessage.handleError(this.getClass(),1,lvalChildren.getThisNodeLine());
        }
        lvalChildren.RunSymbolBuilder(symbolTable,tuples);
        expChilren.RunSymbolBuilder(symbolTable,tuples);
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        expChilren.ToIntermediate(intermediateBuilder,symbolTable);
        VariableOperand variableOperand = (VariableOperand) expChilren.getDst();
        lvalChildren.UpdateValueForIntermediate(variableOperand,intermediateBuilder,symbolTable);
    }

    public void RunStmtLvalExpParser() {
        LvalNode lvalNode = new LvalNode(this,printDeep);
        setLvalChildren(lvalNode);
        lvalNode.RunLvalParser();
        WordProvider.CheckEndSign(null,this.getClass(),-1,"ASSIGN");
        ExpNode expNode = new ExpNode(this,printDeep);
        setExpChilren(expNode);
        expNode.RunExpParser();
    }
}
