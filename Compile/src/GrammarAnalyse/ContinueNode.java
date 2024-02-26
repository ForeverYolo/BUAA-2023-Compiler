package GrammarAnalyse;

import IntermediateCode.Elements.OtherElements;
import IntermediateCode.IntermediateBuilder;
import SymbolTables.PrimaryTuple;
import SymbolTables.SymbolTable;
import Tools.ErrorMessage;
import Tools.WordProvider;
import WordAnalyse.Word;

public class ContinueNode extends TreeNode{
    public ForLoopNode loopNode;
    public Word ContinueTk;

    public ContinueNode(TreeNode Father,int deep) {
        super(Father,deep);
        ContinueTk = null;
        loopNode = null;
    }

    @Override
    public void treePrint(StringBuilder stringBuilder) {
        stringBuilder.append("continue;\n");
    }

    @Override
    public Word getThisNodeLine() {
        return ContinueTk;
    }

    @Override
    public void RunSymbolBuilder(SymbolTable symbolTable, PrimaryTuple tuples) {
        TreeNode testTreeNode = Father;
        while(testTreeNode != null) {
            if(testTreeNode instanceof ForLoopNode) {
                loopNode = (ForLoopNode) testTreeNode;
                return;
            }
            testTreeNode = testTreeNode.Father;
        }
        ErrorMessage.handleError(this.getClass(),0,ContinueTk);
    }

    @Override
    public void ToIntermediate(IntermediateBuilder intermediateBuilder, SymbolTable symbolTable) {
        intermediateBuilder.AddIntermediateExpression(OtherElements.createJumpElement(loopNode.getSelfAddTag()));
    }

    public void ContinueParser() {
        ContinueTk = WordProvider.CheckEndSign(null,this.getClass(),-1,"CONTINUETK");
        WordProvider.CheckEndSign(ContinueTk,this.getClass(),1,"SEMICN");
    }
}
