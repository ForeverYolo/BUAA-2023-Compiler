package GrammarAnalyse;


import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class FuncFParamsNode extends TreeNode{
    private ArrayList<FuncFParamNode> funcFParamChildren;
    private ArrayList<VariableOperand> FormalParams;

    public FuncFParamsNode(TreeNode Father,int deep) {
        super(Father,deep);
        funcFParamChildren = new ArrayList<>();
        FormalParams = new ArrayList<>();
    }



    public ArrayList<FuncFParamNode> getFuncFParamChildren() {
        return funcFParamChildren;
    }

    public void addFuncFParamChildren(FuncFParamNode funcFParamNode) {
        funcFParamChildren.add(funcFParamNode);
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        int count = 0;
        for (FuncFParamNode funcFParamNode: funcFParamChildren) {
            if (count != 0) {
                stringBuilder.append(",");
            }
            funcFParamNode.treePrint(stringBuilder);
            count++;
        }
    }

    @Override
    public Word getThisNodeLine() {
        return funcFParamChildren.get(0).getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        for(FuncFParamNode funcFParamNode:funcFParamChildren) {
            funcFParamNode.RunSymbolBuilder(symbolTable,tuples);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        /* No implement */
    }

    public ArrayList<VariableOperand> getFormalParams() {
        return FormalParams;
    }

    public void RunFuncFParamsParser() {
        FuncFParamNode funcFParamNode = new FuncFParamNode(this,printDeep);
        addFuncFParamChildren(funcFParamNode);
        funcFParamNode.RunFuncFParamParser();
        while(true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && word.getCategoryCode().equals("COMMA")) {
                FuncFParamNode funcFParamNode1 = new FuncFParamNode(this,printDeep);
                addFuncFParamChildren(funcFParamNode1);
                funcFParamNode1.RunFuncFParamParser();
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<FuncFParams>");
    }

}
