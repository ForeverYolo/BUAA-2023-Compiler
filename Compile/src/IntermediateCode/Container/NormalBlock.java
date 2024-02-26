package IntermediateCode.Container;

import IntermediateCode.Elements.BranchElement;
import IntermediateCode.Elements.OtherElements;
import IntermediateCode.Elements.PrimaryElement;
import IntermediateCode.Operands.VariableOperand;
import ObjectCode.Assembly;
import ObjectCode.Instruction.PrimaryInstruction;
import Tools.ErrorMessage;
import Tools.GlobalSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class NormalBlock extends PrimaryBlock{
    private int Deep;
    private int id;
    private FuncBlock funcBlock;
    protected HashMap<Integer,HashSet<VariableOperand>> FuncCallActiveMap;

    public NormalBlock(int id, FuncBlock funcBlock) {
        super();
        Deep = 0;
        this.id = id;
        this.funcBlock = funcBlock;
        this.FuncCallActiveMap = new HashMap<>();
    }

    public HashMap<Integer, HashSet<VariableOperand>> getFuncCallActiveMap() {
        return FuncCallActiveMap;
    }

    public int getId() {
        return id;
    }

    public int CalculateSpace(int startSpace) {
        int space = startSpace;
        for(PrimaryElement primaryElement : IntermediateExpression) {
            int newSpace = primaryElement.getSpace();
            primaryElement.setAddress(space);
            space += newSpace;
        }
        return space;
    }


    /* public void ToAssembly(Assembly assembly) {
        for (int i = 0; i < IntermediateExpression.size(); i++) {
            PrimaryElement primaryElement = IntermediateExpression.get(i);
            //-----------------------------------------------保证写回一致性----------------------------------------------
            if (primaryElement.getOperatorName().equals("Tag") || primaryElement.getOperatorName().equals("Jump")) {
                if (((OtherElements)primaryElement).getOperand().getOperandName().contains("Loop_CondCheck")) {
                    assembly.FlushLocalRegister();
                }
            }
            primaryElement.setPlaceMessage(this,i);
            assembly.getObjectCode().add(PrimaryInstruction.createComment(primaryElement.toString(),2));
            primaryElement.ToAssembly(assembly);
        }
    } */

    public void ToAssembly(Assembly assembly) {
        if (GlobalSetting.RegisterOptimize) {
            assembly.getObjectCode().add(PrimaryInstruction.createComment(this.toString(),2));
            for (int i = 0; i < IntermediateExpression.size() - 1; i++) {
                PrimaryElement primaryElement = IntermediateExpression.get(i);
                primaryElement.setPlaceMessage(this,i);
                assembly.getObjectCode().add(PrimaryInstruction.createComment(primaryElement.toString(),2));
                primaryElement.ToAssembly(assembly);
                //如果是内存访问的变量，需要写回。
                primaryElement.getDefineVariable().forEach(var -> {
                    if (var.isMemVisit()) {
                        assembly.WriteBackVariable(var);
                    }
                });
            }
            if (!IntermediateExpression.isEmpty()) {
                PrimaryElement lastElement = getLastMediateExpression();
                if (lastElement instanceof BranchElement || (lastElement instanceof OtherElements otherElements && (OtherElements.isJump(otherElements)|| OtherElements.isReturn(otherElements)))) {
                    assembly.JustWriteBackGlobalInLocalRegister();
                    lastElement.setPlaceMessage(this,IntermediateExpression.size() - 1);
                    assembly.getObjectCode().add(PrimaryInstruction.createComment(lastElement.toString(),2));
                    lastElement.ToAssembly(assembly);
                    assembly.FlushAllLocalRegister();
                } else {
                    lastElement.setPlaceMessage(this,IntermediateExpression.size() - 1);
                    assembly.getObjectCode().add(PrimaryInstruction.createComment(lastElement.toString(),2));
                    lastElement.ToAssembly(assembly);
                    lastElement.getDefineVariable().forEach(var -> {
                        if (var.isMemVisit()) {
                            assembly.WriteBackVariable(var);
                        }
                    });
                    assembly.JustWriteBackGlobalInLocalRegister();
                    assembly.FlushAllLocalRegister();
                }
            }
        } else {
            for (int i = 0; i < IntermediateExpression.size(); i++) {
                PrimaryElement primaryElement = IntermediateExpression.get(i);
                primaryElement.setPlaceMessage(this,i);
                assembly.getObjectCode().add(PrimaryInstruction.createComment(primaryElement.toString(),2));
                primaryElement.ToAssembly(assembly);
                assembly.WriteBackAllLocalRegister();
            }
        }
    }



    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NormalBlock normalBlock) {
            return normalBlock.id == this.id;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    public PrimaryElement getLastMediateExpression() {
        int size = IntermediateExpression.size();
        if (size == 0) {
            ErrorMessage.handleSelfCheckError(this.getClass());
        }
        return IntermediateExpression.get(IntermediateExpression.size() - 1);
    }

    public PrimaryElement getFirstMediateExpression() {
        int size = IntermediateExpression.size();
        if (size == 0) {
            ErrorMessage.handleSelfCheckError(this.getClass());
        }
        return IntermediateExpression.get(0);
    }

    @Override
    public String toString() {
        return "B-" + this.id;
    }
}
