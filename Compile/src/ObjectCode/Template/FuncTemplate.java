package ObjectCode.Template;

import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.FuncOperand;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import ObjectCode.Assembly;
import ObjectCode.Instruction.PrimaryInstruction;
import Tools.GlobalSetting;

import java.util.ArrayList;
import java.util.HashSet;

public class FuncTemplate {
    public static int RealRegister = Register.v1.ordinal();
    public static int TempRegister = Register.v1.ordinal();

    public static void FuncPushTemplate(PrimaryOperand param,ArrayList<PrimaryOperand> RealParams,Assembly assembly) {
        RealParams.add(param);
    }

    public static void funcCallTemplate(FuncOperand funcOperand, ArrayList<PrimaryInstruction> objectCode,
                                        Assembly assembly, ArrayList<PrimaryOperand> RealParams, NormalBlock normalBlock,
                                        int normalLineIndex) {
        if (funcOperand.isMain()) {
            if (funcOperand.getSpace() != 0) {
                CalTemplate.addImmTemplate(Register.sp.ordinal(),Register.sp.ordinal(), funcOperand.getSpace(),objectCode);
            }
            objectCode.add(PrimaryInstruction.createJump(funcOperand.getOperandName()));
        } else {
            int RealSpace = funcOperand.getSpace() << 2;
            MemTemplate.storeRegisterTemplate(Register.ra.ordinal(),-4,Register.sp.ordinal(),objectCode);
            for (int i = 0; i < RealParams.size(); i++) {
                if (RealParams.get(i) instanceof ConstValue) {
                    RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,((ConstValue) RealParams.get(i)).getValue(),objectCode);
                    MemTemplate.storeRegisterTemplate(RealRegister,(i*4)-RealSpace,Register.sp.ordinal(),objectCode);
                } else {
                    int dstRegister = ((VariableOperand) RealParams.get(i)).loadValueFromMemory(assembly);
                    MemTemplate.storeRegisterTemplate(dstRegister,(i*4)-RealSpace,Register.sp.ordinal(),objectCode);
                }
            }
            RealParams.clear();
            //-----------------------------------保存现场---------------------------------------------------
            HashSet<VariableOperand> MayNeedSave = new HashSet<>();
            if (GlobalSetting.RegisterOptimize) {
                assembly.WriteBackAllLocalRegister();
                MayNeedSave = normalBlock.getFuncCallActiveMap().get(normalLineIndex);
                assembly.WriteBackAllGlobalRegister(MayNeedSave);
            } else {
                assembly.WriteBackAllLocalRegister();
            }
            //-----------------------------------保存现场---------------------------------------------------
            CalTemplate.addImmTemplate(Register.sp.ordinal(),Register.sp.ordinal(),-RealSpace,objectCode);
            objectCode.add(PrimaryInstruction.createJal(funcOperand.getOperandName()));
            CalTemplate.addImmTemplate(Register.sp.ordinal(),Register.sp.ordinal(),RealSpace,objectCode);
            //-----------------------------------恢复现场---------------------------------------------------
            if (GlobalSetting.RegisterOptimize) {
                assembly.RestoreGlobalRegister(MayNeedSave);
            }
            //-----------------------------------恢复现场---------------------------------------------------
            MemTemplate.loadRegisterTemplate(Register.ra.ordinal(),-4,Register.sp.ordinal(),objectCode);
        }
    }

    public static void funcReturnTemplate(FuncOperand funcOperand, PrimaryOperand Inner, ArrayList<PrimaryInstruction> objectCode, Assembly assembly) {
        if (funcOperand.isMain()) {
            CalTemplate.loadConstToRegTemplate(Register.v0.ordinal(),10,objectCode);
            objectCode.add(PrimaryInstruction.createSyscall());
        } else {
            if (Inner != null) {
                if (Inner instanceof ConstValue) {
                    CalTemplate.loadConstToRegTemplate(Register.v0.ordinal(),((ConstValue) Inner).getValue(),objectCode);
                } else {
                    ((VariableOperand)Inner).CopyValueToSelectRegister(Register.v0.ordinal(),assembly);
                }
            }
            objectCode.add(PrimaryInstruction.createJr(Register.ra.ordinal()));
        }
    }

    public static void funcScanTemplate(PrimaryOperand dst,ArrayList<PrimaryInstruction> objectCode,Assembly assembly) {
        int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
        CalTemplate.loadConstToRegTemplate(Register.v0.ordinal(), 5,objectCode);
        objectCode.add(PrimaryInstruction.createSyscall());
        CalTemplate.MoveTemplate(dstRegister,Register.v0.ordinal(),objectCode);
    }

    public static void funcPrintStrTemplate(int address,ArrayList<PrimaryInstruction> objectCode) {
        objectCode.add(PrimaryInstruction.createLa("str_" + address,Register.a0.ordinal()));
        CalTemplate.loadConstToRegTemplate(Register.v0.ordinal(),4,objectCode);
        objectCode.add(PrimaryInstruction.createSyscall());
    }


    public static void funcPrintNumTemplate(PrimaryOperand primaryOperand,ArrayList<PrimaryInstruction> objectCode,Assembly assembly) {
        if (primaryOperand instanceof ConstValue) {
            CalTemplate.loadConstToRegTemplate(Register.a0.ordinal(),((ConstValue) primaryOperand).getValue(),objectCode);
        } else {
            ((VariableOperand)primaryOperand).CopyValueToSelectRegister(Register.a0.ordinal(),assembly);
        }
        CalTemplate.loadConstToRegTemplate(Register.v0.ordinal(), 1,objectCode);
        objectCode.add(PrimaryInstruction.createSyscall());
    }
}
