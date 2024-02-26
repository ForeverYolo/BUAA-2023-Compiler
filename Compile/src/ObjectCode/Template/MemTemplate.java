package ObjectCode.Template;

import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import ObjectCode.Assembly;
import ObjectCode.Instruction.PrimaryInstruction;

import java.util.ArrayList;

public class MemTemplate {
    public static int TempRegister = Register.v1.ordinal();
    public static int RealRegister = Register.v1.ordinal();
    public static void loadRegisterTemplate(int dst, int offset, int base, ArrayList<PrimaryInstruction> objectCode) {
        objectCode.add(PrimaryInstruction.createLw(dst,offset,base));
    }

    public static void storeRegisterTemplate(int dst,int offset,int base,ArrayList<PrimaryInstruction> objectCode) {
        objectCode.add(PrimaryInstruction.createSw(dst,offset,base));
    }

    public static void loadWordTemplate(PrimaryOperand dst, PrimaryOperand addr, Assembly assembly, ArrayList<PrimaryInstruction> objectCode) {
        int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
        if (addr instanceof ConstValue) {
            int addrValue = ((ConstValue) addr).getValue();
            if (Short.MIN_VALUE <= addrValue && addrValue <= Short.MAX_VALUE) {
                loadRegisterTemplate(dstRegister,addrValue,Register.zero.ordinal(),objectCode);
            } else {
                int offset = addrValue % Short.MAX_VALUE;
                int needToRegValue = addrValue - offset;
                RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,needToRegValue,objectCode);
                loadRegisterTemplate(dstRegister,offset,RealRegister,objectCode);
            }
        } else {
            int baseRegister = ((VariableOperand)addr).loadValueFromMemory(assembly);
            loadRegisterTemplate(dstRegister,0,baseRegister,objectCode);
        }
    }

    public static void storeWordTemplate(PrimaryOperand dst, PrimaryOperand addr, Assembly assembly, ArrayList<PrimaryInstruction> objectCode) {
        int dstRegister;
        if (dst instanceof ConstValue) {
            dstRegister = CalTemplate.loadConstToRegTemplate(TempRegister,((ConstValue) dst).getValue(),objectCode);
        } else {
            dstRegister = ((VariableOperand)dst).loadValueFromMemory(assembly);
        }
        if (addr instanceof ConstValue) {
            int addrValue = ((ConstValue) addr).getValue();
            if (Short.MIN_VALUE <= addrValue && addrValue <= Short.MAX_VALUE) {
                storeRegisterTemplate(dstRegister,addrValue,Register.zero.ordinal(),objectCode);
            } else {
                int offset = addrValue % Short.MAX_VALUE;
                int needToRegValue = addrValue - offset;
                RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,needToRegValue,objectCode);
                storeRegisterTemplate(dstRegister,offset,RealRegister,objectCode);
            }
        } else {
            int baseRegister = ((VariableOperand)addr).loadValueFromMemory(assembly);
            storeRegisterTemplate(dstRegister,0,baseRegister,objectCode);
        }
    }
}
