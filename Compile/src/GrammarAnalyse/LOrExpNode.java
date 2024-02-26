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

public class LOrExpNode extends TreeNode{
    private ArrayList<LAndExpNode> LAndExpChildren;
    private VariableOperand CompareResultDst;

    public LOrExpNode(TreeNode Father,int deep) {
        super(Father,deep);
        LAndExpChildren = new ArrayList<>();
    }

    public void addLAndExpChildren(LAndExpNode lAndExpNode) {
        this.LAndExpChildren.add(lAndExpNode);
    }

    public ArrayList<LAndExpNode> getLAndExpChildren() {
        return LAndExpChildren;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        int count = 0;
        for (LAndExpNode lAndExpNode:LAndExpChildren) {
            if (count != 0) {
                stringBuilder.append(" ").append("||").append(" ");
            }
            lAndExpNode.treePrint(stringBuilder);
            count++;
        }
    }

    @Override
    public Word getThisNodeLine() {
        return LAndExpChildren.get(0).getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        for (LAndExpNode lAndExpNode:LAndExpChildren) {
            lAndExpNode.RunSymbolBuilder(symbolTable,null);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        LAndExpChildren.get(0).ToIntermediate(intermediateBuilder,symbolTable);
        VariableOperand src1 = (VariableOperand) LAndExpChildren.get(0).getDst();
        CompareResultDst = src1;
        TagOperand IgnoreTag = null;
        for (int i = 1;i < LAndExpChildren.size();i++) {
            if (i == 1) {
                IgnoreTag = intermediateBuilder.putTagOperandAndReturn("IgnoreTag");
                VariableOperand src2 = intermediateBuilder.putTempVariableAndReturn();
                CompareResultDst = intermediateBuilder.putTempVariableAndReturn();
                PrimaryOperand Const_0 = intermediateBuilder.putIntConstAndReturnVariable(0);
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(src2,Const_0,Const_0));
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createSneElement(CompareResultDst,src1,src2));
            }
            TagOperand NotIgnoreTag = intermediateBuilder.putTagOperandAndReturn("NotIgnoreTag");
            intermediateBuilder.AddIntermediateExpression(BranchElement.createBnzElement(NotIgnoreTag,IgnoreTag,CompareResultDst));
            intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(NotIgnoreTag));
            LAndExpChildren.get(i).ToIntermediate(intermediateBuilder,symbolTable);
            src1 = (VariableOperand) LAndExpChildren.get(i).getDst();
            PrimaryOperand Const_0 = intermediateBuilder.putIntConstAndReturnVariable(0);
            intermediateBuilder.AddIntermediateExpression(CalculateElement.createSneElement(CompareResultDst,src1,Const_0));
            if (i == LAndExpChildren.size() - 1) {
                intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(IgnoreTag));
            }
        }
        dst = CompareResultDst;
    }

    public void RunLOrExpParser() {
        LAndExpNode lAndExpNode = new LAndExpNode(this,printDeep);
        addLAndExpChildren(lAndExpNode);
        lAndExpNode.RunLAndExpParser();
        printGrammerElement();
        while (true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && word.getCategoryCode().equals("OR")) {
                LAndExpNode lAndExpNode1 = new LAndExpNode(this,printDeep);
                addLAndExpChildren(lAndExpNode1);
                lAndExpNode1.RunLAndExpParser();
                printGrammerElement();
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
    }

    public void printGrammerElement() {
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<LOrExp>");
    }
}
