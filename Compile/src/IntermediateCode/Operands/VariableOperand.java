package IntermediateCode.Operands;

import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Container.PrimaryBlock;
import ObjectCode.Assembly;
import ObjectCode.Template.CalTemplate;
import ObjectCode.Template.MemTemplate;
import ObjectCode.Template.Register;
import Tools.ErrorMessage;

public abstract class VariableOperand extends PrimaryOperand {
    private boolean isConst;
    private VariableType variableType;
    private PrimaryBlock normalBlock;
    private int normalLineIndex;
    private int offset;
    private int AllocatedRegister;
    private int RegisterRecord;
    private boolean MemVisit;

    public VariableOperand(boolean isConst, String name, VariableType variableType) {
        super(name);
        this.isConst = isConst;
        this.variableType = variableType;
        this.offset = -1;
        this.AllocatedRegister = -1;
        this.normalBlock = null;
        this.normalLineIndex = -1;
        this.RegisterRecord = -1;
        this.MemVisit = false;
    }

    public VariableOperand(boolean isConst, String name,VariableType variableType,int offset) {
        super(name);
        this.isConst = isConst;
        this.variableType = variableType;
        this.offset = offset;
        this.AllocatedRegister = -1;
        this.normalBlock = null;
        this.normalLineIndex = -1;
        this.RegisterRecord = -1;
        this.MemVisit = false;
    }

    public VariableOperand(VariableOperand var,int id) {
        super(var.OperandName + "_" + id);
        this.isConst = var.isConst;
        this.variableType = var.variableType;
        this.offset = var.offset;
        this.AllocatedRegister = var.AllocatedRegister;
        this.normalBlock = var.normalBlock;
        this.normalLineIndex = var.normalLineIndex;
        this.RegisterRecord = var.RegisterRecord;
        this.MemVisit = var.MemVisit;
    }

    public boolean isMemVisit() {
        return MemVisit;
    }

    public void setMemVisit(boolean memVisit) {
        MemVisit = memVisit;
    }

    public void setAllocatedGlobalRegister(int allocatedRegister) {
        AllocatedRegister = allocatedRegister;
        RegisterRecord = allocatedRegister;
    }

    public PrimaryBlock getNormalBlockId() {
        return normalBlock;
    }

    public void setNormalBlockId(PrimaryBlock normalBlockId) {
        this.normalBlock = normalBlockId;
    }

    public void setPlaceMessage(PrimaryBlock block,int Index) {
        this.normalBlock = block;
        this.normalLineIndex = Index;
    }

    public int getNormalLineIndex() {
        return normalLineIndex;
    }

    public void setNormalLineIndex(int normalLineIndex) {
        this.normalLineIndex = normalLineIndex;
    }

    public VariableType getVariableType() {
        return variableType;
    }

    public int getOffset() {
        return offset;
    }

    public int getSpace() {
        return offset < 0 ? 1:0;
    }


    public void setOffset(int offset) {
        if (this.offset >= 0) {
            return;
        }
        this.offset = offset;
    }

    public static VariableOperand CreateGlobalVariable(boolean isConst, int id) {
        return new VariableOperand(isConst, TypeToString.get(VariableType.GLOBAL) + id, VariableType.GLOBAL) {

        };
    }

    public static VariableOperand CreateNormalVariable(boolean isConst, int id) {
        return new VariableOperand(isConst, TypeToString.get(VariableType.VARIABLE) + id, VariableType.VARIABLE) {

        };
    }

    public static VariableOperand CreateParamVariable(boolean isConst, int id) {
        return new VariableOperand(isConst, TypeToString.get(VariableType.PARAM) + id, VariableType.PARAM,id) {

        };
    }

    public static VariableOperand CreateReturnVariable(boolean isConst, int id) {
        return new VariableOperand(isConst, TypeToString.get(VariableType.RETURN) + id, VariableType.RETURN) {

        };
    }

    public static VariableOperand CreateTempVariable(boolean isConst, int id) {
        return new VariableOperand(isConst, TypeToString.get(VariableType.TEMP) + id, VariableType.TEMP) {

        };
    }

    public static VariableOperand CreateRenameVar(VariableOperand var,int id,boolean GiveNewLife) {
        if (GiveNewLife) {
            return new VariableOperand(var.isConst, var.OperandName + "_" + id, VariableType.TEMP) {
            };
        } else {
            return new VariableOperand(var, id) {
            };
        }
    }

    public int allocateRegister(Assembly assembly) {
        if (RegisterRecord != -1) {
            AllocatedRegister = RegisterRecord;
            return AllocatedRegister;
        }
        AllocatedRegister = assembly.AllocateLocalRegister(this, (NormalBlock) normalBlock, normalLineIndex);
        return AllocatedRegister;
    }

    public int loadValueFromMemory(Assembly assembly) {
        if (RegisterRecord != -1) {
            AllocatedRegister = RegisterRecord;
            return AllocatedRegister;
        }
        if ((assembly.QueryAllocatedRegister(this) == AllocatedRegister) && AllocatedRegister != -1) {
            return AllocatedRegister;
        }
        AllocatedRegister = -1;
        if (variableType == VariableType.GLOBAL || assembly.isProcessingGlobal()) {
            AllocatedRegister = assembly.AllocateLocalRegister(this, (NormalBlock) normalBlock, normalLineIndex);
            MemTemplate.loadRegisterTemplate(AllocatedRegister, offset << 2, Register.gp.ordinal(),assembly.getObjectCode());
        } else if (variableType == VariableType.VARIABLE || variableType == VariableType.PARAM || variableType == VariableType.TEMP) {
            AllocatedRegister = assembly.AllocateLocalRegister(this, (NormalBlock) normalBlock, normalLineIndex);
            MemTemplate.loadRegisterTemplate(AllocatedRegister, offset << 2, Register.sp.ordinal(),assembly.getObjectCode());
        } else if (variableType == VariableType.RETURN) {
            AllocatedRegister = Register.v0.ordinal();
        } else {
            ErrorMessage.handleError(null,-1,null);
        }
        return AllocatedRegister;
    }

    public void CopyValueToSelectRegister(int dst, Assembly assembly) {
        if (RegisterRecord != -1) {
            CalTemplate.MoveTemplate(dst,RegisterRecord,assembly.getObjectCode());
            return;
        }
        if ((assembly.QueryAllocatedRegister(this) == AllocatedRegister) && AllocatedRegister != -1) {
            CalTemplate.MoveTemplate(dst,AllocatedRegister,assembly.getObjectCode());
            return;
        }
        if (variableType == VariableType.GLOBAL || assembly.isProcessingGlobal()) {
            MemTemplate.loadRegisterTemplate(dst,offset << 2,Register.gp.ordinal(),assembly.getObjectCode());
        } else if (variableType == VariableType.VARIABLE || variableType == VariableType.PARAM || variableType == VariableType.TEMP) {
            MemTemplate.loadRegisterTemplate(dst,offset << 2,Register.sp.ordinal(),assembly.getObjectCode());
        } else if (variableType == VariableType.RETURN) {
            CalTemplate.MoveTemplate(dst,Register.v0.ordinal(),assembly.getObjectCode());
        }
    }

    public void RestoreValueFromMemory(Assembly assembly) {
        if (variableType == VariableType.GLOBAL || assembly.isProcessingGlobal()) {
            MemTemplate.loadRegisterTemplate(RegisterRecord,offset << 2,Register.gp.ordinal(),assembly.getObjectCode());
        } else if (variableType == VariableType.VARIABLE || variableType == VariableType.PARAM || variableType == VariableType.TEMP) {
            MemTemplate.loadRegisterTemplate(RegisterRecord,offset << 2,Register.sp.ordinal(),assembly.getObjectCode());
        } else if (variableType == VariableType.RETURN) {
            CalTemplate.MoveTemplate(RegisterRecord,Register.v0.ordinal(),assembly.getObjectCode());
        }
    }

    public int getRegisterRecord() {
        return RegisterRecord;
    }

    public void writeBackFormLocalRegister(Assembly assembly) {
        if (variableType == VariableType.GLOBAL || assembly.isProcessingGlobal()) {
            MemTemplate.storeRegisterTemplate(AllocatedRegister,offset << 2,Register.gp.ordinal(),assembly.getObjectCode());
        } else {
            MemTemplate.storeRegisterTemplate(AllocatedRegister,offset << 2,Register.sp.ordinal(),assembly.getObjectCode());
        }
    }

    public void setNeedWriteBack(Assembly assembly) {
        if (RegisterRecord < 0) {
            assembly.setNeedWriteBack(this);
        }
    }

    public void writeBackFromGlobalRegister(Assembly assembly) {
        if (variableType == VariableType.GLOBAL || assembly.isProcessingGlobal()) {
            MemTemplate.storeRegisterTemplate(RegisterRecord,offset << 2,Register.gp.ordinal(),assembly.getObjectCode());
        } else {
            MemTemplate.storeRegisterTemplate(RegisterRecord
                    ,offset << 2,Register.sp.ordinal(),assembly.getObjectCode());
        }
    }
}
