package GrammarAnalyse;



import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.ErrorMessage;
import Tools.WordProvider;
import WordAnalyse.Word;

public class DeclNode extends TreeNode{
    TreeNode children;
    public DeclNode(TreeNode Father,int deep) {
        super(Father,deep);
        children = null;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        //stringBuilder.append(deepToPrint(printDeep));
        children.treePrint(stringBuilder);
    }

    @Override
    public Word getThisNodeLine() {
        return children.getThisNodeLine();
    }

    public void setChildren(TreeNode children) {
        this.children = children;
    }

    public void RunDeclParser() {
        Word word = WordProvider.GetNextWord();
        if (word != null) {
            if (word.getCategoryCode().equals("CONSTTK")) {
                WordProvider.RollBackWord(1);
                ConstDeclNode constDeclNode = new ConstDeclNode(this,printDeep);
                setChildren(constDeclNode);
                constDeclNode.RunConstDeclParser();
            } else {
                WordProvider.RollBackWord(1);
                VarDeclNode varDeclNode = new VarDeclNode(this,printDeep);
                setChildren(varDeclNode);
                varDeclNode.RunVarDeclParser();
            }
        } else {
            ErrorMessage.handleError(this.getClass(),-1,null);
        }
        /*if (GlobalSetting.FileGrammarAnalyzerResult) {
            WordProvider.FileWaitPrintQueue();
            WordProvider.WriteBuffer.add("<Decl>");
        }
        if (GlobalSetting.PrintGrammarAnalyzerResult) {
            WordProvider.ClearAndPrintWaitPrintQueue();
            System.out.println("<Decl>");
        }*/
    }

    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuple) {
        children.RunSymbolBuilder(symbolTable,null);
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        children.ToIntermediate(intermediateBuilder,symbolTable);
    }
}
