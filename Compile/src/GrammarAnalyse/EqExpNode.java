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

public class EqExpNode extends TreeNode{
    // RelExp { (==|!=) RelExp }
    private ArrayList<Combination<RelExpNode,Word>> relExpChildren;
    public EqExpNode(TreeNode Father,int deep) {
        super(Father,deep);
        relExpChildren = new ArrayList<>();
    }
    public void addRelExpChildren(RelExpNode relExpNode,Word operator) {
        relExpChildren.add(new Combination<>(relExpNode,operator));
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        for(Combination<RelExpNode,Word> combination :relExpChildren) {
            if (combination.getValue() != null) {
                stringBuilder.append(" ").append(combination.getValue().OriginWord).append(" ");
            }
            combination.getKey().treePrint(stringBuilder);
        }
    }

    @Override
    public Word getThisNodeLine() {
        return relExpChildren.get(0).getKey().getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        for(Combination<RelExpNode,Word> combination :relExpChildren) {
            combination.getKey().RunSymbolBuilder(symbolTable,null);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        relExpChildren.get(0).getKey().ToIntermediate(intermediateBuilder,symbolTable);
        VariableOperand src1 = (VariableOperand) relExpChildren.get(0).getKey().dst;
        for (int i = 1; i < relExpChildren.size(); i++) {
            relExpChildren.get(i).getKey().ToIntermediate(intermediateBuilder,symbolTable);
            VariableOperand src2 = (VariableOperand) relExpChildren.get(i).getKey().dst;
            VariableOperand dst = intermediateBuilder.putTempVariableAndReturn();
            if (relExpChildren.get(i).getValue().getCategoryCode().equals("EQL")) {
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createSeqElement(dst,src1,src2));
            } else {
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createSneElement(dst,src1,src2));
            }
            src1 = dst;
        }
        dst = src1;
    }

    public void RunEqExpParser() {
        RelExpNode relExpNode = new RelExpNode(this,printDeep);
        addRelExpChildren(relExpNode,null);
        relExpNode.RunRelExpParser();
        printGrammerElement();
        while (true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && (word.getCategoryCode().equals("EQL") || word.getCategoryCode().equals("NEQ"))) {
                RelExpNode relExpNode1 = new RelExpNode(this,printDeep);
                addRelExpChildren(relExpNode1,word);
                relExpNode1.RunRelExpParser();
                printGrammerElement();
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
    }

    public void printGrammerElement() {
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<EqExp>");
    }
}
