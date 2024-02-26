package GrammarAnalyse;


import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

public class CondNode extends TreeNode{
    private LOrExpNode lOrExpChildren;
    public CondNode(TreeNode Father,int deep) {
        super(Father,deep);
        lOrExpChildren = null;
    }
    public void setlOrExpChildren(LOrExpNode children) {
        lOrExpChildren = children;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        lOrExpChildren.treePrint(stringBuilder);
    }

    @Override
    public Word getThisNodeLine() {
        return lOrExpChildren.getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        lOrExpChildren.RunSymbolBuilder(symbolTable,null);
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder,SymbolTable symbolTable) {
        lOrExpChildren.ToIntermediate(intermediateBuilder,symbolTable);
        dst = lOrExpChildren.dst;
    }

    public void RunCondParser() {
        LOrExpNode lOrExpNode = new LOrExpNode(this,printDeep);
        setlOrExpChildren(lOrExpNode);
        lOrExpNode.RunLOrExpParser();
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<Cond>");
    }
}
