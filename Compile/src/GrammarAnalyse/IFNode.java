package GrammarAnalyse;


import IntermediateCode.Elements.BranchElement;
import IntermediateCode.Elements.OtherElements;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.TagOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.WordProvider;
import WordAnalyse.Word;

public class IFNode extends TreeNode{
    private Word IfTK;
    // 这是一个虚拟的非终结点
    private CondNode condChildren;
    private StmtNode stmtChildrenOne;
    private StmtNode stmtChildrenTwo;
    public IFNode(TreeNode Father,int deep) {
        super(Father,deep);
        condChildren = null;
        stmtChildrenOne = null;
        stmtChildrenTwo = null;
        IfTK = null;
    }

    public void setCondChildren(CondNode condChildren) {
        this.condChildren = condChildren;
    }

    public void setStmtChildrenOne(StmtNode stmtChildrenOne) {
        this.stmtChildrenOne = stmtChildrenOne;
    }

    public void setStmtChildrenTwo(StmtNode stmtChildrenTwo) {
        this.stmtChildrenTwo = stmtChildrenTwo;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        stringBuilder.append("if (");
        condChildren.treePrint(stringBuilder);
        stringBuilder.append(")\n");
        stmtChildrenOne.treePrint(stringBuilder);
        if (stmtChildrenTwo != null) {
            stringBuilder.append(deepToPrint(printDeep));
            stringBuilder.append("else\n");
            stmtChildrenTwo.treePrint(stringBuilder);
        }
    }

    @Override
    public Word getThisNodeLine() {
        return null;
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        condChildren.RunSymbolBuilder(symbolTable,null);
        stmtChildrenOne.RunSymbolBuilder(symbolTable,null);
        if (stmtChildrenTwo != null) {
            stmtChildrenTwo.RunSymbolBuilder(symbolTable,null);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        condChildren.ToIntermediate(intermediateBuilder,symbolTable);
        VariableOperand compareResult = (VariableOperand) condChildren.getDst();
        TagOperand if_entrance = intermediateBuilder.putTagOperandAndReturn("if_entrance");
        TagOperand if_exit = intermediateBuilder.putTagOperandAndReturn("if_exit");
        if (stmtChildrenTwo != null) {
            TagOperand else_entrance = intermediateBuilder.putTagOperandAndReturn("else_entrance");
            TagOperand else_exit = intermediateBuilder.putTagOperandAndReturn("else_exit");
            intermediateBuilder.AddIntermediateExpression(BranchElement.createBeqzElement(if_entrance,else_entrance,compareResult));
            intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(if_entrance));
            stmtChildrenOne.ToIntermediate(intermediateBuilder,symbolTable);
            intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(if_exit));
            intermediateBuilder.AddIntermediateExpression(OtherElements.createJumpElement(else_exit));
            intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(else_entrance));
            if(stmtChildrenTwo != null) {
                stmtChildrenTwo.ToIntermediate(intermediateBuilder,symbolTable);
            }
            intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(else_exit));
        } else {
            intermediateBuilder.AddIntermediateExpression(BranchElement.createBeqzElement(if_entrance,if_exit,compareResult));
            intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(if_entrance));
            stmtChildrenOne.ToIntermediate(intermediateBuilder,symbolTable);
            intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(if_exit));
        }
        // 条件
        // 跳转 Jum if_1 tag if_2_tag
        // if_1 tag
        // if_1 语句
        // if_1_end
        // jmp if_2_end
        // if_2 tag
        // if_2 语句
        // if_2_end
    }

    public void RunIFParser() {
        IfTK =  WordProvider.CheckEndSign(null,this.getClass(),-1,"IFTK");
        WordProvider.CheckEndSign(null,this.getClass(),-1,"LPARENT");
        CondNode condNode = new CondNode(this,printDeep);
        setCondChildren(condNode);
        condNode.RunCondParser();
        WordProvider.CheckEndSign(condNode.getThisNodeLine(),this.getClass(),0,"RPARENT");
        StmtNode stmtNodeOne = new StmtNode(this,printDeep);
        setStmtChildrenOne(stmtNodeOne);
        stmtNodeOne.RunStmtParser();
        Word word = WordProvider.GetNextWord();
        if (word != null && word.getCategoryCode().equals("ELSETK")) {
            StmtNode stmtNodeTwo = new StmtNode(this,printDeep);
            setStmtChildrenTwo(stmtNodeTwo);
            stmtNodeTwo.RunStmtParser();
        } else {
            WordProvider.RollBackWord(1);
        }
    }
}
