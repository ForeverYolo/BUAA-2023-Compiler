package GrammarAnalyse;

import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.TagOperand;
import IntermediateCode.Operands.VariableOperand;
import IntermediateCode.Operands.VariableType;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.Combination;
import WordAnalyse.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;

public abstract class TreeNode {
    public String NodeType;
    public int NodeDim;
    public ArrayList<Integer> PerDim;
    public TreeNode Father;
    public int printDeep;
    public PrimaryOperand dst;
    public static LinkedList<Combination<TagOperand, VariableOperand>> inLineStack = new LinkedList<>();

    public PrimaryOperand getDst() {
        return dst;
    }

    public TreeNode(TreeNode Father) {
        this.Father = Father;
    }
    public TreeNode(TreeNode Father,int deep) {
        this.Father = Father;
        this.printDeep = deep;
        this.NodeType = "Primary";
        this.NodeDim = -1;
        this.PerDim = new ArrayList<>();
    }
    public String deepToPrint(int printDeep) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i<printDeep;i++) {
            stringBuilder.append("\t");
        }
        return stringBuilder.toString();
    }
    public abstract void treePrint(StringBuilder stringBuilder);

    public void deleteTab(StringBuilder stringBuilder) {
        int length = stringBuilder.length() - 1;
        for (int i = length;i>=0;i--) {
            if (stringBuilder.charAt(i) == '\t') {
                stringBuilder.deleteCharAt(i);
            } else {
                break;
            }
        }
    }

    public void ChangeNodeTypeAndDim(TreeNode node) {
        if (this.NodeType.equals("Primary")) {
            this.NodeType = node.NodeType;
        } else if (!this.NodeType.equals(node.NodeType) || node.NodeType.equals("Error")) {
            this.NodeType = "Error";
        }

        if (this.NodeDim == -1) {
            this.NodeDim = node.NodeDim;
        } else if (this.NodeDim != node.NodeDim || node.NodeDim == -2) {
            this.NodeDim = -2;
        }

        this.PerDim = node.PerDim;
    }

    public void setNodeType(String nodeType) {
        this.NodeType = nodeType;
    }

    public void setNodeDim(int nodeDim) {
        NodeDim = nodeDim;
    }

    public abstract Word getThisNodeLine();

    public abstract void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples);

    public abstract void ToIntermediate(IntermediateBuilder intermediateBuilder,SymbolTable symbolTable);

}
