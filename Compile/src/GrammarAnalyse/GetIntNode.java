package GrammarAnalyse;


import IntermediateCode.Elements.OtherElements;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.WordProvider;
import WordAnalyse.Word;

public class GetIntNode extends TreeNode{
    //这是一个虚拟非终结点
    private LvalNode lvalChildren;
    public GetIntNode(TreeNode Father,int deep) {
        super(Father,deep);
        lvalChildren = null;
    }

    public void setLvalChildren(LvalNode lvalChildren) {
        this.lvalChildren = lvalChildren;
    }


    @Override
    public void treePrint(StringBuilder stringBuilder) {
        lvalChildren.treePrint(stringBuilder);
        stringBuilder.append(" = getint();");
    }

    @Override
    public Word getThisNodeLine() {
        return lvalChildren.getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        /* no need */
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        VariableOperand temp_dst = intermediateBuilder.putTempVariableAndReturn();
        intermediateBuilder.AddIntermediateExpression(OtherElements.createScanElement(temp_dst));
        lvalChildren.UpdateValueForIntermediate(temp_dst,intermediateBuilder,symbolTable);
    }

    public void RunGetIntParser() {
        LvalNode lvalNode = new LvalNode(this,printDeep);
        setLvalChildren(lvalNode);
        lvalNode.RunLvalParser();
        WordProvider.CheckEndSign(null,this.getClass(),-1,"ASSIGN");
        WordProvider.CheckEndSign(null,this.getClass(),-1,"GETINTTK");
    }
}
