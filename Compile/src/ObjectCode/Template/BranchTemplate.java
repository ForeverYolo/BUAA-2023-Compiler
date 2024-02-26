package ObjectCode.Template;

import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import ObjectCode.Assembly;
import ObjectCode.Instruction.PrimaryInstruction;
import Tools.GlobalSetting;

import java.util.ArrayList;

public class BranchTemplate {
    public static int TempRegister = Register.v1.ordinal();
    public static int RealRegister = Register.v1.ordinal();

    private static void shorterSeqTemplate(int srcRegister,int value,ArrayList<PrimaryInstruction> objectCode) {
        if (-Short.MAX_VALUE <= value && value <= -Short.MIN_VALUE) {
            objectCode.add(PrimaryInstruction.createAddiu(TempRegister,srcRegister,-value));
            RealRegister = TempRegister;
        } else if (0 <= value && value <= Short.MAX_VALUE - Short.MIN_VALUE) {
            objectCode.add(PrimaryInstruction.createXori(TempRegister,srcRegister,value));
            RealRegister = TempRegister;
        } else {
            RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createXor(RealRegister,srcRegister,RealRegister));
        }
    }

    public static void SeqTemplate(PrimaryOperand dst, PrimaryOperand src1, PrimaryOperand src2, Assembly assembly, ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
           int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
           CalTemplate.loadConstToRegTemplate(dstRegister,((ConstValue) src1).getValue() == ((ConstValue) src2).getValue() ? 1 : 0,objectCode);
        } else if (src1 instanceof ConstValue) {
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue) src1).getValue();
            if (GlobalSetting.CompareInstSelectOptimize) {
                BranchTemplate.shorterSeqTemplate(src2Register,value,objectCode);
                objectCode.add(PrimaryInstruction.createSltiu(dstRegister,RealRegister,1));
            } else {
                RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
                objectCode.add(PrimaryInstruction.createSeq(dstRegister,RealRegister,src2Register));
            }
        } else if (src2 instanceof ConstValue) {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue)src2).getValue();
            if (GlobalSetting.CompareInstSelectOptimize) {
                BranchTemplate.shorterSeqTemplate(src1Register,value,objectCode);
                objectCode.add(PrimaryInstruction.createSltiu(dstRegister,RealRegister,1));
            } else {
                RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
                objectCode.add(PrimaryInstruction.createSeq(dstRegister,src1Register,RealRegister));
            }
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            if (GlobalSetting.CompareInstSelectOptimize) {
                objectCode.add(PrimaryInstruction.createXor(TempRegister,src1Register,src2Register));
                objectCode.add(PrimaryInstruction.createSltiu(dstRegister,TempRegister,1));
            } else {
                objectCode.add(PrimaryInstruction.createSeq(dstRegister,src1Register,src2Register));
            }
        }
    }


    public static void SneTemplate(PrimaryOperand dst, PrimaryOperand src1, PrimaryOperand src2, Assembly assembly, ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            CalTemplate.loadConstToRegTemplate(dstRegister,((ConstValue) src1).getValue() == ((ConstValue) src2).getValue() ? 0 : 1,objectCode);
        } else if (src1 instanceof ConstValue) {
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue) src1).getValue();
            if (GlobalSetting.CompareInstSelectOptimize) {
                BranchTemplate.shorterSeqTemplate(src2Register,value,objectCode);
                objectCode.add(PrimaryInstruction.createSltu(dstRegister,Register.zero.ordinal(), RealRegister));
            } else {
                RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
                objectCode.add(PrimaryInstruction.createSne(dstRegister,RealRegister,src2Register));
            }
        } else if (src2 instanceof ConstValue) {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue)src2).getValue();
            if (GlobalSetting.CompareInstSelectOptimize) {
                BranchTemplate.shorterSeqTemplate(src1Register,value,objectCode);
                objectCode.add(PrimaryInstruction.createSltu(dstRegister,Register.zero.ordinal(), RealRegister));
            } else {
                RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
                objectCode.add(PrimaryInstruction.createSne(dstRegister,src1Register,RealRegister));
            }
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            if (GlobalSetting.CompareInstSelectOptimize) {
                objectCode.add(PrimaryInstruction.createXor(TempRegister,src1Register,src2Register));
                objectCode.add(PrimaryInstruction.createSltu(dstRegister,Register.zero.ordinal(),TempRegister));
            } else {
                objectCode.add(PrimaryInstruction.createSne(dstRegister,src1Register,src2Register));
            }
        }
    }

    public static void SgeTemplate(PrimaryOperand dst, PrimaryOperand src1, PrimaryOperand src2, Assembly assembly, ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            CalTemplate.loadConstToRegTemplate(dstRegister,((ConstValue) src1).getValue() >= ((ConstValue) src2).getValue() ? 1 : 0,objectCode);
        } else if (src1 instanceof ConstValue) {
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue) src1).getValue();
            RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createSge(dstRegister,RealRegister,src2Register));
        } else if (src2 instanceof ConstValue) {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue)src2).getValue();
            RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createSge(dstRegister,src1Register,RealRegister));
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            objectCode.add(PrimaryInstruction.createSge(dstRegister,src1Register,src2Register));
        }
    }

    public static void SgtTemplate(PrimaryOperand dst, PrimaryOperand src1, PrimaryOperand src2, Assembly assembly, ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            CalTemplate.loadConstToRegTemplate(dstRegister,((ConstValue) src1).getValue() > ((ConstValue) src2).getValue() ? 1 : 0,objectCode);
        } else if (src1 instanceof ConstValue) {
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue) src1).getValue();
            RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createSgt(dstRegister,RealRegister,src2Register));
        } else if (src2 instanceof ConstValue) {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue)src2).getValue();
            RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createSgt(dstRegister,src1Register,RealRegister));
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            objectCode.add(PrimaryInstruction.createSgt(dstRegister,src1Register,src2Register));
        }
    }


    public static void SleTemplate(PrimaryOperand dst, PrimaryOperand src1, PrimaryOperand src2, Assembly assembly, ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            CalTemplate.loadConstToRegTemplate(dstRegister,((ConstValue) src1).getValue() <= ((ConstValue) src2).getValue() ? 1 : 0,objectCode);
        } else if (src1 instanceof ConstValue) {
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue) src1).getValue();
            if (GlobalSetting.CompareInstSelectOptimize) {
                if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
                    objectCode.add(PrimaryInstruction.createSlti(TempRegister,src2Register,value));
                } else {
                    RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
                    objectCode.add(PrimaryInstruction.createSlt(TempRegister,src2Register,RealRegister));
                }
                objectCode.add(PrimaryInstruction.createXori(dstRegister,TempRegister,1));
            } else {
                RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
                objectCode.add(PrimaryInstruction.createSle(dstRegister,RealRegister,src2Register));
            }
        } else if (src2 instanceof ConstValue) {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue)src2).getValue();
            if (GlobalSetting.CompareInstSelectOptimize) {
                RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
                objectCode.add(PrimaryInstruction.createSlt(TempRegister,RealRegister,src1Register));
                objectCode.add(PrimaryInstruction.createXori(dstRegister,TempRegister,1));
            } else {
                RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
                objectCode.add(PrimaryInstruction.createSle(dstRegister,src1Register,RealRegister));
            }
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            if (GlobalSetting.CompareInstSelectOptimize) {
                objectCode.add(PrimaryInstruction.createSlt(TempRegister,src2Register,src1Register));
                objectCode.add(PrimaryInstruction.createXori(dstRegister,TempRegister,1));
            } else {
                objectCode.add(PrimaryInstruction.createSle(dstRegister,src1Register,src2Register));
            }
        }
    }


    public static void SltTemplate(PrimaryOperand dst, PrimaryOperand src1, PrimaryOperand src2, Assembly assembly, ArrayList<PrimaryInstruction> objectCode) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            CalTemplate.loadConstToRegTemplate(dstRegister,((ConstValue) src1).getValue() < ((ConstValue) src2).getValue() ? 1 : 0,objectCode);
        } else if (src1 instanceof ConstValue) {
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue) src1).getValue();
            RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createSlt(dstRegister,RealRegister,src2Register));
        } else if (src2 instanceof ConstValue) {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            int value = ((ConstValue)src2).getValue();
            if (GlobalSetting.CompareInstSelectOptimize) {
                if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
                    objectCode.add(PrimaryInstruction.createSlti(dstRegister,src1Register,value));
                } else {
                    RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
                    objectCode.add(PrimaryInstruction.createSlt(dstRegister,src1Register,RealRegister));
                }
            } else {
                RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
                objectCode.add(PrimaryInstruction.createSlt(dstRegister,src1Register,RealRegister));
            }
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int dstRegister = ((VariableOperand)dst).allocateRegister(assembly);
            objectCode.add(PrimaryInstruction.createSlt(dstRegister,src1Register,src2Register));
        }
    }

    public static void BnzTemplate(PrimaryOperand src, String notBranchTag, String BranchTag
            , ArrayList<PrimaryInstruction> objectCode, Assembly assembly) {
        if (src instanceof ConstValue) {
            objectCode.add(PrimaryInstruction.createJump(((ConstValue) src).getValue() != 0 ? BranchTag : notBranchTag));
        } else {
            int srcRegister = ((VariableOperand)src).loadValueFromMemory(assembly);
            objectCode.add(PrimaryInstruction.createBnz(srcRegister,BranchTag));
        }
    }

    public static void BeqzTemplate(PrimaryOperand src, String notBranchTag, String BranchTag
            , ArrayList<PrimaryInstruction> objectCode, Assembly assembly) {
        if (src instanceof ConstValue) {
            objectCode.add(PrimaryInstruction.createJump(((ConstValue) src).getValue() == 0 ? BranchTag : notBranchTag));
        } else {
            int srcRegister = ((VariableOperand)src).loadValueFromMemory(assembly);
            objectCode.add(PrimaryInstruction.createBeqz(srcRegister,BranchTag));
        }
    }

    public static void BneTemplate(PrimaryOperand src1,PrimaryOperand src2, String notBranchTag, String BranchTag
            , ArrayList<PrimaryInstruction> objectCode, Assembly assembly) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            objectCode.add(PrimaryInstruction.createJump(((ConstValue) src1).getValue() != ((ConstValue) src2).getValue() ? BranchTag : notBranchTag));
        } else if (src1 instanceof ConstValue){
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int value = ((ConstValue) src1).getValue();
            RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createBne(RealRegister,src2Register,BranchTag));
        } else if (src2 instanceof ConstValue) {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int value = ((ConstValue) src2).getValue();
            RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createBne(src1Register,RealRegister,BranchTag));
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            objectCode.add(PrimaryInstruction.createBne(src1Register,src2Register,BranchTag));
        }
    }

    public static void BeqTemplate(PrimaryOperand src1,PrimaryOperand src2, String notBranchTag, String BranchTag
            , ArrayList<PrimaryInstruction> objectCode, Assembly assembly) {
        if (src1 instanceof ConstValue && src2 instanceof ConstValue) {
            objectCode.add(PrimaryInstruction.createJump(((ConstValue) src1).getValue() == ((ConstValue) src2).getValue() ? BranchTag : notBranchTag));
        } else if (src1 instanceof ConstValue){
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            int value = ((ConstValue) src1).getValue();
            RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createBeq(RealRegister,src2Register,BranchTag));
        } else if (src2 instanceof ConstValue) {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int value = ((ConstValue) src2).getValue();
            RealRegister = CalTemplate.loadConstToRegTemplate(TempRegister,value,objectCode);
            objectCode.add(PrimaryInstruction.createBeq(src1Register,RealRegister,BranchTag));
        } else {
            int src1Register = ((VariableOperand)src1).loadValueFromMemory(assembly);
            int src2Register = ((VariableOperand)src2).loadValueFromMemory(assembly);
            objectCode.add(PrimaryInstruction.createBeq(src1Register,src2Register,BranchTag));
        }
    }


    public static void BgezTemplate(PrimaryOperand src,String notBranchTag, String BranchTag
            , ArrayList<PrimaryInstruction> objectCode, Assembly assembly) {
        if (src instanceof ConstValue) {
            objectCode.add(PrimaryInstruction.createJump(((ConstValue) src).getValue() >= 0 ? BranchTag : notBranchTag));
        } else {
            int srcRegister = ((VariableOperand)src).loadValueFromMemory(assembly);
            objectCode.add(PrimaryInstruction.createBgez(srcRegister,BranchTag));
        }
    }

    public static void BgtzTemplate(PrimaryOperand src,String notBranchTag, String BranchTag
            , ArrayList<PrimaryInstruction> objectCode, Assembly assembly) {
        if (src instanceof ConstValue) {
            objectCode.add(PrimaryInstruction.createJump(((ConstValue) src).getValue() > 0 ? BranchTag : notBranchTag));
        } else {
            int srcRegister = ((VariableOperand)src).loadValueFromMemory(assembly);
            objectCode.add(PrimaryInstruction.createBgtz(srcRegister,BranchTag));
        }
    }


    public static void BlezTemplate(PrimaryOperand src,String notBranchTag, String BranchTag
            , ArrayList<PrimaryInstruction> objectCode, Assembly assembly) {
        if (src instanceof ConstValue) {
            objectCode.add(PrimaryInstruction.createJump(((ConstValue) src).getValue() <= 0 ? BranchTag : notBranchTag));
        } else {
            int srcRegister = ((VariableOperand)src).loadValueFromMemory(assembly);
            objectCode.add(PrimaryInstruction.createBlez(srcRegister,BranchTag));
        }
    }


    public static void BltzTemplate(PrimaryOperand src,String notBranchTag, String BranchTag
            , ArrayList<PrimaryInstruction> objectCode, Assembly assembly) {
        if (src instanceof ConstValue) {
            objectCode.add(PrimaryInstruction.createJump(((ConstValue) src).getValue() < 0 ? BranchTag : notBranchTag));
        } else {
            int srcRegister = ((VariableOperand)src).loadValueFromMemory(assembly);
            objectCode.add(PrimaryInstruction.createBltz(srcRegister,BranchTag));
        }
    }
}
