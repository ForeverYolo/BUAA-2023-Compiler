package GrammarAnalyse;


import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.Elements.MemoryElements;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.*;
import Tools.Combination;
import Tools.GlobalSetting;
import Tools.ErrorMessage;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class LvalNode extends TreeNode{
    private Word ident;
    private final ArrayList<ExpNode> children;

    public LvalNode(TreeNode Father,int deep) {
        super(Father,deep);
        children = new ArrayList<>();
    }

    public void setLvalIdent(Word word) {
        this.ident = word;
    }

    public void AddChildren(ExpNode children) {
        this.children.add(children);
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        stringBuilder.append(ident.OriginWord);
        for (TreeNode treeNode : children) {
            stringBuilder.append("[");
            treeNode.treePrint(stringBuilder);
            stringBuilder.append("]");
        }
    }

    public void UpdateValueForIntermediate(VariableOperand variableOperand, IntermediateBuilder intermediateBuilder,SymbolTable symbolTable) {
        dst = symbolTable.queryTupleFromAllRelativeSymbolTable(ident.OriginWord,true).getPrimaryOperand();
        if (children.isEmpty()) {
            PrimaryOperand const_0 = intermediateBuilder.putIntConstAndReturnVariable(0);
            intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement((VariableOperand) dst,variableOperand,const_0));
        } else {
            Combination<VariableOperand,Boolean> temp_addr = getAddr(intermediateBuilder,symbolTable);
            intermediateBuilder.AddIntermediateExpression(MemoryElements.CreateStoreElement(variableOperand,temp_addr.getKey()));
        }
    }

    public Combination<VariableOperand,Boolean> getAddr(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        PrimaryTuple ArrayTuple = symbolTable.queryTupleFromAllRelativeSymbolTable(ident.OriginWord,true);
        ArrayList<Integer> PerDimensionSize = ArrayTuple instanceof RealArrayTuple ? ((RealArrayTuple)ArrayTuple).getPerDimensionSize() :
                                                ArrayTuple instanceof FormalArrayTuple ? ((FormalArrayTuple)ArrayTuple).getPerDimensionSize() : new ArrayList<>();
        PrimaryOperand const_0 = intermediateBuilder.putIntConstAndReturnVariable(0);
        VariableOperand temp_old = null;
        VariableOperand dst_addr = null;
        for(int i = 0; i < children.size(); i++) {
            children.get(i).ToIntermediate(intermediateBuilder,symbolTable);
            VariableOperand variableOperand = (VariableOperand) children.get(i).getDst();
            int count = i;
            int size = 4;
            while(count + 1 < PerDimensionSize.size()) {
                count = count + 1;
                size = PerDimensionSize.get(count) * size;
            }
            PrimaryOperand const_Size = intermediateBuilder.putIntConstAndReturnVariable(size);
            VariableOperand temp_dst_1 = intermediateBuilder.putTempVariableAndReturn();
            intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(temp_dst_1,const_Size,const_0));
            VariableOperand temp_dst_2 = intermediateBuilder.putTempVariableAndReturn();
            intermediateBuilder.AddIntermediateExpression(CalculateElement.createMulElement(temp_dst_2,variableOperand,temp_dst_1));
            if (i != 0) {
                dst_addr = intermediateBuilder.putTempVariableAndReturn();
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(dst_addr,temp_old,temp_dst_2));
            } else {
                dst_addr = temp_dst_2;
            }
            temp_old = temp_dst_2;
        }
        VariableOperand temp_end = intermediateBuilder.putTempVariableAndReturn();
        intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(temp_end,dst,dst_addr));
        if (children.size() == PerDimensionSize.size()) {
            return new Combination<>(temp_end,true);
        } else {
            return new Combination<>(temp_end,false);
        }
    }

    @Override
    public Word getThisNodeLine() {
        return ident;
    }

    public void setFather(TreeNode Father) {
        this.Father = Father;
    }
    public void RunLvalParser() {
        setLvalIdent(WordProvider.CheckEndSign(null,this.getClass(),-1,"IDENFR"));
        while(true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && word.getCategoryCode().equals("LBRACK")) {
                ExpNode expNode = new ExpNode(this,printDeep);
                AddChildren(expNode);
                expNode.RunExpParser();
                WordProvider.CheckEndSign(expNode.getThisNodeLine(),this.getClass(),0,"RBRACK");
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<LVal>");
    }

    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple formalTuple) {
        PrimaryTuple realTuple = symbolTable.queryTupleFromAllRelativeSymbolTable(ident.OriginWord,false);
        if (realTuple == null) {
            ErrorMessage.handleError(this.getClass(),1,ident);
        }
        setNodeTypeAndDim(realTuple);
        for (int i = children.size() - 1; i >= 0; i--) {
            children.get(i).RunSymbolBuilder(symbolTable,null);
        }
        /*if (formalTuple != null) {
            int Real_Dimension = (realTuple instanceof RealArrayTuple) ? ((RealArrayTuple) realTuple).getDimension() - children.size() :
                    (realTuple instanceof FormalArrayTuple) ? ((FormalArrayTuple) realTuple).getDimension() - children.size() : 0;
            int Formal_Dimension = (formalTuple instanceof FormalArrayTuple) ? ((FormalArrayTuple) formalTuple).getDimension() : 0;
            if (Real_Dimension == Formal_Dimension) {
                return;
            }
            ErrorMessage.handleError(this.getClass(),2,SearchFuncCallNodeWord());
        }*/
    }

    public void setNodeTypeAndDim(PrimaryTuple realTuple) {
        int Real_Dimension = (realTuple instanceof RealArrayTuple) ? ((RealArrayTuple) realTuple).getDimension() - children.size() :
                (realTuple instanceof FormalArrayTuple) ? ((FormalArrayTuple) realTuple).getDimension() - children.size() : 0;
        ArrayList<Integer> PerDim = (realTuple instanceof RealArrayTuple) ? ((RealArrayTuple)realTuple).getPerDimensionSize() :
                                    (realTuple instanceof FormalArrayTuple) ? ((FormalArrayTuple)realTuple).getPerDimensionSize() : new ArrayList<>();
        int start = PerDim.size() - Real_Dimension;
        for (int i = start ; i < PerDim.size(); i++) {
            this.PerDim.add(PerDim.get(i));
        }
        setNodeDim(Real_Dimension);
        setNodeType("int");
    }


    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        dst = symbolTable.queryTupleFromAllRelativeSymbolTable(ident.OriginWord,true).getPrimaryOperand();
        if (!children.isEmpty()) {
            Combination<VariableOperand,Boolean> temp_addr = getAddr(intermediateBuilder,symbolTable);
            if (temp_addr.getValue()) {
                VariableOperand temp_dst = intermediateBuilder.putTempVariableAndReturn();
                intermediateBuilder.AddIntermediateExpression(MemoryElements.CreateLoadElement(temp_dst,temp_addr.getKey()));
                dst = temp_dst;
            } else {
                dst = temp_addr.getKey();
            }
        }
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

    // 如果不是ConstExp我们认为调用这个方法是不一定正确的，因为可能求不出来
    public int calculateValue(SymbolTable CalculateNeededTable) {
        PrimaryTuple primaryTuple = CalculateNeededTable.queryTupleFromAllRelativeSymbolTable(ident.OriginWord,true);
        if (primaryTuple == null || !primaryTuple.isConst()) {
            //000000000000000000000000000000000000000000000000000
            ErrorMessage.handleError(this.getClass(),-1,null); //这里不行就改成0
            //000000000000000000000000000000000000000000000000000
        }
        if(children.isEmpty()) {
            return CalculateNeededTable.queryConstVariableValue(ident.OriginWord,null);
        } else {
            ArrayList<Integer> expValue = new ArrayList<>();
            for(ExpNode expNode:children) {
                expValue.add(expNode.calculateValue(CalculateNeededTable));
            }
            return CalculateNeededTable.queryConstVariableValue(ident.OriginWord,expValue);
        }
    }

    public Word getIdent() {
        return ident;
    }
}
