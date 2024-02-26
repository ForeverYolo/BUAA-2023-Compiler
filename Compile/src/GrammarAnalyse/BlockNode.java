package GrammarAnalyse;


import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class BlockNode extends TreeNode{
    private Word EndLBraceWord;
    private Word NodeBeginWord;
    private SymbolTable symbolTable;
    private ArrayList<BlockItemNode> blockItemChildren;
    public BlockNode(TreeNode Father,int deep) {
        super(Father,deep);
        blockItemChildren = new ArrayList<>();
        symbolTable = null;
        NodeBeginWord = null;
        EndLBraceWord = null;
    }

    public void AddBlockItemChildren(BlockItemNode blockItemChildren) {
        this.blockItemChildren.add(blockItemChildren);
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        deleteTab(stringBuilder);
        stringBuilder.append(deepToPrint(printDeep - 1));
        stringBuilder.append("{\n");
        for(BlockItemNode blockItemNode:blockItemChildren) {
            stringBuilder.append(deepToPrint(printDeep));
            blockItemNode.treePrint(stringBuilder);
        }
        stringBuilder.append(deepToPrint(printDeep - 1));
        if (this.Father instanceof StmtNode) {
            stringBuilder.append("}");
        } else {
            stringBuilder.append("}\n");
        }
    }

    @Override
    public Word getThisNodeLine() {
        return NodeBeginWord;
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        if (!(Father instanceof FuncDefNode) && !(Father instanceof MainFuncDefNode)) {
            this.symbolTable = new SymbolTable(symbolTable);
            symbolTable.AddChildren(this.symbolTable);
        } else  {
            this.symbolTable = symbolTable;
        }
        for(BlockItemNode blockItemNode:blockItemChildren) {
            blockItemNode.RunSymbolBuilder(this.symbolTable,null);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder,SymbolTable symbolTable) {
        dst = null;
        for(BlockItemNode blockItemNode:blockItemChildren) {
            blockItemNode.ToIntermediate(intermediateBuilder,this.symbolTable);
        }
    }

    public Word getEndLBraceWord() {
        return EndLBraceWord;
    }

    public ReturnNode SearchReturnNode() {
        for(BlockItemNode blockItemNode:blockItemChildren) {
            ReturnNode returnNode = blockItemNode.SearchReturnNode();
            if (returnNode != null) {
                return returnNode;
            }
        }
        return null;
    }

    public void RunBlockParser() {
        NodeBeginWord = WordProvider.CheckEndSign(null,this.getClass(),-1,"LBRACE");
        while (true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && !word.getCategoryCode().equals("RBRACE")) {
                WordProvider.RollBackWord(1);
                BlockItemNode blockItemNode = new BlockItemNode(this,printDeep);
                AddBlockItemChildren(blockItemNode);
                blockItemNode.RunBlockItemParser();
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
        EndLBraceWord = WordProvider.CheckEndSign(null,this.getClass(),-1,"RBRACE");
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<Block>");
    }


}
