package IntermediateCode.Elements;

import IntermediateCode.Container.PrimaryBlock;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import ObjectCode.Assembly;

import java.util.ArrayList;

public class ParallelCopyElement extends PrimaryElement {
    protected VariableOperand Dst;
    protected PrimaryOperand Src;

    public ParallelCopyElement(VariableOperand Dst, PrimaryOperand Src) {
        super("ParallelCopy");
        this.Dst = Dst;
        this.Src = Src;
    }

    public static ParallelCopyElement CreateParallelCopyElement(VariableOperand Dst,PrimaryOperand Src) {
        return new ParallelCopyElement(Dst,Src);
    }

    @Override
    public String toString() {
        return "ParallelCopy " + Dst.toString() + " <- " + Src.toString();
    }

    @Override
    public void setPlaceMessage(PrimaryBlock block, int Index) {}

    @Override
    public void setAddress(int address) {}

    @Override
    public int getSpace() {return 0;}

    @Override
    public void ToAssembly(Assembly assembly) {}

    @Override
    public ArrayList<VariableOperand> getUsedVariable() {
        ArrayList<VariableOperand> temp = new ArrayList<>();
        if (Src instanceof VariableOperand var) {
            temp.add(var);
        }
        return temp;
    }


    @Override
    public ArrayList<PrimaryOperand> getUsed() {
        ArrayList<PrimaryOperand> temp = new ArrayList<>();
        if (Src != null) {
            temp.add(Src);
        }
        return temp;
    }

    @Override
    public ArrayList<VariableOperand> getDefineVariable() {
        ArrayList<VariableOperand> temp = new ArrayList<>();
        if (Dst != null) {
            temp.add(Dst);
        }
        return temp;
    }

    @Override
    public ArrayList<PrimaryOperand> getDefine() {
        ArrayList<PrimaryOperand> temp = new ArrayList<>();
        if (Dst != null) {
            temp.add(Dst);
        }
        return temp;
    }

    @Override
    public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {
        if (Dst == origin) {
            Dst = (VariableOperand) target;
        }
    }

    @Override
    public void ReplaceUseVariable(PrimaryOperand operand, PrimaryOperand target) {
        if (Src == operand) {
            Src = target;
        }
    }
}
