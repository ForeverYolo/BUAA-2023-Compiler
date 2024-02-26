package GrammarAnalyse;


import IntermediateCode.Elements.OtherElements;
import IntermediateCode.Elements.PrimaryElement;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.ErrorMessage;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class PrintfNode extends TreeNode{
    private Word PrintWord;
    private Word FormatString;
    private ArrayList<ExpNode> expChildren;

    public PrintfNode(TreeNode Father,int deep) {
        super(Father,deep);
        expChildren = new ArrayList<>();
        FormatString = null;
        PrintWord = null;
    }

    public void setFormatString(Word formatString) {
        FormatString = formatString;
    }

    public void addExpChildren(ExpNode expNode) {
        this.expChildren.add(expNode);
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        stringBuilder.append("printf(");
        stringBuilder.append(FormatString.OriginWord);
        for(ExpNode expNode:expChildren) {
            stringBuilder.append(",");
            expNode.treePrint(stringBuilder);
        }
        stringBuilder.append(");");
    }

    @Override
    public Word getThisNodeLine() {
        return PrintWord;
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        if (FormatString.OriginWord.split("%d").length - 1 != expChildren.size()) {
            ErrorMessage.handleError(this.getClass(),0,PrintWord);
        }
        checkFormatString(FormatString);
        for (int i = expChildren.size() - 1; i >= 0; i--) {
            expChildren.get(i).RunSymbolBuilder(symbolTable,null);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        String OriginString = FormatString.OriginWord.substring(1,FormatString.OriginWord.length()-1);
        StringBuilder StringCollect = new StringBuilder();
        int expCount = 0;
        for (int i = expChildren.size() - 1; i >= 0; i--) {
            expChildren.get(i).ToIntermediate(intermediateBuilder,symbolTable);
        }
        for (int i = 0; i < OriginString.length() ; i++) {
            if (i + 1 < OriginString.length() && OriginString.charAt(i) == '%' && OriginString.charAt(i + 1) == 'd') {
                if (!StringCollect.isEmpty()) {
                    RegisterString(StringCollect,intermediateBuilder);
                    StringCollect = new StringBuilder();
                }
                VariableOperand number = (VariableOperand) expChildren.get(expCount).getDst();
                intermediateBuilder.AddIntermediateExpression(OtherElements.createPutNumberElement(number));
                expCount += 1;
                i += 1;
            } else {
                StringCollect.append(OriginString.charAt(i));
            }
        }
        if (!StringCollect.isEmpty()) {
            RegisterString(StringCollect,intermediateBuilder);
        }
    }

    public void RegisterString(StringBuilder StringCollect,IntermediateBuilder intermediateBuilder) {
        int Addr = intermediateBuilder.putStringConstAndReturnID(StringCollect.toString());
        PrimaryOperand Const_n = intermediateBuilder.putIntConstAndReturnVariable(Addr);
        intermediateBuilder.AddIntermediateExpression(OtherElements.createPutStrElement(Const_n));
    }

    public void checkFormatString(Word formatString) {
        char [] s = formatString.OriginWord.substring(1,formatString.OriginWord.length()-1).toCharArray();
        for (int i = 0;i < s.length;i++) {
            if ((s[i] == 32 || s[i] == 33 || (s[i] >= 40 && s[i] <= 126 && s[i] != '\\'))) {
                continue;
            } else if (s[i] == '\\' && (i < s.length - 1 && s[i + 1] == 'n')) {
                i += 1;
                continue;
            } else if (s[i] == '%' && (i < s.length - 1 && s[i + 1] == 'd')) {
                i += 1;
                continue;
            }
            ErrorMessage.handleError(this.getClass(),1,formatString);
            break;
        }
    }

    public void PrintfParser() {
        PrintWord = WordProvider.CheckEndSign(null,this.getClass(),-1,"PRINTFTK");
        WordProvider.CheckEndSign(null,this.getClass(),-1, "LPARENT");
        FormatString = WordProvider.CheckEndSign(null,this.getClass(),-1,"STRCON");
        while (true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && word.getCategoryCode().equals("COMMA")) {
                ExpNode expNode = new ExpNode(this,printDeep);
                addExpChildren(expNode);
                expNode.RunExpParser();
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
        WordProvider.CheckEndSign(PrintWord,this.getClass(),2,"RPARENT");
        WordProvider.CheckEndSign(PrintWord,this.getClass(),3, "SEMICN");
    }
}
