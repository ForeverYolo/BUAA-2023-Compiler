package GrammarAnalyse;

import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

public class ConstExpNode extends TreeNode{
    AddExpNode addExpChildren;

    public ConstExpNode(TreeNode Father,int deep) {
        super(Father,deep);
        addExpChildren = null;
    }

    public void setAddExpChildren(AddExpNode children) {
        addExpChildren = children;
    }

    public AddExpNode getAddExpChildren() {
        return addExpChildren;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        addExpChildren.treePrint(stringBuilder);
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        addExpChildren.RunSymbolBuilder(symbolTable,null);
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        addExpChildren.ToIntermediate(intermediateBuilder,symbolTable);
        dst = addExpChildren.getDst();
    }

    public void RunConstExpParser() {
        AddExpNode addExpNode = new AddExpNode(this,printDeep);
        setAddExpChildren(addExpNode);
        addExpNode.RunAddExpParser();
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<ConstExp>");
    }

    public int calculateValue(SymbolTable CalculateNeededTable) {
        return addExpChildren.calculateValue(CalculateNeededTable);
    }

    public Word getThisNodeLine() {
        return addExpChildren.getThisNodeLine();
    }
}
