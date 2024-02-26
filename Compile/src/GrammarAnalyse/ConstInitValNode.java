package GrammarAnalyse;


import IntermediateCode.IntermediateBuilder;
import SymbolTables.RealArrayTuple;
import SymbolTables.PrimaryTuple;
import SymbolTables.SimpleVarDefTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class ConstInitValNode extends TreeNode {
    private ArrayList<TreeNode> children;
    private String childrenType;
    public ConstInitValNode(TreeNode Father,int deep) {
        super(Father,deep);
        children = new ArrayList<>();
    }
    public void AddChildren(TreeNode children) {
        this.children.add(children);
    }

    public ArrayList<TreeNode> getChildren() {
        return children;
    }
    public void setChildrenType(String type) {
        childrenType = type;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        int count = 0;
        for(TreeNode treeNode:children) {
            if (childrenType.equals("ConstExp")) {
                treeNode.treePrint(stringBuilder);
            } else {
                if (count == 0) {
                    stringBuilder.append("{");
                    treeNode.treePrint(stringBuilder);
                } else {
                    stringBuilder.append(", ");
                    treeNode.treePrint(stringBuilder);
                }
            }
            count++;
        }
        if (childrenType.equals("ConstInitVal")) {
            stringBuilder.append("}");
        }
    }

    @Override
    public Word getThisNodeLine() {
        return children.get(0).getThisNodeLine();
    }

    public void RunConstInitValParser() {
        Word word = WordProvider.GetNextWord();
        if (word != null && word.getCategoryCode().equals("LBRACE")) {
            word = WordProvider.GetNextWord();
            if (word != null && !word.getCategoryCode().equals("RBRACE")) {
                WordProvider.RollBackWord(1);
                ConstInitValNode constInitValNode = new ConstInitValNode(this,printDeep);
                AddChildren(constInitValNode);
                constInitValNode.RunConstInitValParser();
                setChildrenType("ConstInitVal");
                while (true) {
                    word = WordProvider.GetNextWord();
                    if (word != null && word.getCategoryCode().equals("COMMA")) {
                        ConstInitValNode constInitValNode1 = new ConstInitValNode(this,printDeep);
                        AddChildren(constInitValNode1);
                        constInitValNode1.RunConstInitValParser();
                    } else {
                        WordProvider.RollBackWord(1);
                        break;
                    }
                }
                WordProvider.CheckEndSign(null,this.getClass(),-1,"RBRACE");
            }
        } else {
            setChildrenType("ConstExp");
            WordProvider.RollBackWord(1);
            ConstExpNode constExpNode = new ConstExpNode(this,printDeep);
            AddChildren(constExpNode);
            constExpNode.RunConstExpParser();
        }
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<ConstInitVal>");
    }

    public void calculateValue(RealArrayTuple tuple,SymbolTable CalculateNeededTable) {
        if (!children.isEmpty()) {
            if (childrenType.equals("ConstExp")) {
                int value = ((ConstExpNode) children.get(0)).calculateValue(CalculateNeededTable);
                tuple.addValueArray(value);
            } else {
                for (TreeNode treeNode : children) {
                    ((ConstInitValNode) treeNode).calculateValue(tuple, CalculateNeededTable);
                }
            }
        }
    }

    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuple) {
        //000000000000000000000000000000000000000000000000000
        for (TreeNode treeNode:children) {
            treeNode.RunSymbolBuilder(symbolTable,null);
        }
        //000000000000000000000000000000000000000000000000000
        if (tuple != null) {
            if (childrenType != null) {
                if (childrenType.equals("ConstExp")) {
                    int value = ((ConstExpNode)children.get(0)).calculateValue(symbolTable);
                    ((SimpleVarDefTuple)tuple).setConstVarValue(value);
                } else {
                    calculateValue((RealArrayTuple)tuple,symbolTable);
                }
            }
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        /* Because We Collect Useful Information In Symbol Table,So We Do Not Need To Use This Node Again */
    }
}
