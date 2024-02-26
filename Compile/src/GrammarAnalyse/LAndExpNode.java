package GrammarAnalyse;


import IntermediateCode.Elements.BranchElement;
import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.Elements.OtherElements;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.TagOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class LAndExpNode extends TreeNode{
    // EqExp { && EqExp }
    private ArrayList<EqExpNode> eqExpChildren;
    private VariableOperand CompareResultDst;
    public LAndExpNode(TreeNode Father,int deep) {
        super(Father,deep);
        eqExpChildren = new ArrayList<>();
    }

    public void addEqExpChildren(EqExpNode expNode) {
        this.eqExpChildren.add(expNode);
    }

    public ArrayList<EqExpNode> getEqExpChildren() {
        return eqExpChildren;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        int count = 0;
        for (EqExpNode expNode:eqExpChildren) {
            if (count != 0) {
                stringBuilder.append(" ").append("&&").append(" ");
            }
            expNode.treePrint(stringBuilder);
            count++;
        }
    }

    @Override
    public Word getThisNodeLine() {
        return eqExpChildren.get(0).getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        for (EqExpNode eqExpNode : eqExpChildren) {
            eqExpNode.RunSymbolBuilder(symbolTable,null);
        }
    }


    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        eqExpChildren.get(0).ToIntermediate(intermediateBuilder,symbolTable);
        VariableOperand src1 = (VariableOperand) eqExpChildren.get(0).getDst();
        CompareResultDst = src1;
        TagOperand IgnoreTag = null;
        for (int i = 1;i < eqExpChildren.size();i++) {
            if (i == 1) {
                IgnoreTag = intermediateBuilder.putTagOperandAndReturn("IgnoreTag");
                VariableOperand src2 = intermediateBuilder.putTempVariableAndReturn();
                CompareResultDst = intermediateBuilder.putTempVariableAndReturn();
                PrimaryOperand Const_0 = intermediateBuilder.putIntConstAndReturnVariable(0);
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(src2,Const_0,Const_0));
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createSneElement(CompareResultDst,src1,src2));
            }
            TagOperand NotIgnoreTag = intermediateBuilder.putTagOperandAndReturn("NotIgnoreTag");
            intermediateBuilder.AddIntermediateExpression(BranchElement.createBeqzElement(NotIgnoreTag,IgnoreTag,CompareResultDst));
            intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(NotIgnoreTag));
            eqExpChildren.get(i).ToIntermediate(intermediateBuilder,symbolTable);
            src1 = (VariableOperand) eqExpChildren.get(i).getDst();
            PrimaryOperand Const_0 = intermediateBuilder.putIntConstAndReturnVariable(0);
            intermediateBuilder.AddIntermediateExpression(CalculateElement.createSneElement(CompareResultDst,src1,Const_0));
            if (i == eqExpChildren.size() - 1) {
                intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(IgnoreTag));
            }
        }
        dst = CompareResultDst;
    }


    public void RunLAndExpParser() {
        EqExpNode eqExpNode = new EqExpNode(this,printDeep);
        addEqExpChildren(eqExpNode);
        eqExpNode.RunEqExpParser();
        printGrammerElement();
        while (true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && word.getCategoryCode().equals("AND")) {
                EqExpNode eqExpNode1 = new EqExpNode(this,printDeep);
                addEqExpChildren(eqExpNode1);
                eqExpNode1.RunEqExpParser();
                printGrammerElement();
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
    }

    public void printGrammerElement() {
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<LAndExp>");
    }
}
