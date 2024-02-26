package GrammarAnalyse;


import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.ErrorMessage;
import Tools.WordProvider;
import WordAnalyse.Word;

public class PrimaryExpNode extends TreeNode {
    private TreeNode children;
    public PrimaryExpNode(TreeNode Father,int deep) {
        super(Father,deep);
        children = null;
    }

    public void setChildren(TreeNode children) {
        this.children = children;
    }

    public TreeNode getChildren() {
        return children;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        if (children instanceof ExpNode) {
            stringBuilder.append("(");
            children.treePrint(stringBuilder);
            stringBuilder.append(")");
        } else {
            children.treePrint(stringBuilder);
        }
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
        this.dst = children.getDst();
    }

    public void RunPrimaryExpParser() {
        Word word = WordProvider.GetNextWord();
        if (word != null && word.getCategoryCode().equals("LPARENT")) {
            ExpNode expNode = new ExpNode(this,printDeep);
            setChildren(expNode);
            expNode.RunExpParser();
            WordProvider.CheckEndSign(expNode.getThisNodeLine(),this.getClass(),0,"RPARENT");
        } else if (word != null && word.getCategoryCode().equals("IDENFR")) {
            WordProvider.RollBackWord(1);
            LvalNode lvalNode = new LvalNode(this,printDeep);
            setChildren(lvalNode);
            lvalNode.RunLvalParser();
        } else if (word != null && word.getCategoryCode().equals("INTCON")) {
            WordProvider.RollBackWord(1);
            NumberNode numberNode = new NumberNode(this,printDeep);
            setChildren(numberNode);
            numberNode.RunNumberParserParser();
        } else {
            ErrorMessage.handleError(this.getClass(),-1,null);
        }
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<PrimaryExp>");
    }

    public int calculateValue(SymbolTable CalculateNeededTable) {
        if(children instanceof ExpNode) {
            return ((ExpNode)children).calculateValue(CalculateNeededTable);
        } else if (children instanceof LvalNode) {
            return ((LvalNode)children).calculateValue(CalculateNeededTable);
        } else {
            return ((NumberNode)children).calculateValue();
        }
    }
}
