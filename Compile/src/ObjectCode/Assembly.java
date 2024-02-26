package ObjectCode;

import IntermediateCode.Container.FuncBlock;
import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Elements.PrimaryElement;
import IntermediateCode.Operands.FuncOperand;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import IntermediateCode.Operands.VariableType;
import ObjectCode.Instruction.PrimaryInstruction;
import ObjectCode.Template.Register;
import Tools.Combination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Assembly {
    protected ArrayList<PrimaryInstruction> objectCode;
    protected ArrayList<String> initString;
    protected FuncOperand currentFunction;
    protected ArrayList<PrimaryOperand> RealFuncParams;
    protected boolean ProcessingGlobal;
    protected static HashMap<Integer,VariableOperand> localRegisterPool = new HashMap<>() {
        {
            put(5,null);
            put(6,null);
            put(7,null);
            put(8,null);
            put(9,null);
            put(10,null);
            put(11,null);
            put(12,null);
            put(13,null);
            put(14,null);
            put(15,null);
            put(24,null);
            put(25,null);
        }
    };

    protected static HashMap<Integer,VariableOperand> globalRegisterPool = new HashMap<>() {
        {
            put(16, null);
            put(17, null);
            put(18, null);
            put(19, null);
            put(20, null);
            put(21, null);
            put(22, null);
            put(23, null);
            put(26, null);
            put(27, null);
            put(30, null);
        }
    };

    protected static HashSet<VariableOperand> needWriteBack = new HashSet<>();

    public HashMap<Integer, VariableOperand> getGlobalRegisterPool() {
        return globalRegisterPool;
    }

    public Assembly() {
        this.objectCode = new ArrayList<>();
        this.initString = new ArrayList<>();
        this.RealFuncParams = new ArrayList<>();
        this.ProcessingGlobal = false;
    }

    public void setProcessingGlobal(boolean processingGlobal) {
        ProcessingGlobal = processingGlobal;
    }

    public boolean isProcessingGlobal() {
        return ProcessingGlobal;
    }

    public ArrayList<PrimaryOperand> getRealFuncParams() {
        return RealFuncParams;
    }

    public void setCurrentFunction(FuncOperand currentFunction) {
        this.currentFunction = currentFunction;
    }

    public FuncOperand getCurrentFunction() {
        return currentFunction;
    }

    public void WriteBackAllLocalRegister() {
        localRegisterPool.forEach((integer, variableOperand) -> {
            if(variableOperand != null) {
                if (needWriteBack.contains(variableOperand)) {
                    objectCode.add(PrimaryInstruction.createComment("WriteBackAllLocalRegister " + variableOperand,2));
                    WriteLocalBackRegister(integer);
                    needWriteBack.remove(variableOperand);
                }
            }
            localRegisterPool.put(integer,null);
        });
    }

    public void JustWriteBackGlobalInLocalRegister() {
        localRegisterPool.forEach((integer, variableOperand) -> {
            if(variableOperand != null) {
                if (needWriteBack.contains(variableOperand) && variableOperand.getVariableType().equals(VariableType.GLOBAL)) {
                    objectCode.add(PrimaryInstruction.createComment("WriteBackAllLocalRegister " + variableOperand,2));
                    WriteLocalBackRegister(integer);
                    needWriteBack.remove(variableOperand);
                }
            }
        });
        needWriteBack.clear();
    }


    public void setNeedWriteBack(VariableOperand var) {
        for (Map.Entry<Integer,VariableOperand> entry : localRegisterPool.entrySet()) {
            if (entry.getValue() == var) {
                needWriteBack.add(var);
                return;
            }
        }
    }

    public void FlushAllLocalRegister() {
        localRegisterPool.forEach((integer, variableOperand) -> {
            localRegisterPool.put(integer,null);
            needWriteBack.clear();
        });
    }


    public void WriteBackVariable(VariableOperand variableOperand) {
        int Register = QueryAllocatedRegister(variableOperand);
        if (Register != -1) {
            objectCode.add(PrimaryInstruction.createComment("WriteBackVariable " + variableOperand,3));
            WriteLocalBackRegister(Register);
            localRegisterPool.put(Register,null);
            needWriteBack.remove(variableOperand);
        }
    }

    public void ProcessStringConst(HashMap<String,Integer> stringToID) {
        stringToID.forEach((s, integer) -> {
            String s1 = "str_" + integer + ": " + ".asciiz " + "\"" + s + "\"";
            initString.add(s1);
            objectCode.add(PrimaryInstruction.createInitStr(s1));
        });
    }

    public void WriteBackAllGlobalRegister(HashSet<VariableOperand> MayNeedWriteBack) {
        MayNeedWriteBack.forEach(var -> {
            if (var.getRegisterRecord() != -1) {
                objectCode.add(PrimaryInstruction.createComment("WriteBackGlobalRegister " + var,2));
                var.writeBackFromGlobalRegister(this);
            }
        });
    }

    public void RestoreGlobalRegister(HashSet<VariableOperand> MayNeedRestore) {
        for (VariableOperand var : MayNeedRestore) {
            if (var.getRegisterRecord() != -1) {
                objectCode.add(PrimaryInstruction.createComment("RestoreGlobalRegister " + var, 2));
                var.RestoreValueFromMemory(this);
            }
        }
    }

    public void PrepareForDump() {
        int deep = 0;
        for (int i = 0; i < objectCode.size(); i++) {
            PrimaryInstruction instruction = objectCode.get(i);
            if (instruction.getName().equals("#") || instruction.getName().equals("Segment")) {
                deep = instruction.getDeep();
            } else {
                instruction.setDeep(deep + 1);
            }
        }
    }

    public int QueryAllocatedRegister(VariableOperand variableOperand) {
        for(Map.Entry<Integer,VariableOperand> entry: localRegisterPool.entrySet()) {
            if (entry.getValue() == variableOperand) {
                return entry.getKey();
            }
        }
        return -1;
    }

    // Warning : 这个函数在没有任何例如控制流分析的帮助下，是无用的
    public int AllocateLocalRegister(VariableOperand variableOperand,NormalBlock normalBlock,int elementIndex) {
        if (QueryAllocatedRegister(variableOperand) != -1) {
            return QueryAllocatedRegister(variableOperand);
        } else {
            HashMap<Integer,VariableOperand> queryMap = localRegisterPool;
            for (Map.Entry<Integer,VariableOperand> entry : queryMap.entrySet()) {
                if (entry.getValue() == null) {
                    queryMap.put(entry.getKey(),variableOperand);
                    return entry.getKey();
                }
            }
            //说明满了，选一个替换
            Combination<Boolean,Integer> BeReplacedRegister = OptSchedule(queryMap,normalBlock,elementIndex);
            //在这个基本块后面是否还要用到，不用到不用写回。但全局是要写回的。
            if (BeReplacedRegister.getKey()) {
                if (needWriteBack.contains(queryMap.get(BeReplacedRegister.getValue()))) {
                    objectCode.add(PrimaryInstruction.createComment("WriteBack $" + BeReplacedRegister.getValue(),2));
                    WriteLocalBackRegister(BeReplacedRegister.getValue());
                }
            }
            queryMap.put(BeReplacedRegister.getValue(),variableOperand);
            return BeReplacedRegister.getValue();
        }
    }

    public Combination<Boolean,Integer> OptSchedule(HashMap<Integer,VariableOperand> queryMap, NormalBlock normalBlock, int elementIndex) {
        HashMap<VariableOperand,Integer> WhenUseCount = new HashMap<>();
        for (int i = elementIndex; i < normalBlock.IntermediateExpression.size(); i++) {
            int gap = i - elementIndex;
            PrimaryElement NextElement = normalBlock.IntermediateExpression.get(i);
            HashSet<VariableOperand> ElementVar = new HashSet<>();
            ElementVar.addAll(NextElement.getDefineVariable());
            ElementVar.addAll(NextElement.getUsedVariable());
            for (VariableOperand var : ElementVar) {
                if (queryMap.containsValue(var) && !WhenUseCount.containsKey(var)) {
                    if (i == elementIndex) {
                        WhenUseCount.put(var,-1);
                    } else {
                        WhenUseCount.put(var,gap);
                    }
                }
            }
        }
        int MaxGap = -1;
        int SelectRegister = -1;
        VariableOperand SelectVar = null;
        for (Map.Entry<Integer,VariableOperand> entry : queryMap.entrySet()) {
            VariableOperand var = entry.getValue();
            int Register = entry.getKey();
            if (WhenUseCount.containsKey(var) && WhenUseCount.get(var) > MaxGap) {
                //为空随便选
                if (SelectVar == null) {
                    MaxGap = WhenUseCount.get(var);
                    SelectRegister = Register;
                    SelectVar = var;
                }
                //已选择的为Global,新来的也是Global,选一个最远的(局部)
                else if (SelectVar.getVariableType().equals(VariableType.GLOBAL) && var.getVariableType().equals(VariableType.GLOBAL)) {
                    MaxGap = WhenUseCount.get(var);
                    SelectRegister = Register;
                    SelectVar = var;
                }
                //已选择的为Global,新来的是局部，更换局部。
                else if (SelectVar.getVariableType().equals(VariableType.GLOBAL) && !var.getVariableType().equals(VariableType.GLOBAL)) {
                    MaxGap = WhenUseCount.get(var);
                    SelectRegister = Register;
                    SelectVar = var;
                }
                //已选择的为局部，新来的为局部，选一个最远的。
                else if (!SelectVar.getVariableType().equals(VariableType.GLOBAL) && !var.getVariableType().equals(VariableType.GLOBAL)) {
                    MaxGap = WhenUseCount.get(var);
                    SelectRegister = Register;
                    SelectVar = var;
                }
            }
            //虽然比目前的全局变量近，但我们仍然倾向于替换局部变量，因为它不需要最终写回。
            else if (WhenUseCount.containsKey(var) && WhenUseCount.get(var) <= MaxGap) {
                if (SelectVar != null && SelectVar.getVariableType().equals(VariableType.GLOBAL) && !var.getVariableType().equals(VariableType.GLOBAL)) {
                    MaxGap = WhenUseCount.get(var);
                    SelectRegister = Register;
                    SelectVar = var;
                }
            }
            //对于局部，不需要写回，对于全局，迟早写回。
            if (!WhenUseCount.containsKey(var)) {
                MaxGap = Integer.MAX_VALUE;
                SelectRegister = Register;
                SelectVar = var;
                break;
            }
        }
        if (MaxGap == Integer.MAX_VALUE && SelectVar != null && !SelectVar.getVariableType().equals(VariableType.GLOBAL)) {
            return new Combination<>(false,SelectRegister);
        } else {
            return new Combination<>(true,SelectRegister);
        }
    }

    public void WriteLocalBackRegister(int dst) {
        VariableOperand variableOperand = localRegisterPool.get(dst);
        variableOperand.writeBackFormLocalRegister(this);
    }


    public ArrayList<PrimaryInstruction> getObjectCode() {
        return objectCode;
    }

    public void InitFuncParam(FuncOperand func) {
        boolean PrintTrick = true;
        for (int i = 0; i < func.getFormalParams().size(); i++) {
            PrimaryOperand operand = func.getFormalParams().get(i);
            if (operand instanceof VariableOperand Param) {
                int register = Param.getRegisterRecord();
                if (register > 0) {
                    if (PrintTrick) {
                        PrintTrick = false;
                        objectCode.add(PrimaryInstruction.createComment("InitParam " + operand,2));
                    }
                    objectCode.add(PrimaryInstruction.createLw(register
                            , i << 2, Register.sp.ordinal()));
                }
            }
        }
    }

}
