package GrammarAnalyse;

import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.ErrorMessage;
import Tools.WordProvider;
import WordAnalyse.Word;

public class BtypeNode extends TreeNode{
    private Word type;
    public BtypeNode(TreeNode Father,int deep) {
        super(Father,deep);
    }
    @Override
    public void treePrint(StringBuilder stringBuilder) {
        stringBuilder.append(type.OriginWord);
    }

    @Override
    public Word getThisNodeLine() {
        return type;
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        /* No Implement */
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder,SymbolTable symbolTable) {
        /* No Implement */
    }

    public void RunBTypeParser() {
        type = WordProvider.CheckEndSign(null,this.getClass(),-1,"INTTK");
        /*if (GlobalSetting.FileGrammarAnalyzerResult) {
            WordProvider.FileWaitPrintQueue();
            WordProvider.WriteBuffer.add("<BType>");
        }
        if (GlobalSetting.PrintGrammarAnalyzerResult) {
            WordProvider.ClearAndPrintWaitPrintQueue();
            System.out.println("<BType>");
        }*/
    }
}
