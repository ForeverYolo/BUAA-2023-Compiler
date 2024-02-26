package GrammarAnalyse;


import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.Combination;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class AddExpNode extends TreeNode {
    private final ArrayList<Combination<MulExpNode,Word>> children;
    private int deep;
    public AddExpNode(TreeNode Father,int deep) {
        super(Father,deep);
        children = new ArrayList<>();
    }
    public void AddChildren(MulExpNode mulExpNode,Word operator) {
        this.children.add(new Combination<>(mulExpNode,operator));
    }

    public ArrayList<Combination<MulExpNode,Word>> getChildren() {
        return children;
    }

    public void treePrint(StringBuilder stringBuilder) {
        for (Combination<MulExpNode,Word> combination : children) {
            if (combination.getValue() != null) {
                stringBuilder.append(" ").append(combination.getValue().OriginWord).append(" ");
            }
            combination.getKey().treePrint(stringBuilder);
        }
    }

    public void RunAddExpParser() {
        MulExpNode mulExpNode = new MulExpNode(this,printDeep);
        this.AddChildren(mulExpNode,null);
        mulExpNode.RunMulExpParser();
        printGrammerElement();
        while(true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && (word.getCategoryCode().equals("PLUS") || word.getCategoryCode().equals("MINU"))) {
                MulExpNode mulExpNode1 = new MulExpNode(this,printDeep);
                this.AddChildren(mulExpNode1,word);
                mulExpNode1.RunMulExpParser();
                printGrammerElement();
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
    }

    public void printGrammerElement() {
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<AddExp>");
    }

    public int calculateValue(SymbolTable CalculateNeededTable) {
        int value = 0;
        for(Combination<MulExpNode,Word> combination : children) {
            if (combination.getValue() == null) {
                value = combination.getKey().calculateValue(CalculateNeededTable);
            } else {
                if (combination.getValue().getCategoryCode().equals("PLUS")) {
                    value = value + combination.getKey().calculateValue(CalculateNeededTable);
                } else {
                    value = value - combination.getKey().calculateValue(CalculateNeededTable);
                }
            }
        }
        return value;
    }

    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuple) {
        for(Combination<MulExpNode,Word> tuples : children) {
            tuples.getKey().RunSymbolBuilder(symbolTable,tuple);
            ChangeNodeTypeAndDim(tuples.getKey());
        }
    }


    public Word getThisNodeLine() {
        return children.get(0).getKey().getThisNodeLine();
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder,SymbolTable symbolTable) {
        children.get(0).getKey().ToIntermediate(intermediateBuilder,symbolTable);
        VariableOperand src1 = (VariableOperand) children.get(0).getKey().getDst();
        for (int i = 1;i < children.size();i++) {
            VariableOperand Dst_1 = intermediateBuilder.putTempVariableAndReturn();
            PrimaryOperand const_0 = intermediateBuilder.putIntConstAndReturnVariable(0);
            intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(Dst_1,src1,const_0));
            VariableOperand Dst_2 = intermediateBuilder.putTempVariableAndReturn();
            children.get(i).getKey().ToIntermediate(intermediateBuilder,symbolTable);
            VariableOperand src2 = (VariableOperand) children.get(i).getKey().getDst();
            intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(Dst_2,src2,const_0));
            VariableOperand Dst = intermediateBuilder.putTempVariableAndReturn();
            if (children.get(i).getValue().getCategoryCode().equals("PLUS")) {
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(Dst,Dst_1,Dst_2));
            } else {
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createSubElement(Dst,Dst_1,Dst_2));
            }
            src1 = Dst;
        }
        dst = src1;
    }
}


