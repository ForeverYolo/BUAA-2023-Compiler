package ObjectCode.Template;

import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import IntermediateCode.Operands.VariableType;
import ObjectCode.Assembly;
import ObjectCode.Instruction.PrimaryInstruction;
import Optimize.BackOpt.MulDivOptimize;
import Optimize.BackOpt.MulOptimizeValue;
import Tools.GlobalSetting;

import java.util.ArrayList;

public class CalTemplate {
    public static int TempRegister = Register.v1.ordinal();
    public static int RealRegister = Register.v1.ordinal();
    public static void MoveTemplate(int dst, int src, ArrayList<PrimaryInstruction> objectCode) {
        objectCode.add(PrimaryInstruction.createAddu(dst,src, Register.zero.ordinal()));
    }

    public static int loadConstToRegTemplate(int dst,int value,ArrayList<PrimaryInstruction> objectCode) {
        if (value == 0 && dst == TempRegister) { //说明只是常规的临时加载。
            return Register.zero.ordinal();
        } else if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
            objectCode.add(PrimaryInstruction.createAddiu(dst,Register.zero.ordinal(),value));
        } else if (value >= 0 && value <= Short.MAX_VALUE - Short.MIN_VALUE) {
            objectCode.add(PrimaryInstruction.createOri(dst,Register.zero.ordinal(),value));
        } else if ((value & 0xffff) == 0) {
            objectCode.add(PrimaryInstruction.createLui(dst,(value >> 16) & 0xffff));
        } else {
            objectCode.add(PrimaryInstruction.createLui(dst,(value >> 16) & 0xffff));
            objectCode.add(PrimaryInstruction.createOri(dst,dst,(value & 0xffff)));
        }
        return dst;
    }

    public static void addImmTemplate(int dst,int src,int value,ArrayList<PrimaryInstruction> objectCode) {
        if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
            objectCode.add(PrimaryInstruction.createAddiu(dst,src,value));
        } else {
            RealRegister = loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createAddu(dst,src,RealRegister));
        }
    }

    public static void addTemplate(PrimaryOperand dst,PrimaryOperand src1,PrimaryOperand src2,Assembly assembly,ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            loadConstToRegTemplate(dstRegister,((ConstValue)src1).getValue()+((ConstValue)src2).getValue(),objectCode);
        } else if (src1 instanceof ConstValue) {
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue)src1).getValue();
            addImmTemplate(dstRegister,src2Register,value,objectCode);
        } else if (src2 instanceof ConstValue) {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue)src2).getValue();
            addImmTemplate(dstRegister,src1Register,value,objectCode);
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            objectCode.add(PrimaryInstruction.createAddu(dstRegister,src1Register,src2Register));
        }
    }

    public static void andTemplate(PrimaryOperand dst,PrimaryOperand src1,PrimaryOperand src2,Assembly assembly,ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            loadConstToRegTemplate(dstRegister,((ConstValue)src1).getValue() & ((ConstValue)src2).getValue(),objectCode);
        } else if (src1 instanceof ConstValue) {
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue)src1).getValue();
            RealRegister = loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createAnd(dstRegister,src2Register,RealRegister));
        } else if (src2 instanceof ConstValue) {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue)src2).getValue();
            RealRegister = loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createAnd(dstRegister,src1Register,RealRegister));
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            objectCode.add(PrimaryInstruction.createAnd(dstRegister,src1Register,src2Register));
        }
    }

    public static void xorTemplate(PrimaryOperand dst,PrimaryOperand src1,PrimaryOperand src2,Assembly assembly,ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            loadConstToRegTemplate(dstRegister,((ConstValue)src1).getValue() ^ ((ConstValue)src2).getValue(),objectCode);
        } else if (src1 instanceof ConstValue) {
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue)src1).getValue();
            RealRegister = loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createXor(dstRegister,src2Register,RealRegister));
        } else if (src2 instanceof ConstValue) {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue)src2).getValue();
            RealRegister = loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createXor(dstRegister,src1Register,RealRegister));
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            objectCode.add(PrimaryInstruction.createXor(dstRegister,src1Register,src2Register));
        }
    }

    public static void orTemplate(PrimaryOperand dst,PrimaryOperand src1,PrimaryOperand src2,Assembly assembly,ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            loadConstToRegTemplate(dstRegister,((ConstValue)src1).getValue() | ((ConstValue)src2).getValue(),objectCode);
        } else if (src1 instanceof ConstValue) {
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue)src1).getValue();
            RealRegister = loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createOr(dstRegister,src2Register,RealRegister));
        } else if (src2 instanceof ConstValue) {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue)src2).getValue();
            RealRegister = loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createOr(dstRegister,src1Register,RealRegister));
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            objectCode.add(PrimaryInstruction.createOr(dstRegister,src1Register,src2Register));
        }
    }

    public static void subTemplate(PrimaryOperand dst,PrimaryOperand src1,PrimaryOperand src2,Assembly assembly,ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            loadConstToRegTemplate(dstRegister,((ConstValue)src1).getValue()-((ConstValue)src2).getValue(),objectCode);
        } else if (src1 instanceof ConstValue) {
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            RealRegister = loadConstToRegTemplate(TempRegister,((ConstValue)src1).getValue(),objectCode);
            objectCode.add(PrimaryInstruction.createSubu(dstRegister,RealRegister,src2Register));
        } else if (src2 instanceof ConstValue) {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = -((ConstValue)src2).getValue();
            if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
                objectCode.add(PrimaryInstruction.createAddiu(dstRegister,src1Register,value));
            } else {
                RealRegister = loadConstToRegTemplate(TempRegister,((ConstValue)src2).getValue(),objectCode);
                objectCode.add(PrimaryInstruction.createSubu(dstRegister,src1Register,RealRegister));
            }
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            objectCode.add(PrimaryInstruction.createSubu(dstRegister,src1Register,src2Register));
        }
    }

    public static void mulTemplate(PrimaryOperand dst,PrimaryOperand src1,PrimaryOperand src2,Assembly assembly,ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            loadConstToRegTemplate(dstRegister,((ConstValue) src1).getValue() * ((ConstValue) src2).getValue(),objectCode);
        } else if (src1 instanceof ConstValue) {
            int value = ((ConstValue) src1).getValue();
            if (GlobalSetting.MulDivOptimize && MulDivOptimize.MulCanOptimize(value)) {
                MulDivOptimize.MulOptimize((VariableOperand) dst, (VariableOperand) src2,value,assembly);
            } else {
                int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
                int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
                RealRegister = loadConstToRegTemplate(TempRegister,value,objectCode);
                objectCode.add(PrimaryInstruction.createMult(RealRegister,src2Register));
                objectCode.add(PrimaryInstruction.createMflo(dstRegister));
            }
        } else if (src2 instanceof ConstValue) {
            int value = ((ConstValue) src2).getValue();
            if (GlobalSetting.MulDivOptimize && MulDivOptimize.MulCanOptimize(value)) {
                MulDivOptimize.MulOptimize((VariableOperand) dst, (VariableOperand) src1,value,assembly);
            } else {
                int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
                int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
                RealRegister = loadConstToRegTemplate(TempRegister,value,objectCode);
                objectCode.add(PrimaryInstruction.createMult(RealRegister,src1Register));
                objectCode.add(PrimaryInstruction.createMflo(dstRegister));
            }
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            objectCode.add(PrimaryInstruction.createMult(src1Register,src2Register));
            objectCode.add(PrimaryInstruction.createMflo(dstRegister));
        }
    }


    public static void divTemplate(PrimaryOperand dst,PrimaryOperand src1,PrimaryOperand src2,Assembly assembly,ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            loadConstToRegTemplate(dstRegister,((ConstValue) src1).getValue() / ((ConstValue) src2).getValue(),objectCode);
        } else if (src1 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int value = ((ConstValue) src1).getValue();
            RealRegister = loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createDiv(RealRegister,src2Register));
            objectCode.add(PrimaryInstruction.createMflo(dstRegister));
        } else if (src2 instanceof ConstValue) {
            int value = ((ConstValue) src2).getValue();
            if (GlobalSetting.MulDivOptimize) {
                MulDivOptimize.DivOptimize((VariableOperand) dst, (VariableOperand) src1,value,assembly);
            } else {
                int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
                int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
                RealRegister = loadConstToRegTemplate(TempRegister,value,objectCode);
                objectCode.add(PrimaryInstruction.createDiv(src1Register,RealRegister));
                objectCode.add(PrimaryInstruction.createMflo(dstRegister));
            }
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            objectCode.add(PrimaryInstruction.createDiv(src1Register,src2Register));
            objectCode.add(PrimaryInstruction.createMflo(dstRegister));
        }
    }

    public static void modTemplate(PrimaryOperand dst,PrimaryOperand src1,PrimaryOperand src2,Assembly assembly,ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            loadConstToRegTemplate(dstRegister,((ConstValue) src1).getValue() % ((ConstValue) src2).getValue(),objectCode);
        } else if (src1 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int value = ((ConstValue) src1).getValue();
            RealRegister = loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createDiv(RealRegister,src2Register));
            objectCode.add(PrimaryInstruction.createMfhi(dstRegister));
        } else if (src2 instanceof ConstValue) {
            int value = ((ConstValue) src2).getValue();
            if (GlobalSetting.MulDivOptimize) {
                MulDivOptimize.ModOptimize((VariableOperand) dst, (VariableOperand) src1,value,assembly);
            } else {
                int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
                int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
                RealRegister = loadConstToRegTemplate(TempRegister,value,objectCode);
                objectCode.add(PrimaryInstruction.createDiv(src1Register,RealRegister));
                objectCode.add(PrimaryInstruction.createMfhi(dstRegister));
            }
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            objectCode.add(PrimaryInstruction.createDiv(src1Register,src2Register));
            objectCode.add(PrimaryInstruction.createMfhi(dstRegister));
        }
    }


    public static void sllTemplate(PrimaryOperand dst,PrimaryOperand src1,PrimaryOperand src2,Assembly assembly,ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            loadConstToRegTemplate(dstRegister,((ConstValue) src1).getValue() << ((ConstValue) src2).getValue(),objectCode);
        } else if (src1 instanceof ConstValue) {
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue) src1).getValue();
            RealRegister = loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createSllv(dstRegister,RealRegister,src2Register));
        } else if (src2 instanceof ConstValue) {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            objectCode.add(PrimaryInstruction.createSll(dstRegister,src1Register,((ConstValue) src2).getValue()));
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            objectCode.add(PrimaryInstruction.createSllv(dstRegister,src1Register,src2Register));
        }
    }

    public static void loadAddrTemplate(PrimaryOperand dst,int offset,Assembly assembly,ArrayList<PrimaryInstruction> objectCode) {
        int RealOffset = offset * 4;
        int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
        addImmTemplate(dstRegister,((VariableOperand)dst).getVariableType() == VariableType.GLOBAL ?
                Register.gp.ordinal() : Register.sp.ordinal(),RealOffset,objectCode);
    }


}
