package GrammarAnalyse;


import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

public class ExpNode extends TreeNode{
    private AddExpNode children;
    public ExpNode(TreeNode Father,int deep) {
        super(Father,deep);
        children = null;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        children.treePrint(stringBuilder);
    }

    @Override
    public Word getThisNodeLine() {
        return children.getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        children.RunSymbolBuilder(symbolTable,tuples);
        ChangeNodeTypeAndDim(children);
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        children.ToIntermediate(intermediateBuilder,symbolTable);
        dst = children.getDst();
    }

    public void setChildren(AddExpNode children) {
        this.children = children;
    }

    public void setFather(TreeNode Father) {
        this.Father = Father;
    }
    public AddExpNode getChildren() {
        return children;
    }

    public void RunExpParser() {
        AddExpNode addExpNode = new AddExpNode(this,printDeep);
        setChildren(addExpNode);
        addExpNode.RunAddExpParser();
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<Exp>");
    }

    public int calculateValue(SymbolTable CalculateNeededTable) {
        return children.calculateValue(CalculateNeededTable);
    }
}
