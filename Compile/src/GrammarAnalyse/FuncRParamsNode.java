package GrammarAnalyse;


import IntermediateCode.Elements.OtherElements;
import IntermediateCode.Elements.PrimaryElement;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.*;
import Tools.GlobalSetting;
import Tools.ErrorMessage;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;
import java.util.Objects;

public class FuncRParamsNode extends TreeNode{
    private ArrayList<ExpNode> expChildren;
    private ArrayList<PrimaryElement> PushElements;
    private boolean InLine;

    public ArrayList<ExpNode> getExpChildren() {
        return expChildren;
    }

    public FuncRParamsNode(TreeNode Father, int deep) {
        super(Father,deep);
        expChildren = new ArrayList<>();
        PushElements = new ArrayList<>();
        InLine = false;
    }

    public void addExpChildren(ExpNode expNode) {
        expChildren.add(expNode);
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        int count = 0;
        for(ExpNode expNode:expChildren) {
            if (count != 0) {
                stringBuilder.append(",");
            }
            count++;
            expNode.treePrint(stringBuilder);
        }
    }
    public void setInLine(boolean inLine) {
        InLine = inLine;
    }

    @Override
    public Word getThisNodeLine() {
        return expChildren.get(0).getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        FuncDefTuple funcDefTuple = (FuncDefTuple) tuples;
        if (expChildren.size() != funcDefTuple.getFuncFParamsCount()) {
            ErrorMessage.handleError(this.getClass(),0,Father.getThisNodeLine());
        }
        int count = 0;
        for(ExpNode expNode:expChildren) {
            if (count < funcDefTuple.getFuncFParamsType().size()) {
                PrimaryTuple primaryTuple = funcDefTuple.getFuncFParamsType().get(count);
                expNode.RunSymbolBuilder(symbolTable,primaryTuple);
                CompareRealAndFormal(primaryTuple,expNode);
                count++;
                // 把函数形参传下去便于和实参对比。
            } else {
                expNode.RunSymbolBuilder(symbolTable,null);
                count++;
            }
        }
    }

    public void CompareRealAndFormal(PrimaryTuple primaryTuple,ExpNode expNode) {
        if (primaryTuple instanceof SimpleVarDefTuple && (!expNode.NodeType.equals("int") || expNode.NodeDim != 0)) {
            ErrorMessage.handleError(this.getClass(),1,Father.getThisNodeLine());
        } else if (primaryTuple instanceof FormalArrayTuple formalArrayTuple) {
            int Formal_Dim = formalArrayTuple.getDimension();
            if (Formal_Dim != expNode.NodeDim || !expNode.NodeType.equals("int")) {
                ErrorMessage.handleError(this.getClass(),2,Father.getThisNodeLine());
            } else {
                ArrayList<Integer> FormalPerDim = formalArrayTuple.getPerDimensionSize();
                ArrayList<Integer> RealPerDim = expNode.PerDim;
                for (int i = 1; i < RealPerDim.size(); i++) {
                    if (!Objects.equals(FormalPerDim.get(i), RealPerDim.get(i))) {
                        ErrorMessage.handleError(this.getClass(),3,Father.getThisNodeLine());
                    }
                }
            }
        }
    }



    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        for(int i = expChildren.size() - 1; i >= 0; i--) {
            expChildren.get(i).ToIntermediate(intermediateBuilder,symbolTable);
        }
        if (!GlobalSetting.FunctionInLineOptimize || !InLine) {
            for(ExpNode expNode: expChildren) {
                OtherElements PushElement = OtherElements.createPushElement((VariableOperand) expNode.dst);
                PushElements.add(PushElement);
                intermediateBuilder.AddIntermediateExpression(PushElement);
            }
        }
    }

    public ArrayList<PrimaryElement> getPushElements() {
        return PushElements;
    }

    public void RunFuncRParamsParser() {
        ExpNode expNode = new ExpNode(this,printDeep);
        expNode.RunExpParser();
        addExpChildren(expNode);
        while (true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && word.getCategoryCode().equals("COMMA")) {
                ExpNode expNode1 = new ExpNode(this,printDeep);
                expNode1.RunExpParser();
                addExpChildren(expNode1);
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<FuncRParams>");
    }
}
