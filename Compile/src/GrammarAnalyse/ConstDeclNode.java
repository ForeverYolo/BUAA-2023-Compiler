package GrammarAnalyse;


import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class ConstDeclNode extends TreeNode{
    private Word ConstTK;
    private BtypeNode btypeChildren;
    private final ArrayList<ConstDefNode> constDefChildren;
    public ConstDeclNode(TreeNode Father,int deep) {
        super(Father,deep);
        constDefChildren = new ArrayList<>();
        ConstTK = null;
    }

    public void setBtypeChildren(BtypeNode btypeChildren) {
        this.btypeChildren = btypeChildren;
    }

    public void AddConstDefChildren(ConstDefNode children) {
        this.constDefChildren.add(children);
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        stringBuilder.append("const ");
        btypeChildren.treePrint(stringBuilder);
        stringBuilder.append(" ");
        int count = 0;
        for (ConstDefNode constDefNode:constDefChildren) {
            if (count != 0) {
                stringBuilder.append(",");
            }
            constDefNode.treePrint(stringBuilder);
            count++;
        }
        stringBuilder.append(";\n");
    }

    @Override
    public Word getThisNodeLine() {
        return ConstTK;
    }

    public void RunConstDeclParser() {
        ConstTK = WordProvider.CheckEndSign(null,this.getClass(),-1,"CONSTTK");
        BtypeNode btypeNode = new BtypeNode(this,printDeep);
        setBtypeChildren(btypeNode);
        btypeNode.RunBTypeParser();

        ConstDefNode constDefNode = new ConstDefNode(this,printDeep);
        AddConstDefChildren(constDefNode);
        constDefNode.RunConstDefParser();

        while (true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && word.getCategoryCode().equals("COMMA")) {
                ConstDefNode constDefNode1 = new ConstDefNode(this,printDeep);
                AddConstDefChildren(constDefNode1);
                constDefNode1.RunConstDefParser();
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }

        WordProvider.CheckEndSign(constDefChildren.get(constDefChildren.size()-1).getThisNodeLine()
                ,this.getClass(),0, "SEMICN");
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<ConstDecl>");
    }

    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuple) {
        for(ConstDefNode constDefNode:constDefChildren) {
            constDefNode.RunSymbolBuilder(symbolTable,null);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder,SymbolTable symbolTable) {
        for(ConstDefNode constDefNode:constDefChildren) {
            constDefNode.ToIntermediate(intermediateBuilder,symbolTable);
        }
        this.dst = null;
    }
}
