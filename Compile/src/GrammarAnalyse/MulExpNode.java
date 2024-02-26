package GrammarAnalyse;


import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.Combination;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class MulExpNode extends TreeNode{
    private final ArrayList<Combination<UnaryExpNode,Word>> children;
    public MulExpNode(TreeNode Father,int deep) {
        super(Father,deep);
        children = new ArrayList<>();
    }

    public void AddChildren(UnaryExpNode unaryExpNode,Word operator) {
        this.children.add(new Combination<>(unaryExpNode,operator));
    }

    public ArrayList<Combination<UnaryExpNode,Word>> getChildren() {
        return children;
    }

    public void treePrint(StringBuilder stringBuilder) {
        for (Combination<UnaryExpNode,Word> combination : children) {
            if (combination.getValue() != null) {
                stringBuilder.append(" ").append(combination.getValue().OriginWord).append(" ");
            }
            combination.getKey().treePrint(stringBuilder);
        }
    }

    @Override
    public Word getThisNodeLine() {
        return children.get(0).getKey().getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuple) {
        for(Combination<UnaryExpNode,Word> tuples : children) {
            tuples.getKey().RunSymbolBuilder(symbolTable,tuple);
            ChangeNodeTypeAndDim(tuples.getKey());
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        children.get(0).getKey().ToIntermediate(intermediateBuilder,symbolTable);
        VariableOperand src1 = (VariableOperand) children.get(0).getKey().getDst();
        for (int i = 1;i < children.size();i++) {
            VariableOperand Dst_1 = intermediateBuilder.putTempVariableAndReturn();
            PrimaryOperand const_0 = intermediateBuilder.putIntConstAndReturnVariable(0);
            intermediateBuilder.AddIntermediateExpression(CalculateElement.createSubElement(Dst_1,src1,const_0));
            VariableOperand Dst_2 = intermediateBuilder.putTempVariableAndReturn();
            children.get(i).getKey().ToIntermediate(intermediateBuilder,symbolTable);
            VariableOperand src2 = (VariableOperand) children.get(i).getKey().getDst();
            intermediateBuilder.AddIntermediateExpression(CalculateElement.createSubElement(Dst_2,src2,const_0));
            VariableOperand Dst = intermediateBuilder.putTempVariableAndReturn();
            if (children.get(i).getValue().getCategoryCode().equals("MULT")) {
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createMulElement(Dst,Dst_1,Dst_2));
            } else if (children.get(i).getValue().getCategoryCode().equals("DIV")){
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createDivElement(Dst,Dst_1,Dst_2));
            } else {
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createModElement(Dst,Dst_1,Dst_2));
            }
            src1 = Dst;
        }
        dst = src1;
    }

    public void RunMulExpParser() {
        UnaryExpNode unaryExpNode = new UnaryExpNode(this,printDeep);
        AddChildren(unaryExpNode,null);
        unaryExpNode.RunUnaryExpParser();
        printGrammerElement();
        while(true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && (word.getCategoryCode().equals("MULT")
                    || word.getCategoryCode().equals("DIV")
                    || word.getCategoryCode().equals("MOD"))) {
                UnaryExpNode unaryExpNode1 = new UnaryExpNode(this,printDeep);
                AddChildren(unaryExpNode1,word);
                unaryExpNode1.RunUnaryExpParser();
                printGrammerElement();
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
    }

    public void printGrammerElement() {
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<MulExp>");
    }

    public int calculateValue(SymbolTable CalculateNeededTable) {
        int value = 0;
        for(Combination<UnaryExpNode,Word> combination : children) {
            if (combination.getValue() == null) {
                value = combination.getKey().calculateValue(CalculateNeededTable);
            } else {
                if (combination.getValue().getCategoryCode().equals("MULT")) {
                    value = value * combination.getKey().calculateValue(CalculateNeededTable);
                } else if (combination.getValue().getCategoryCode().equals("DIV")){
                    value = value / combination.getKey().calculateValue(CalculateNeededTable);
                } else {
                    value = value % combination.getKey().calculateValue(CalculateNeededTable);
                }
            }
        }
        return value;
    }
}
