package GrammarAnalyse;


import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.Elements.MemoryElements;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.PrimaryTuple;
import SymbolTables.RealArrayTuple;
import SymbolTables.SimpleVarDefTuple;
import SymbolTables.SymbolTable;
import Tools.GlobalSetting;
import Tools.ErrorMessage;
import Tools.WordProvider;
import WordAnalyse.Word;

import java.util.ArrayList;

public class ConstDefNode extends TreeNode {
    private Word ident;
    private PrimaryTuple DeclTupleInSymbolTable;
    private ArrayList<ConstExpNode> constExpChildren;
    private ConstInitValNode constInitValChildren;

    public ConstDefNode(TreeNode Father,int deep) {
        super(Father,deep);
        constExpChildren = new ArrayList<>();
        constInitValChildren = null;
        ident = null;
    }

    public void setIdent(Word word) {
        this.ident = word;
    }

    public void addConstExpChildren(ConstExpNode children) {
        this.constExpChildren.add(children);
    }

    public void setConstInitValChildren(ConstInitValNode children) {
        this.constInitValChildren = children;
    }

    public ArrayList<ConstExpNode> getConstExpChildren() {
        return constExpChildren;
    }

    public ConstInitValNode getConstInitValChildren() {
        return constInitValChildren;
    }

    public Word getIdent() {
        return ident;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        stringBuilder.append(ident.OriginWord);
        for(ConstExpNode constExpNode:constExpChildren) {
            stringBuilder.append("[");
            constExpNode.treePrint(stringBuilder);
            stringBuilder.append("]");
        }
        stringBuilder.append(" = ");
        constInitValChildren.treePrint(stringBuilder);
    }


    public void RunConstDefParser() {
        // Ident
       setIdent(WordProvider.CheckEndSign(null,this.getClass(),-1,"IDENFR"));
        // [ ConstExp ]
        while (true) {
            Word word = WordProvider.GetNextWord();
            ConstExpNode constExpNode = new ConstExpNode(this,printDeep);
            if (word != null && word.getCategoryCode().equals("LBRACK")) {
                addConstExpChildren(constExpNode);
                constExpNode.RunConstExpParser();
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
            WordProvider.CheckEndSign(constExpNode.getThisNodeLine(),this.getClass(),0,"RBRACK");
        }
        // =
        WordProvider.CheckEndSign(null,this.getClass(),-1,"ASSIGN");
        // ConstInitVal
        ConstInitValNode constInitValNode = new ConstInitValNode(this,printDeep);
        setConstInitValChildren(constInitValNode);
        constInitValNode.RunConstInitValParser();

        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<ConstDef>");
    }

    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        if (constExpChildren.isEmpty()) {
            SimpleVarDefTuple simpleVarDefTuple = new SimpleVarDefTuple
                    (ident.OriginWord,symbolTable.getOffsetRecord(),"int",true);
            if (symbolTable.queryTupleFromCurrentSymbolTable(ident.OriginWord,false) != null) {
                ErrorMessage.handleError(this.getClass(),1,ident);
            }
            symbolTable.addTuple(ident.OriginWord,simpleVarDefTuple);
            symbolTable.updateOffset(simpleVarDefTuple);
            constInitValChildren.RunSymbolBuilder(symbolTable,simpleVarDefTuple);
            DeclTupleInSymbolTable = simpleVarDefTuple;
        } else {
            RealArrayTuple realArrayTuple = new RealArrayTuple
                    (ident.OriginWord,symbolTable.getOffsetRecord(),"int",true);
            for (ConstExpNode constExpNode:constExpChildren) {
                //000000000000000000000000000000000000000000000000000
                constExpNode.RunSymbolBuilder(symbolTable,null);
                //000000000000000000000000000000000000000000000000000
                int size = constExpNode.calculateValue(symbolTable);
                realArrayTuple.SetPerDimensionSize(size);
            }
            if (symbolTable.queryTupleFromCurrentSymbolTable(ident.OriginWord,false) != null) {
                ErrorMessage.handleError(this.getClass(),2,ident);
            }
            symbolTable.addTuple(ident.OriginWord,realArrayTuple);
            DeclTupleInSymbolTable = realArrayTuple;
            symbolTable.updateOffset(realArrayTuple);
            constInitValChildren.RunSymbolBuilder(symbolTable, realArrayTuple);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder,SymbolTable symbolTable) {
        if (constExpChildren.isEmpty()) {
            VariableOperand temp_dst = intermediateBuilder.putTempVariableAndReturn();
            int Value = ((SimpleVarDefTuple)DeclTupleInSymbolTable).queryConstVarValue();
            PrimaryOperand const_src1 = intermediateBuilder.putIntConstAndReturnVariable(Value);
            PrimaryOperand const_src2 = intermediateBuilder.putIntConstAndReturnVariable(0);
            intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(temp_dst,const_src1,const_src2));
            VariableOperand variable = symbolTable.getFather() == null ? intermediateBuilder.putGlobalVariableAndReturn() : intermediateBuilder.putNormalVariableAndReturn();
            DeclTupleInSymbolTable.setPrimaryOperand(variable);
            intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(variable,temp_dst,const_src2));
        } else {
            int Value = ((RealArrayTuple)DeclTupleInSymbolTable).getElementNumber();
            PrimaryOperand const_src1 = intermediateBuilder.putIntConstAndReturnVariable(Value);
            PrimaryOperand const_src2 = intermediateBuilder.putIntConstAndReturnVariable(0);
            PrimaryOperand const_src3 = intermediateBuilder.putIntConstAndReturnVariable(4);
            VariableOperand variable = symbolTable.getFather() == null ? intermediateBuilder.putGlobalVariableAndReturn() : intermediateBuilder.putNormalVariableAndReturn();
            DeclTupleInSymbolTable.setPrimaryOperand(variable);
            intermediateBuilder.AddIntermediateExpression(MemoryElements.CreateAllocElement(variable,const_src1));
            ArrayList<Integer> values = ((RealArrayTuple) DeclTupleInSymbolTable).getValueArray();
            if (!values.isEmpty()) {
                VariableOperand temp_addr = intermediateBuilder.putTempVariableAndReturn();
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(temp_addr,variable,const_src2));
                PrimaryOperand const_value = intermediateBuilder.putIntConstAndReturnVariable(values.get(0));
                intermediateBuilder.AddIntermediateExpression(MemoryElements.CreateStoreElement(const_value,temp_addr));
                for(int i = 1; i < values.size();i++) {
                    VariableOperand temp_addr_2 = intermediateBuilder.putTempVariableAndReturn();
                    intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(temp_addr_2,temp_addr,const_src3));
                    PrimaryOperand const_value_2 = intermediateBuilder.putIntConstAndReturnVariable(values.get(i));
                    intermediateBuilder.AddIntermediateExpression(MemoryElements.CreateStoreElement(const_value_2,temp_addr_2));
                    temp_addr = temp_addr_2;
                }
            }
        }
        dst = null;
    }

    public Word getThisNodeLine() {
        return ident;
    }
}
