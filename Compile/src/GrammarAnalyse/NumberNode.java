package GrammarAnalyse;

import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.ErrorMessage;
import Tools.GlobalSetting;
import Tools.WordProvider;
import WordAnalyse.Word;

public class NumberNode extends TreeNode {
    private Word IntConst;

    public NumberNode(TreeNode Father,int deep) {
        super(Father,deep);
    }

    public void setIntConst(Word word) {
        this.IntConst = word;
    }

    public Word getIntConst() {
        return IntConst;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        stringBuilder.append(IntConst.OriginWord);
    }

    @Override
    public Word getThisNodeLine() {
        return IntConst;
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        /*if (tuples instanceof FormalArrayTuple formalArrayTuple) {
            if (formalArrayTuple.getDimension() != 0) {
                ErrorMessage.handleError(this.getClass(),0,SearchFuncCallNodeWord());
            }
        }*/
        setNodeDim(0);
        setNodeType("int");
    }

    public Word SearchFuncCallNodeWord() {
        TreeNode nowNode = this.Father;
        while(nowNode != null) {
            if (nowNode instanceof FuncCallNode) {
                return nowNode.getThisNodeLine();
            }
            nowNode = nowNode.Father;
        }
        ErrorMessage.handleError(this.getClass(),-1,null);
        return null;
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        PrimaryOperand const_n = intermediateBuilder.putIntConstAndReturnVariable(Integer.parseInt(IntConst.OriginWord));
        PrimaryOperand const_0 = intermediateBuilder.putIntConstAndReturnVariable(0);
        VariableOperand dst = intermediateBuilder.putTempVariableAndReturn();
        intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(dst,const_n,const_0));
        this.dst = dst;
    }

    public void RunNumberParserParser() {
        setIntConst(WordProvider.CheckEndSign(null,this.getClass(),-1,"INTCON"));
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<Number>");
    }

    public int calculateValue() {
        return Integer.parseInt(IntConst.OriginWord);
    }
}
