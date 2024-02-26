package GrammarAnalyse;


import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.FuncDefTuple;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.ErrorMessage;
import Tools.WordProvider;
import WordAnalyse.Word;

public class UnaryExpNode extends TreeNode {
    private UnaryOpNode unaryOpNode;
    private TreeNode children;
    public UnaryExpNode(TreeNode Father,int deep) {
        super(Father,deep);
        children = null;
        unaryOpNode = null;
    }

    public void SetChildren(TreeNode children) {
        this.children = children;
    }

    public void SetUnaryOp(UnaryOpNode unaryOpNode) {
        this.unaryOpNode = unaryOpNode;
    }

    public void treePrint(StringBuilder stringBuilder) {
        if (children instanceof PrimaryExpNode) {
            children.treePrint(stringBuilder);
        } else if (children instanceof FuncCallNode) {
            children.treePrint(stringBuilder);
        } else {
            unaryOpNode.treePrint(stringBuilder);
            children.treePrint(stringBuilder);
        }
    }

    @Override
    public Word getThisNodeLine() {
        if (unaryOpNode != null) {
            return unaryOpNode.getThisNodeLine();
        } else {
            return children.getThisNodeLine();
        }
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
       children.RunSymbolBuilder(symbolTable,tuples);
       ChangeNodeTypeAndDim(children);
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        children.ToIntermediate(intermediateBuilder,symbolTable);
        if (children instanceof PrimaryExpNode) {
            this.dst = children.getDst();
        } else if (children instanceof FuncCallNode) {
            FuncDefTuple funcDefTuple = (FuncDefTuple) symbolTable.queryTupleFromAllRelativeSymbolTable(((FuncCallNode)children).ident.OriginWord,true);
            this.dst = funcDefTuple.getRealReturnOperand();
        } else {
            VariableOperand temp = intermediateBuilder.putTempVariableAndReturn();
            this.dst = children.getDst();
            PrimaryOperand const_0 = intermediateBuilder.putIntConstAndReturnVariable(0);
            if (unaryOpNode.getUnaryOp().getCategoryCode().equals("PLUS")) {
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createAddElement(temp,const_0,this.dst));
            } else if (unaryOpNode.getUnaryOp().getCategoryCode().equals("MINU")) {
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createSubElement(temp,const_0,this.dst));
            } else {
                intermediateBuilder.AddIntermediateExpression(CalculateElement.createSeqElement(temp,const_0,this.dst));
            }
            this.dst = temp;
        }
    }

    public void RunUnaryExpParser() {
        Word word = WordProvider.GetNextWord();
        if (word != null && word.getCategoryCode().equals("IDENFR")) {
            word = WordProvider.GetNextWord();
            if (word != null && word.getCategoryCode().equals("LPARENT")) {
                WordProvider.RollBackWord(2);
                FuncCallNode funcCallNode = new FuncCallNode(this,printDeep);
                SetChildren(funcCallNode);
                funcCallNode.FuncCallParser();
            } else {
                WordProvider.RollBackWord(2);
                PrimaryExpNode primaryExpNode = new PrimaryExpNode(this,printDeep);
                SetChildren(primaryExpNode);
                primaryExpNode.RunPrimaryExpParser();
            }
        } else if (word != null && (word.getCategoryCode().equals("PLUS")
                || word.getCategoryCode().equals("MINU")
                || word.getCategoryCode().equals("NOT"))) {
            WordProvider.RollBackWord(1);
            UnaryOpNode unaryOpNode = new UnaryOpNode(this,printDeep);
            SetUnaryOp(unaryOpNode);
            unaryOpNode.RunUnaryOpParser();

            UnaryExpNode unaryExpNode = new UnaryExpNode(this,printDeep);
            SetChildren(unaryExpNode);
            unaryExpNode.RunUnaryExpParser();
        } else {
            WordProvider.RollBackWord(1);
            PrimaryExpNode primaryExpNode = new PrimaryExpNode(this,printDeep);
            SetChildren(primaryExpNode);
            primaryExpNode.RunPrimaryExpParser();
        }
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<UnaryExp>");
    }

    public int calculateValue(SymbolTable CalculateNeededTable) {
        int value = 0;
        if (children instanceof PrimaryExpNode) {
            value = ((PrimaryExpNode)children).calculateValue(CalculateNeededTable);
        } else if (children instanceof FuncCallNode) {
            ErrorMessage.handleError(this.getClass(),-1,null);
        } else {
            value = ((UnaryExpNode)children).calculateValue(CalculateNeededTable);
            String operator = unaryOpNode.getUnaryOp().getCategoryCode();
            if (operator.equals("MINU")) {
                value = -value;
            } else if (!operator.equals("PLUS")) {
                ErrorMessage.handleError(this.getClass(),-1,null);
            }
        }
        return value;
    }
}
