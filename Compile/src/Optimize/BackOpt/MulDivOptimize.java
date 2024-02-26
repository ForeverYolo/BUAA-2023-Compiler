package Optimize.BackOpt;

import IntermediateCode.Operands.VariableOperand;
import ObjectCode.Assembly;
import ObjectCode.Instruction.PrimaryInstruction;
import ObjectCode.Template.CalTemplate;
import ObjectCode.Template.Register;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Objects;

public class MulDivOptimize {
    protected static int Shift;
    protected static int LargeOneBit;
    protected static long Mutiplier;
    protected static HashMap<Integer,MulOptimizeValue> OptValueMap = new HashMap<>();

    static {
        ArrayList<MulOptimizeValue> OptimizeValues = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            OptimizeValues.add(new MulOptimizeValue(i));
            OptimizeValues.add(new MulOptimizeValue(i | 0x80000000));
            for (int j = 0; j < 32; j++) {
                OptimizeValues.add(new MulOptimizeValue(i,j));
                OptimizeValues.add(new MulOptimizeValue(i | 0x80000000,j));
                OptimizeValues.add(new MulOptimizeValue(i,j | 0x80000000));
                OptimizeValues.add(new MulOptimizeValue(i | 0x80000000,j | 0x80000000));
                for(int k = 0; k < 32; k++) {
                    OptimizeValues.add(new MulOptimizeValue(i,j,k));
                    OptimizeValues.add(new MulOptimizeValue(i | 0x80000000,j,k));
                    OptimizeValues.add(new MulOptimizeValue(i,j | 0x80000000,k));
                    OptimizeValues.add(new MulOptimizeValue(i,j,k | 0x80000000));
                    OptimizeValues.add(new MulOptimizeValue(i | 0x80000000,j | 0x80000000,k));
                    OptimizeValues.add(new MulOptimizeValue(i | 0x80000000,j,k | 0x80000000));
                    OptimizeValues.add(new MulOptimizeValue(i,j | 0x80000000,k | 0x80000000));
                    OptimizeValues.add(new MulOptimizeValue(i | 0x80000000,j | 0x80000000,k | 0x80000000));
                }
            }
        }
        OptimizeValues.stream().filter(MulOptimizeValue::CheckBetter).forEach(mulOptimizeValue -> {
            if (!OptValueMap.containsKey(mulOptimizeValue.value) || OptValueMap.get(mulOptimizeValue.value).price > mulOptimizeValue.price) {
                OptValueMap.put(mulOptimizeValue.value,mulOptimizeValue);
            }
        });
    }

    public static void CalculateMutiplier(int DivNumber) {
        LargeOneBit = 0;
        BigInteger DivNum = BigInteger.valueOf(DivNumber).abs();
        while(DivNum.compareTo(BigInteger.valueOf(1).shiftLeft(LargeOneBit)) > 0) {
            LargeOneBit++;
        }
        Shift = LargeOneBit;
        long LeftValue = BigInteger.valueOf(1).shiftLeft(32 + LargeOneBit).divide(DivNum).longValue();
        long RightValue = BigInteger.valueOf(1).shiftLeft(32 + LargeOneBit)
                .add(BigInteger.valueOf(1).shiftLeft(1 + LargeOneBit)).divide(DivNum).longValue();
        while(RightValue >> 1 > LeftValue >> 1 && Shift > 0) {
            RightValue >>= 1;
            LeftValue >>= 1;
            Shift--;
        }
        Mutiplier = RightValue;
    }

    //----------------------------------------------乘法-------------------------------------------
    public static boolean MulCanOptimize(int value) {
        return OptValueMap.containsKey(value);
    }
    public static void MulOptimize(VariableOperand dst, VariableOperand src, int Value, Assembly assembly) {
        int dstRegister = dst.allocateRegister(assembly);
        int srcRegister = src.loadValueFromMemory(assembly);
        OptValueMap.get(Value).MulTemplate(dstRegister,srcRegister,assembly.getObjectCode());
    }

    //---------------------------------------------除法----------------------------------------------
    public static void DivTemplate(int dst, int src, ArrayList<PrimaryInstruction> objectCode) {
        if (Mutiplier < Integer.MAX_VALUE) {
            CalTemplate.loadConstToRegTemplate(Register.a0.ordinal(), (int)Mutiplier, objectCode);
            objectCode.add(PrimaryInstruction.createMult(Register.a0.ordinal(),src));
            objectCode.add(PrimaryInstruction.createMfhi(Register.a0.ordinal()));
            objectCode.add(PrimaryInstruction.createSra(Register.v1.ordinal(),Register.a0.ordinal(),Shift));
        }
        else {
            CalTemplate.loadConstToRegTemplate(Register.a0.ordinal(), (int)(Mutiplier - (1L << 32)), objectCode);
            objectCode.add(PrimaryInstruction.createMult(Register.a0.ordinal(),src));
            objectCode.add(PrimaryInstruction.createMfhi(Register.a0.ordinal()));
            objectCode.add(PrimaryInstruction.createAddu(Register.v1.ordinal(),Register.a0.ordinal(),src));
            objectCode.add(PrimaryInstruction.createSra(Register.v1.ordinal(),Register.v1.ordinal(),Shift));
        }
        // 负数的值向上取整
        objectCode.add(PrimaryInstruction.createSlt(Register.a0.ordinal(),src,Register.zero.ordinal()));
        objectCode.add(PrimaryInstruction.createAddu(dst, Register.v1.ordinal(), Register.a0.ordinal()));
    }


    public static void DivOptimize(VariableOperand dst,VariableOperand src, int value,Assembly assembly) {
        CalculateMutiplier(value);
        int dstRegister = dst.allocateRegister(assembly);
        int srcRegister = src.loadValueFromMemory(assembly);
        //这里应该不会出现，根据GVN，但是写了总比不写强。
        if (Math.abs(value) == 1) {
            CalTemplate.loadConstToRegTemplate(dstRegister,srcRegister,assembly.getObjectCode());
        } else if (Integer.bitCount(Math.abs(value)) == 1) {
            assembly.getObjectCode().add(PrimaryInstruction.createSra(Register.a0.ordinal(),srcRegister,LargeOneBit - 1));
            if (LargeOneBit != 32) {
                assembly.getObjectCode().add(PrimaryInstruction.createSrl(Register.a0.ordinal(),Register.a0.ordinal(),32-LargeOneBit));
            }
            assembly.getObjectCode().add(PrimaryInstruction.createAddu(dstRegister,srcRegister,Register.a0.ordinal()));
            assembly.getObjectCode().add(PrimaryInstruction.createSra(dstRegister,dstRegister,LargeOneBit));
        } else {
            DivTemplate(dstRegister,srcRegister,assembly.getObjectCode());
        }
        if (value < 0) {
            assembly.getObjectCode().add(PrimaryInstruction.createSubu(dstRegister,Register.zero.ordinal(),dstRegister));
        }
    }

    //-------------------------------------------------------取模------------------------------------------------------------------
    public static void ModOptimize(VariableOperand dst,VariableOperand src, int value,Assembly assembly) {
        CalculateMutiplier(value);
        int dstRegister = dst.allocateRegister(assembly);
        int srcRegister = src.loadValueFromMemory(assembly);
        //这里应该不会出现，根据GVN，但是写了总比不写强。
        if (Math.abs(value) == 1) {
            CalTemplate.loadConstToRegTemplate(dstRegister,Register.zero.ordinal(),assembly.getObjectCode());
        }
        else if (Integer.bitCount(Math.abs(value)) == 1) {
            assembly.getObjectCode().add(PrimaryInstruction.createSra(Register.a0.ordinal(),srcRegister,LargeOneBit - 1));
            if (LargeOneBit != 32) {
                assembly.getObjectCode().add(PrimaryInstruction.createSrl(Register.a0.ordinal(),Register.a0.ordinal(),32-LargeOneBit));
            }
            assembly.getObjectCode().add(PrimaryInstruction.createAddu(Register.a0.ordinal(),srcRegister,Register.a0.ordinal()));
            assembly.getObjectCode().add(PrimaryInstruction.createSra(Register.a0.ordinal(),Register.a0.ordinal(),LargeOneBit));
            if (value < 0) {
                assembly.getObjectCode().add(PrimaryInstruction.createSubu(Register.a0.ordinal(),Register.zero.ordinal(),Register.a0.ordinal()));
            }
            assembly.getObjectCode().add(PrimaryInstruction.createSll(Register.a0.ordinal(),Register.a0.ordinal(),LargeOneBit));
            assembly.getObjectCode().add(PrimaryInstruction.createSubu(dstRegister,srcRegister,Register.a0.ordinal()));
        } else {
            DivTemplate(Register.v1.ordinal(),srcRegister,assembly.getObjectCode());
            if (value < 0) {
                assembly.getObjectCode().add(PrimaryInstruction.createSubu(Register.v1.ordinal(),Register.zero.ordinal(), Register.v1.ordinal()));
            }
            CalTemplate.loadConstToRegTemplate(Register.a0.ordinal(),value,assembly.getObjectCode());
            assembly.getObjectCode().add(PrimaryInstruction.createMult(Register.a0.ordinal(),Register.v1.ordinal()));
            assembly.getObjectCode().add(PrimaryInstruction.createMflo(Register.v1.ordinal()));
            assembly.getObjectCode().add(PrimaryInstruction.createSubu(dstRegister,srcRegister,Register.v1.ordinal()));
        }
    }
}
