package GrammarAnalyse;


import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class VarDeclNode extends TreeNode{
    private BtypeNode btypeChildren;
    private final ArrayList<VarDefNode> varDefChildren;
    public VarDeclNode(TreeNode Father,int deep) {
        super(Father,deep);
        varDefChildren = new ArrayList<>();
        btypeChildren = null;
    }
    public void setBtypeChildren(BtypeNode btypeChildren) {
        this.btypeChildren = btypeChildren;
    }

    public void addVarDefChildren(VarDefNode varDefChildren) {
        this.varDefChildren.add(varDefChildren);
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        btypeChildren.treePrint(stringBuilder);
        stringBuilder.append(" ");
        int count = 0;
        for (VarDefNode varDefNode : varDefChildren) {
            if (count == 0) {
                varDefNode.treePrint(stringBuilder);
            } else {
                stringBuilder.append(",");
                varDefNode.treePrint(stringBuilder);
            }
            count++;
        }
        stringBuilder.append(";\n");
    }

    @Override
    public Word getThisNodeLine() {
        return btypeChildren.getThisNodeLine();
    }

    public void RunVarDeclParser() {
        BtypeNode btypeNode = new BtypeNode(this,printDeep);
        setBtypeChildren(btypeNode);
        btypeNode.RunBTypeParser();

        VarDefNode varDefNode = new VarDefNode(this,printDeep);
        addVarDefChildren(varDefNode);
        varDefNode.RunVarDefParserParser();
        while(true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && word.getCategoryCode().equals("COMMA")) {
                VarDefNode varDefNode1 = new VarDefNode(this,printDeep);
                addVarDefChildren(varDefNode1);
                varDefNode1.RunVarDefParserParser();
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
        WordProvider.CheckEndSign(varDefChildren.get(varDefChildren.size()-1).getThisNodeLine()
                ,this.getClass(),0,"SEMICN");
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<VarDecl>");
    }

    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuple) {
        for(VarDefNode varDefNode:varDefChildren) {
            varDefNode.RunSymbolBuilder(symbolTable,null);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        for (VarDefNode varDefNode : varDefChildren) {
            varDefNode.ToIntermediate(intermediateBuilder,symbolTable);
        }
    }
}
