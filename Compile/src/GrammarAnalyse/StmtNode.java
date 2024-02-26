package GrammarAnalyse;

import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.CheckPoint;
import Tools.GlobalSetting;
import Tools.ErrorMessage;
import Tools.WordProvider;
import WordAnalyse.Word;

public class StmtNode extends TreeNode {
    private TreeNode MainChild;

    public StmtNode(TreeNode Father,int deep) {
        super(Father,deep);
        MainChild = null;
    }

    public void setMainChild(TreeNode mainChild) {
        MainChild = mainChild;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        if (MainChild != null) {
            MainChild.treePrint(stringBuilder);
            if (!(MainChild instanceof ForLoopNode) && !(MainChild instanceof IFNode)) {
                stringBuilder.append("\n");
            }
        } else {
            stringBuilder.append(";\n");
        }
    }

    @Override
    public Word getThisNodeLine() {
        return MainChild.getThisNodeLine();
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        if (MainChild != null) {
            MainChild.RunSymbolBuilder(symbolTable,tuples);
        }
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        if (MainChild != null) {
            MainChild.ToIntermediate(intermediateBuilder,symbolTable);
        }
    }

    public ReturnNode SearchReturnNode() {
        if (MainChild instanceof ReturnNode) {
            return (ReturnNode) MainChild;
        } else {
            return null;
        }
    }

    public void RunStmtParser() {
        Word word = WordProvider.GetNextWord();
        // Lval = Exp; / Lval = getint() 和 [Exp];重合部分
        if (word != null && word.getCategoryCode().equals("IDENFR")) {
            word = WordProvider.GetNextWord();
            if (word != null && (word.getCategoryCode().equals("ASSIGN") || word.getCategoryCode().equals("LBRACK"))) {
                WordProvider.RollBackWord(2);
                CheckPoint checkPoint = new CheckPoint(WordProvider.WaitPrintQueue,WordProvider.WriteBuffer,WordProvider.NowIndex);
                LvalNode lvalNode = new LvalNode(null,printDeep);
                lvalNode.RunLvalParser();
                word = WordProvider.GetNextWord();
                if (word != null && word.getCategoryCode().equals("ASSIGN")) {
                    word = WordProvider.GetNextWord();
                    if (word != null && word.getCategoryCode().equals("GETINTTK")) {
                        WordProvider.CheckEndSign(null,this.getClass(),-1,"LPARENT");
                        WordProvider.CheckEndSign(lvalNode.getThisNodeLine(),this.getClass(),0,"RPARENT");
                        //分配节点
                        GetIntNode getIntNode = new GetIntNode(this,printDeep);
                        setMainChild(getIntNode);
                        getIntNode.setLvalChildren(lvalNode);
                        lvalNode.setFather(getIntNode);
                        WordProvider.CheckEndSign(lvalNode.getThisNodeLine(),this.getClass(),1,"SEMICN");
                    } else {
                        WordProvider.RollBackWord(1);
                        ExpNode expNode = new ExpNode(this,printDeep);
                        expNode.RunExpParser();
                        //分配节点
                        StmtLvalExpNode stmtLvalExpNode = new StmtLvalExpNode(this,printDeep);
                        setMainChild(stmtLvalExpNode);
                        stmtLvalExpNode.setExpChilren(expNode);
                        stmtLvalExpNode.setLvalChildren(lvalNode);
                        expNode.setFather(stmtLvalExpNode);
                        lvalNode.setFather(stmtLvalExpNode);
                        WordProvider.CheckEndSign(expNode.getThisNodeLine(),this.getClass(),2,"SEMICN");
                    }
                } else {
                    checkPoint.Restore();
                    ExpNode expNode = new ExpNode(this,printDeep);
                    expNode.RunExpParser();
                    setMainChild(expNode);
                    WordProvider.CheckEndSign(expNode.getThisNodeLine(),this.getClass(),2,"SEMICN");
                }
            }
            else {
                WordProvider.RollBackWord(2);
                ExpNode expNode = new ExpNode(this,printDeep);
                expNode.RunExpParser();
                setMainChild(expNode);
                WordProvider.CheckEndSign(expNode.getThisNodeLine(),this.getClass(),3,"SEMICN");
            }
        }
        // Block
        else if (word != null && word.getCategoryCode().equals("LBRACE")) {
            WordProvider.RollBackWord(1);
            BlockNode blockNode = new BlockNode(this,printDeep + 1);
            setMainChild(blockNode);
            blockNode.RunBlockParser();
        }
        // if
        else if (word != null && word.getCategoryCode().equals("IFTK")) {
            WordProvider.RollBackWord(1);
            IFNode ifNode = new IFNode(this,printDeep);
            setMainChild(ifNode);
            ifNode.RunIFParser();
        }
        // for
        else if (word != null && word.getCategoryCode().equals("FORTK")) {
            WordProvider.RollBackWord(1);
            ForLoopNode forLoopNode = new ForLoopNode(this,printDeep);
            setMainChild(forLoopNode);
            forLoopNode.ForLoopParser();
         }
        // break
        else if (word != null && word.getCategoryCode().equals("BREAKTK")) {
            WordProvider.RollBackWord(1);
            BreakNode breakNode = new BreakNode(this,printDeep);
            setMainChild(breakNode);
            breakNode.BreakParser();
        }
        // continue
        else if (word != null && word.getCategoryCode().equals("CONTINUETK")) {
            WordProvider.RollBackWord(1);
            ContinueNode continueNode = new ContinueNode(this,printDeep);
            setMainChild(continueNode);
            continueNode.ContinueParser();
        }
        // return
        else if (word != null && word.getCategoryCode().equals("RETURNTK")) {
            WordProvider.RollBackWord(1);
            ReturnNode returnNode = new ReturnNode(this,printDeep);
            setMainChild(returnNode);
            returnNode.ReturnParser();
        }
        // printf
        else if (word != null && word.getCategoryCode().equals("PRINTFTK")) {
            WordProvider.RollBackWord(1);
            PrintfNode printfNode = new PrintfNode(this,printDeep);
            setMainChild(printfNode);
            printfNode.PrintfParser();
        }
        // [Exp];
        else if (word != null && !word.getCategoryCode().equals("SEMICN")) {
            WordProvider.RollBackWord(1);
            ExpNode expNode = new ExpNode(this,printDeep);
            setMainChild(expNode);
            expNode.RunExpParser();
            WordProvider.CheckEndSign(expNode.getThisNodeLine(),this.getClass(),4,"SEMICN");
        }
        else if (word == null || !word.getCategoryCode().equals("SEMICN")) {
            ErrorMessage.handleError(this.getClass(),-1,null);
        }
        WordProvider.FileAndClearWaitPrintQueue();
        WordProvider.WriteBuffer.add("<Stmt>");
    }

}
