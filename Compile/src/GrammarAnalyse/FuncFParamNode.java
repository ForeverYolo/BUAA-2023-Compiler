package GrammarAnalyse;


import IntermediateCode.IntermediateBuilder;
import SymbolTables.*;
import Tools.ErrorMessage;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class FuncFParamNode extends TreeNode{
    private BtypeNode btypeChildren;
    private Word ident;
    private ArrayList<ConstExpNode> constExpChildren;
    private boolean isArray;
    public FuncFParamNode(TreeNode Father,int deep) {
        super(Father,deep);
        btypeChildren = null;
        ident = null;
        constExpChildren = new ArrayList<>();
        isArray = false;
    }

    public void setBtypeChildren(BtypeNode btypeChildren) {
        this.btypeChildren = btypeChildren;
    }

    public void setIdent(Word ident) {
        this.ident = ident;
    }

    public void addConstExpChildren(ConstExpNode constExpChildren) {
        this.constExpChildren.add(constExpChildren);
    }

    public void setIsArray(boolean array) {
        isArray = array;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        btypeChildren.treePrint(stringBuilder);
        stringBuilder.append(" ");
        stringBuilder.append(ident.OriginWord);
        if (isArray) {
            stringBuilder.append("[]");
        }
        for (ConstExpNode constExpNode : constExpChildren) {
            stringBuilder.append("[");
            constExpNode.treePrint(stringBuilder);
            stringBuilder.append("]");
        }
    }

    @Override
    public Word getThisNodeLine() {
        return btypeChildren.getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        FuncDefTuple funcDefTuple = (FuncDefTuple) tuples;
        if (symbolTable.queryTupleFromCurrentSymbolTable(ident.OriginWord,false) != null) {
            ErrorMessage.handleError(this.getClass(),2,ident);
        }
        if (isArray || !constExpChildren.isEmpty()) {
            FormalArrayTuple formalArrayTuple = new FormalArrayTuple(ident.OriginWord,symbolTable.getOffsetRecord(),"int",false);
            formalArrayTuple.SetPerDimensionSize(0);
            for(ConstExpNode constExpNode:constExpChildren) {
                //000000000000000000000000000000000000000000000000000
                constExpNode.RunSymbolBuilder(symbolTable,null);
                //000000000000000000000000000000000000000000000000000
                int value = constExpNode.calculateValue(symbolTable);
                formalArrayTuple.SetPerDimensionSize(value);
            }
            funcDefTuple.AddFuncFParams(formalArrayTuple);
            symbolTable.updateOffset(formalArrayTuple);
            symbolTable.addTuple(ident.OriginWord,formalArrayTuple);
        } else {
            SimpleVarDefTuple simpleVarDefTuple =
                    new SimpleVarDefTuple(ident.OriginWord,symbolTable.getOffsetRecord(),"int",false);
            funcDefTuple.AddFuncFParams(simpleVarDefTuple);
            symbolTable.updateOffset(simpleVarDefTuple);
            symbolTable.addTuple(ident.OriginWord,simpleVarDefTuple);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        /* No implement */
    }

    public void RunFuncFParamParser() {
        BtypeNode btypeNode = new BtypeNode(this,printDeep);
        setBtypeChildren(btypeNode);
        btypeNode.RunBTypeParser();

        setIdent(WordProvider.CheckEndSign(null,this.getClass(),-1,"IDENFR"));
        Word word = WordProvider.GetNextWord();
        if (word != null && word.getCategoryCode().equals("LBRACK")) {
            WordProvider.CheckEndSign(btypeNode.getThisNodeLine(),this.getClass(),0,"RBRACK");
            setIsArray(true);
            while (true) {
                word = WordProvider.GetNextWord();
                if (word != null && word.getCategoryCode().equals("LBRACK")) {
                    ConstExpNode constExpNode = new ConstExpNode(this,printDeep);
                    addConstExpChildren(constExpNode);
                    constExpNode.RunConstExpParser();
                    WordProvider.CheckEndSign(constExpNode.getThisNodeLine(),this.getClass(),1,"RBRACK");
                } else {
                    WordProvider.RollBackWord(1);
                    break;
                }
            }
        } else {
            WordProvider.RollBackWord(1);
        }
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<FuncFParam>");
    }


}
