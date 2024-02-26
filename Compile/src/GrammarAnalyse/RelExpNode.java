package GrammarAnalyse;


import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.Combination;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class RelExpNode extends TreeNode{
    private final ArrayList<Combination<AddExpNode,Word>> addExpOperatorChildren;

    public RelExpNode(TreeNode Father,int deep) {
        super(Father,deep);
        addExpOperatorChildren = new ArrayList<>();
    }

    public void AddaddExpOperatorChildren(AddExpNode addExpNode,Word operator) {
        addExpOperatorChildren.add(new Combination<>(addExpNode,operator));
    }

    public ArrayList<Combination<AddExpNode,Word>> getAddExpOperatorChildren() {
        return addExpOperatorChildren;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        for (Combination<AddExpNode,Word> combination :addExpOperatorChildren) {
            if (combination.getValue() != null) {
                stringBuilder.append(" ").append(combination.getValue().OriginWord).append(" ");
            }
            combination.getKey().treePrint(stringBuilder);
        }
    }

    @Override
    public Word getThisNodeLine() {
        return addExpOperatorChildren.get(0).getKey().getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        for(Combination<AddExpNode,Word> combination : addExpOperatorChildren) {
            combination.getKey().RunSymbolBuilder(symbolTable,null);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        addExpOperatorChildren.get(0).getKey().ToIntermediate(intermediateBuilder,symbolTable);
        VariableOperand src1 = (VariableOperand) addExpOperatorChildren.get(0).getKey().getDst();
        for (int i = 1;i < addExpOperatorChildren.size();i++) {
            addExpOperatorChildren.get(i).getKey().ToIntermediate(intermediateBuilder,symbolTable);
            VariableOperand Dst = intermediateBuilder.putTempVariableAndReturn();
            VariableOperand src2 = (VariableOperand) addExpOperatorChildren.get(i).getKey().getDst();
            if (addExpOperatorChildren.get(i).getValue().getCategoryCode().equals("LSS")) {
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createSltElement(Dst,src1,src2));
            } else if (addExpOperatorChildren.get(i).getValue().getCategoryCode().equals("LEQ")) {
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createSleElement(Dst,src1,src2));
            } else if (addExpOperatorChildren.get(i).getValue().getCategoryCode().equals("GRE")) {
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createSgtElement(Dst,src1,src2));
            } else {
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createSgeElement(Dst,src1,src2));
            }
            src1 = Dst;
        }
        dst = src1;
    }

    public void RunRelExpParser() {
        AddExpNode addExpNode = new AddExpNode(this,printDeep);
        AddaddExpOperatorChildren(addExpNode,null);
        addExpNode.RunAddExpParser();
        printGrammerElement();
        while (true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && (word.getCategoryCode().equals("LSS") || word.getCategoryCode().equals("LEQ")
                    || word.getCategoryCode().equals("GRE") || word.getCategoryCode().equals("GEQ"))) {
                AddExpNode addExpNode1 = new AddExpNode(this,printDeep);
                AddaddExpOperatorChildren(addExpNode1,word);
                addExpNode1.RunAddExpParser();
                printGrammerElement();
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
    }

    public void printGrammerElement() {
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<RelExp>");
    }
}
