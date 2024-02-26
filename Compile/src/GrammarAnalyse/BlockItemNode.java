package GrammarAnalyse;


import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

public class BlockItemNode extends TreeNode{
    private TreeNode children;
    public BlockItemNode(TreeNode Father,int deep) {
        super(Father,deep);
        children = null;
    }

    public void setTreeNode(TreeNode treeNode) {
        this.children = treeNode;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        children.treePrint(stringBuilder);
    }

    @Override
    public Word getThisNodeLine() {
        return children.getThisNodeLine();
    }

    public void RunBlockItemParser() {
        Word word = WordProvider.GetNextWord();
        if (word != null && (word.getCategoryCode().equals("INTTK") || word.getCategoryCode().equals("CONSTTK"))) {
            WordProvider.RollBackWord(1);
            DeclNode declNode = new DeclNode(this,printDeep);
            this.setTreeNode(declNode);
            declNode.RunDeclParser();
        } else {
            WordProvider.RollBackWord(1);
            StmtNode stmtNode = new StmtNode(this,printDeep);
            this.setTreeNode(stmtNode);
            stmtNode.RunStmtParser();
        }
        /*if (GlobalSetting.FileGrammarAnalyzerResult) {
            WordProvider.FileWaitPrintQueue();
            WordProvider.WriteBuffer.add("<BlockItem>");
        }
        if (GlobalSetting.PrintGrammarAnalyzerResult) {
            WordProvider.ClearAndPrintWaitPrintQueue();
            System.out.println("<BlockItem>");
        }*/
    }

    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuple) {
        children.RunSymbolBuilder(symbolTable,null);
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        dst = null;
        children.ToIntermediate(intermediateBuilder,symbolTable);
    }

    public ReturnNode SearchReturnNode() {
        if (children instanceof StmtNode) {
            return ((StmtNode)children).SearchReturnNode();
        } else {
            return null;
        }
    }
}
