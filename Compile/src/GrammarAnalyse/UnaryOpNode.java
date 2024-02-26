package GrammarAnalyse;

import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

public class UnaryOpNode extends TreeNode{
    private Word unaryOp;
    public UnaryOpNode(TreeNode Father,int deep) {
        super(Father,deep);
        unaryOp = null;
    }

    public Word getUnaryOp() {
        return unaryOp;
    }

    public void setUnaryOp(Word word) {
        this.unaryOp = word;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        stringBuilder.append(unaryOp.OriginWord);
    }

    @Override
    public Word getThisNodeLine() {
        return unaryOp;
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        /* no need*/
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        /* no need */
    }

    public void RunUnaryOpParser() {
        setUnaryOp(WordProvider.CheckEndSign(null,this.getClass(),-1,"PLUS","MINU","NOT"));
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<UnaryOp>");
    }
}
