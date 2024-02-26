package GrammarAnalyse;

import IntermediateCode.Elements.MemoryElements;
import IntermediateCode.Elements.OtherElements;
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

public class VarDefNode extends TreeNode {
    private Word ident;
    private PrimaryTuple DeclTupleInSymbolTable;
    private final ArrayList<ConstExpNode> constExpChildren;
    private InitValNode initValChildren;
    public VarDefNode(TreeNode Father,int deep) {
        super(Father,deep);
        constExpChildren = new ArrayList<>();
        initValChildren = null;
        ident = null;
    }

    public void setIdent(Word word) {
        this.ident = word;
    }

    public void addConstExpChildren(ConstExpNode children) {
        this.constExpChildren.add(children);
    }

    public void setInitValChildren(InitValNode children) {
        this.initValChildren = children;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        stringBuilder.append(ident.OriginWord);
        for (ConstExpNode constExpNode:constExpChildren) {
            stringBuilder.append("[");
            constExpNode.treePrint(stringBuilder);
            stringBuilder.append("]");
        }
        if (initValChildren != null) {
            stringBuilder.append(" = ");
            initValChildren.treePrint(stringBuilder);
        }
    }

    @Override
    public Word getThisNodeLine() {
        return ident;
    }

    public void RunVarDefParserParser() {
        // Indet
        setIdent(WordProvider.CheckEndSign(null,this.getClass(),-1,"IDENFR"));
        // { [ ConstExp ] }
        while(true) {
            Word word = WordProvider.GetNextWord();
            if (word != null && word.getCategoryCode().equals("LBRACK")) {
                ConstExpNode constExpNode = new ConstExpNode(this,printDeep);
                addConstExpChildren(constExpNode);
                constExpNode.RunConstExpParser();
                WordProvider.CheckEndSign(constExpNode.getThisNodeLine(),this.getClass(),0,"RBRACK");
            } else {
                WordProvider.RollBackWord(1);
                break;
            }
        }
        // = InitVal
        Word word = WordProvider.GetNextWord();

        if (word != null && word.getCategoryCode().equals("ASSIGN")) {
            InitValNode initValNode = new InitValNode(this,printDeep);
            setInitValChildren(initValNode);
            initValNode.RunInitValParser();
        } else {
            WordProvider.RollBackWord(1);
        }
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<VarDef>");
    }

    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuple) {
        if (constExpChildren.isEmpty()) {
            SimpleVarDefTuple simpleVarDefTuple = new SimpleVarDefTuple
                    (ident.OriginWord,symbolTable.getOffsetRecord(),"int",false);
            if (symbolTable.queryTupleFromCurrentSymbolTable(ident.OriginWord,false) != null) {
                ErrorMessage.handleError(this.getClass(),1,ident);
            }
            symbolTable.addTuple(ident.OriginWord,simpleVarDefTuple);
            symbolTable.updateOffset(simpleVarDefTuple);
            DeclTupleInSymbolTable = simpleVarDefTuple;
        } else {
            RealArrayTuple realArrayTuple = new RealArrayTuple
                    (ident.OriginWord,symbolTable.getOffsetRecord(),"int",false);
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
            symbolTable.updateOffset(realArrayTuple);
            symbolTable.addTuple(ident.OriginWord,realArrayTuple);
            DeclTupleInSymbolTable = realArrayTuple;
        }
        if (initValChildren != null) {
            initValChildren.RunSymbolBuilder(symbolTable,null);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        if (initValChildren == null && constExpChildren.isEmpty()) {
            VariableOperand variableOperand = symbolTable.getFather() == null ? intermediateBuilder.putGlobalVariableAndReturn() :
                    intermediateBuilder.putNormalVariableAndReturn();
            DeclTupleInSymbolTable.setPrimaryOperand(variableOperand);
            intermediateBuilder.AddIntermediateExpression(OtherElements.createDeclElement(variableOperand));
            this.dst = variableOperand;
        } else if (constExpChildren.isEmpty()) {
            this.dst = symbolTable.getFather() == null ? intermediateBuilder.putGlobalVariableAndReturn() :
                    intermediateBuilder.putNormalVariableAndReturn();
            initValChildren.ToIntermediate(intermediateBuilder,symbolTable);
            DeclTupleInSymbolTable.setPrimaryOperand(this.dst);
        } else {
            int Value = ((RealArrayTuple)DeclTupleInSymbolTable).getElementNumber();
            PrimaryOperand const_src1 = intermediateBuilder.putIntConstAndReturnVariable(Value);
            VariableOperand variable = symbolTable.getFather() == null ? intermediateBuilder.putGlobalVariableAndReturn() : intermediateBuilder.putNormalVariableAndReturn();
            DeclTupleInSymbolTable.setPrimaryOperand(variable);
            intermediateBuilder.AddIntermediateExpression(MemoryElements.CreateAllocElement(variable,const_src1));
            this.dst = variable;
            if (initValChildren != null) {
                initValChildren.ToIntermediate(intermediateBuilder,symbolTable);
            }
        }
    }
}
