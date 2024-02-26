package GrammarAnalyse;


import IntermediateCode.Elements.BranchElement;
import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.Elements.OtherElements;
import IntermediateCode.IntermediateBuilder;
import IntermediateCode.Operands.TagOperand;
import IntermediateCode.Operands.VariableOperand;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.CheckPoint;
import Tools.ErrorMessage;
import Tools.WordProvider;
import WordAnalyse.Word;

import javax.swing.text.html.HTML;

public class ForLoopNode extends TreeNode{
    // 这是一个虚拟的非终结点
    //  'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    private TagOperand startTag;
    private TagOperand selfAddTag;
    private TagOperand endTag;
    private Word forTK;
    private ForStmtNode forStmtChildrenOne;
    private CondNode condChild;
    private ForStmtNode forStmtChildrenTwo;
    private StmtNode stmtChildren;
    public ForLoopNode(TreeNode Father,int deep) {
        super(Father,deep);
        forStmtChildrenOne = null;
        condChild = null;
        forStmtChildrenTwo = null;
        stmtChildren = null;
        forTK = null;
    }

    public void setForStmtChildrenOne(ForStmtNode forStmtChildrenOne) {
        this.forStmtChildrenOne = forStmtChildrenOne;
    }

    public void setCondChild(CondNode condChild) {
        this.condChild = condChild;
    }

    public void setForStmtChildrenTwo(ForStmtNode forStmtChildrenTwo) {
        this.forStmtChildrenTwo = forStmtChildrenTwo;
    }

    public void setStmtChildren(StmtNode stmtChildren) {
        this.stmtChildren = stmtChildren;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        stringBuilder.append("for(");
        if (forStmtChildrenOne != null) {
            forStmtChildrenOne.treePrint(stringBuilder);
        }
        stringBuilder.append(";");
        if (condChild != null) {
            condChild.treePrint(stringBuilder);
        }
        stringBuilder.append(";");
        if (forStmtChildrenTwo != null) {
            forStmtChildrenTwo.treePrint(stringBuilder);
        }
        stringBuilder.append(")\n");
        stmtChildren.treePrint(stringBuilder);
    }

    @Override
    public Word getThisNodeLine() {
        return forTK;
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        if (forStmtChildrenOne != null) {
            forStmtChildrenOne.RunSymbolBuilder(symbolTable,null);
        }
        if (condChild != null) {
            condChild.RunSymbolBuilder(symbolTable,null);
        }
        if (forStmtChildrenTwo != null) {
            forStmtChildrenTwo.RunSymbolBuilder(symbolTable,null);
        }
        stmtChildren.RunSymbolBuilder(symbolTable,null);
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        if (forStmtChildrenOne != null) {
            forStmtChildrenOne.ToIntermediate(intermediateBuilder,symbolTable);
        }
        TagOperand condCheck = intermediateBuilder.putTagOperandAndReturn("Loop_CondCheck");
        TagOperand start_tag = intermediateBuilder.putTagOperandAndReturn("Loop_Start");
        TagOperand selfAdd = intermediateBuilder.putTagOperandAndReturn("Loop_SelfAdd");
        startTag = condCheck;
        TagOperand exit_tag = intermediateBuilder.putTagOperandAndReturn("Loop_exit");
        endTag = exit_tag;
        selfAddTag = selfAdd;
        intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(condCheck));
        if (condChild != null) {
            condChild.ToIntermediate(intermediateBuilder,symbolTable);
            VariableOperand compareResult = (VariableOperand) condChild.dst;
            intermediateBuilder.AddIntermediateExpression(BranchElement.createBeqzElement(start_tag,exit_tag,compareResult));
        }
        intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(start_tag));
        stmtChildren.ToIntermediate(intermediateBuilder,symbolTable);
        intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(selfAdd));
        if (forStmtChildrenTwo != null) {
            forStmtChildrenTwo.ToIntermediate(intermediateBuilder,symbolTable);
        }
        intermediateBuilder.AddIntermediateExpression(OtherElements.createJumpElement(condCheck));
        intermediateBuilder.AddIntermediateExpression(OtherElements.createTagElement(exit_tag));
        // 初始化
        // 入口标签
        // 判断
        // 执行循环块
        // 自增
        // 跳转入口标签
        // 出口标签
    }

    public TagOperand getSelfAddTag() {
        return selfAddTag;
    }

    public TagOperand getStartTag() {
        return startTag;
    }

    public TagOperand getEndTag() {
        return endTag;
    }


    public void ForLoopParser() {
        forTK =  WordProvider.CheckEndSign(null,this.getClass(),-1,"FORTK");
        WordProvider.CheckEndSign(null,this.getClass(),-1,"LPARENT");
        Word word = WordProvider.GetNextWord();

        if (word != null && word.getCategoryCode().equals("IDENFR")) {
            WordProvider.RollBackWord(1);
            CheckPoint checkPoint = new CheckPoint(WordProvider.WaitPrintQueue,WordProvider.WriteBuffer,WordProvider.NowIndex);
            LvalNode lvalNode = new LvalNode(this,printDeep);
            lvalNode.RunLvalParser();
            word = WordProvider.GetNextWord();
            if (word != null && word.getCategoryCode().equals("ASSIGN")) {
                /* 说明真的是ForStmt */
                checkPoint.Restore();
                ForStmtNode forStmtNodeOne = new ForStmtNode(this,printDeep);
                setForStmtChildrenOne(forStmtNodeOne);
                forStmtNodeOne.RunForStmtParser();
                WordProvider.CheckEndSign(forStmtNodeOne.getThisNodeLine(),this.getClass(),0,"SEMICN");
            } else {
                /* 说明是Cond,那说明少分号 */
                WordProvider.RollBackWord(1);
                ErrorMessage.handleError(this.getClass(),1,WordProvider.GetLastWord());
            }
        } else if (word != null && !word.getCategoryCode().equals("SEMICN")) {
            WordProvider.RollBackWord(1);
            ErrorMessage.handleError(this.getClass(), 2,WordProvider.GetLastWord());
        }

        word = WordProvider.GetNextWord();
        if (word != null && word.getCategoryCode().equals("IDENFR")) {
            WordProvider.RollBackWord(1);
            CheckPoint checkPoint = new CheckPoint(WordProvider.WaitPrintQueue,WordProvider.WriteBuffer,WordProvider.NowIndex);
            LvalNode lvalNode = new LvalNode(this,printDeep);
            lvalNode.RunLvalParser();
            word = WordProvider.GetNextWord();
            if (word != null && !word.getCategoryCode().equals("ASSIGN")) {
                /* 说明真的是Cond */
                checkPoint.Restore();
                CondNode condNode = new CondNode(this,printDeep);
                setCondChild(condNode);
                condNode.RunCondParser();
                WordProvider.CheckEndSign(condNode.getThisNodeLine(),this.getClass(),3,"SEMICN");
            } else {
                /* 说明是ForStmt,那说明少分号 */
                WordProvider.RollBackWord(1);
                ErrorMessage.handleError(this.getClass(),4,WordProvider.GetLastWord());
            }
        } else if (word != null && WordProvider.FindInCondFirst(word)) {
            WordProvider.RollBackWord(1);
            CondNode condNode = new CondNode(this,printDeep);
            setCondChild(condNode);
            condNode.RunCondParser();
            WordProvider.CheckEndSign(condNode.getThisNodeLine(),this.getClass(),5,"SEMICN");
        } else if (word != null && !word.getCategoryCode().equals("SEMICN")) {
            WordProvider.RollBackWord(1);
            ErrorMessage.handleError(this.getClass(), 6, WordProvider.GetLastWord());
        }


        word = WordProvider.GetNextWord();
        if (word != null && word.getCategoryCode().equals("IDENFR")) {
            WordProvider.RollBackWord(1);
            ForStmtNode forStmtNodeTwo = new ForStmtNode(this,printDeep);
            setForStmtChildrenTwo(forStmtNodeTwo);
            forStmtNodeTwo.RunForStmtParser();
            WordProvider.CheckEndSign(WordProvider.GetLastWord(),this.getClass(),8,"RPARENT");
        } else if (word != null && !word.getCategoryCode().equals("RPARENT")) {
            WordProvider.RollBackWord(1);
            ErrorMessage.handleError(this.getClass(),8,WordProvider.GetLastWord());
        }


        StmtNode stmtNode = new StmtNode(this,printDeep);
        setStmtChildren(stmtNode);
        stmtNode.RunStmtParser();
    }

    public TagOperand getStopTag() {
        return endTag;
    }
}
