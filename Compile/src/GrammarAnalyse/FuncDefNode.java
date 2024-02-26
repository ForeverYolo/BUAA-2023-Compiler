package GrammarAnalyse;


import IntermediateCode.Elements.OtherElements;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.FuncOperand;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.FuncDefTuple;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.ErrorMessage;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class FuncDefNode extends TreeNode{
    private FuncDefTuple funcDefTuple;
    private SymbolTable symbolTable;
    private FuncTypeNode funcTypeChildren;
    private Word ident;
    private FuncFParamsNode funcFParamsChildren;
    private BlockNode blockChildren;
    private HashSet<FuncDefNode> CallFuncDefNodeSet;

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public FuncDefNode(TreeNode Father, int deep) {
        super(Father,deep);
        funcTypeChildren = null;
        ident = null;
        funcFParamsChildren = null;
        blockChildren = null;
        CallFuncDefNodeSet = new HashSet<>();
    }

    public BlockNode getBlockChildren() {
        return blockChildren;
    }

    public FuncFParamsNode getFuncFParamsChildren() {
        return funcFParamsChildren;
    }

    public HashSet<FuncDefNode> getCallFuncDefNodeSet() {
        return CallFuncDefNodeSet;
    }

    public Word getIdent() {
        return ident;
    }

    public void setFuncTypeChildren(FuncTypeNode funcTypeChildren) {
        this.funcTypeChildren = funcTypeChildren;
    }

    public void setIdent(Word ident) {
        this.ident = ident;
    }

    public void setFuncFParamsChildren(FuncFParamsNode funcFParamsChildren) {
        this.funcFParamsChildren = funcFParamsChildren;
    }

    public void SearchCallers(HashSet<FuncDefNode> unique) {
        CallFuncDefNodeSet.stream().filter(i -> !unique.contains(i)).forEach(i -> {
            unique.add(i);
            i.SearchCallers(unique);
        });
    }

    public void setBlockChildren(BlockNode blockChildren) {
        this.blockChildren = blockChildren;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        funcTypeChildren.treePrint(stringBuilder);
        stringBuilder.append(" ");
        stringBuilder.append(ident.OriginWord);
        stringBuilder.append("(");
        if (funcFParamsChildren != null) {
            funcFParamsChildren.treePrint(stringBuilder);
        }
        stringBuilder.append(")\n");
        blockChildren.treePrint(stringBuilder);
    }

    @Override
    public Word getThisNodeLine() {
        return funcTypeChildren.getThisNodeLine();
    }

    public FuncDefTuple getFuncDefTuple() {
        return funcDefTuple;
    }

    public void RunFuncDefParser() {
        FuncTypeNode funcTypeNode = new FuncTypeNode(this,printDeep);
        setFuncTypeChildren(funcTypeNode);
        funcTypeNode.RunFuncTypeParser();

        setIdent(WordProvider.CheckEndSign(null,this.getClass(),-1,"IDENFR"));

        WordProvider.CheckEndSign(null,this.getClass(),-1,"LPARENT");

        Word word = WordProvider.GetNextWord();
        if (word != null && word.getCategoryCode().equals("INTTK")) {
            WordProvider.RollBackWord(1);
            FuncFParamsNode funcFParamsNode = new FuncFParamsNode(this,printDeep);
            setFuncFParamsChildren(funcFParamsNode);
            funcFParamsNode.RunFuncFParamsParser();
            WordProvider.CheckEndSign(funcFParamsNode.getThisNodeLine(),this.getClass(),0,"RPARENT");
        } else {
            WordProvider.RollBackWord(1);
            WordProvider.CheckEndSign(funcTypeNode.getThisNodeLine(),this.getClass(),3,"RPARENT");
        }

        BlockNode blockNode = new BlockNode(this,printDeep + 1);
        blockNode.RunBlockParser();
        setBlockChildren(blockNode);
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<FuncDef>");
    }

    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuple) {
        this.symbolTable = new SymbolTable(symbolTable);
        symbolTable.AddChildren(this.symbolTable);
        FuncDefTuple funcDefTuple = new FuncDefTuple(ident.OriginWord,0, funcTypeChildren.getType().OriginWord,this);
        this.funcDefTuple = funcDefTuple;
        if (funcFParamsChildren != null) {
            funcFParamsChildren.RunSymbolBuilder(this.symbolTable,funcDefTuple);
        }
        if (symbolTable.queryTupleFromAllRelativeSymbolTable(ident.OriginWord,false) != null) {
            ErrorMessage.handleError(this.getClass(),1,ident);
        }
        symbolTable.addTuple(ident.OriginWord,funcDefTuple);
        if (ReturnValueType().equals("INTTK")) {
            ReturnNode returnNode = SearchReturnNode();
            if (returnNode == null || returnNode.getExpChildren() == null) {
                ErrorMessage.handleError(this.getClass(),2,blockChildren.getEndLBraceWord());
                //这里有更强的条件约束，如果int函数中的return无返回值也会报错。
            }
        }
        blockChildren.RunSymbolBuilder(this.symbolTable,null);
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        int num = 0;
        PrimaryTuple primaryTuple = symbolTable.queryTupleFromAllRelativeSymbolTable(ident.OriginWord,false);
        ArrayList<PrimaryTuple> FormalFuncParam = ((FuncDefTuple)primaryTuple).getFuncFParamsType();
        ArrayList<PrimaryOperand> primaryOperands = new ArrayList<>();
        for (PrimaryTuple PerFormalFuncParam : FormalFuncParam) {
            VariableOperand variableOperand = intermediateBuilder.putParamVariableAndReturn(num);
            primaryOperands.add(variableOperand);
            PerFormalFuncParam.setPrimaryOperand(variableOperand);
            num++;
        }
        FuncOperand funcOperand = intermediateBuilder.putFuncOperandAndReturn(ident.OriginWord,primaryOperands,null,false);
        primaryTuple.setPrimaryOperand(funcOperand);
        blockChildren.ToIntermediate(intermediateBuilder,symbolTable);
        if (SearchReturnNode() != null) {
            funcOperand.setRealReturnVariable(SearchReturnNode().getInnerDst());
        } else {
            intermediateBuilder.AddIntermediateExpression(OtherElements.createReturnElement(null));
        }
    }

    public ReturnNode SearchReturnNode() {
        return blockChildren.SearchReturnNode();
    }

    public String ReturnValueType() {
        return funcTypeChildren.getType().getCategoryCode();
    }
}
