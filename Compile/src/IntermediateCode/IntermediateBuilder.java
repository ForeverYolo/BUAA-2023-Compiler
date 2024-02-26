package IntermediateCode;

import IntermediateCode.Container.FuncBlock;
import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Container.PrimaryBlock;
import IntermediateCode.Elements.PrimaryElement;
import IntermediateCode.Operands.*;
import ObjectCode.Assembly;
import ObjectCode.Instruction.PrimaryInstruction;
import ObjectCode.Template.CalTemplate;
import ObjectCode.Template.Register;
import Tools.ErrorMessage;
import Tools.GlobalSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IntermediateBuilder {
    private final ArrayList<VariableOperand> TempVariableArray;
    private final ArrayList<VariableOperand> GlobalVariableArray;
    private final ArrayList<VariableOperand> NormalVariableArray;
    private final ArrayList<VariableOperand> ReturnVariableArray;
    private final ArrayList<PrimaryElement> OptimizeWhiteList;
    private final NormalBlock GlobalBlock;
    private final HashMap<FuncOperand, FuncBlock> functionMap;
    private final HashMap<Integer,String> IDToString;
    private final HashMap<String,Integer> StringToID;
    private final HashMap<Integer, ConstValue> valueToConst;
    private final HashMap<TagOperand, Integer> tagToID;
    private final HashMap<Integer,TagOperand> IDToTag;
    private final HashMap<VariableOperand,ArrayList<VariableOperand>> RenameMap;
    private final HashMap<VariableOperand,VariableOperand> RenameToNameMap;
    private final HashMap<PrimaryElement,ArrayList<PrimaryElement>> CallPushMap;
    private PrimaryBlock NowBlock;

    public IntermediateBuilder() {
        this.TempVariableArray = new ArrayList<>();
        this.GlobalVariableArray = new ArrayList<>();
        this.NormalVariableArray = new ArrayList<>();
        this.ReturnVariableArray = new ArrayList<>();
        this.GlobalBlock = new NormalBlock(0,null);
        this.functionMap = new HashMap<>();
        this.IDToString = new HashMap<>();
        this.StringToID = new HashMap<>();
        this.valueToConst = new HashMap<>();
        this.tagToID= new HashMap<>();
        this.IDToTag = new HashMap<>();
        this.RenameMap = new HashMap<>();
        this.NowBlock = GlobalBlock;
        this.CallPushMap = new HashMap<>();
        this.RenameToNameMap = new HashMap<>();
        this.OptimizeWhiteList = new ArrayList<>();
    }

    public ArrayList<PrimaryElement> getOptimizeWhiteList() {
        return OptimizeWhiteList;
    }

    public ArrayList<VariableOperand> getGlobalVariableArray() {
        return GlobalVariableArray;
    }

    public ArrayList<VariableOperand> getNormalVariableArray() {
        return NormalVariableArray;
    }

    public int putStringConstAndReturnID(String str) {
        if (StringToID.containsKey(str)) {
            return StringToID.get(str);
        }
        int Now_size = StringToID.size();
        StringToID.put(str,Now_size);
        IDToString.put(Now_size,str);
        return Now_size;
    }

    public PrimaryOperand putIntConstAndReturnVariable(int value) {
        if (valueToConst.containsKey(value)) {
            return valueToConst.get(value);
        }
        ConstValue constValue = new ConstValue(value);
        valueToConst.put(value,constValue);
        return constValue;
    }

    public void AddCallPushMap(ArrayList<PrimaryElement> PushElements,PrimaryElement CallElement) {
        this.CallPushMap.put(CallElement,PushElements);
    }

    public HashMap<PrimaryElement, ArrayList<PrimaryElement>> getCallPushMap() {
        return CallPushMap;
    }

    public NormalBlock getGlobalBlock() {
        return GlobalBlock;
    }

    public HashMap<VariableOperand, ArrayList<VariableOperand>> getRenameMap() {
        return RenameMap;
    }

    public VariableOperand putGlobalVariableAndReturn() {
        VariableOperand variableOperand = VariableOperand.CreateGlobalVariable(true,GlobalVariableArray.size());
        GlobalVariableArray.add(variableOperand);
        return variableOperand;
    }

    public VariableOperand putNormalVariableAndReturn() {
        VariableOperand variableOperand = VariableOperand.CreateNormalVariable(true,NormalVariableArray.size());
        NormalVariableArray.add(variableOperand);
        return variableOperand;
    }

    public VariableOperand putTempVariableAndReturn() {
        VariableOperand variableOperand = VariableOperand.CreateTempVariable(true,TempVariableArray.size());
        TempVariableArray.add(variableOperand);
        return variableOperand;
    }

    public VariableOperand giveVarReturnRenameVar(VariableOperand var) {
        if (!RenameMap.containsKey(var)) {
            RenameMap.put(var,new ArrayList<>());
            if (var.getVariableType() == VariableType.PARAM) {
                VariableOperand variableOperand = VariableOperand.CreateRenameVar(var,RenameMap.get(var).size(),false);
                RenameMap.get(var).add(variableOperand);
                RenameToNameMap.put(variableOperand,var);
                return variableOperand;
            }
        }
        if (var.getVariableType() == VariableType.PARAM) {
            VariableOperand variableOperand = VariableOperand.CreateRenameVar(var,RenameMap.get(var).size(),true);
            RenameMap.get(var).add(variableOperand);
            RenameToNameMap.put(variableOperand,var);
            return variableOperand;
        } else {
            VariableOperand variableOperand = VariableOperand.CreateRenameVar(var,RenameMap.get(var).size(),false);
            RenameMap.get(var).add(variableOperand);
            RenameToNameMap.put(variableOperand,var);
            return variableOperand;
        }

    }

    public HashMap<VariableOperand, VariableOperand> getRenameToNameMap() {
        return RenameToNameMap;
    }

    public VariableOperand putParamVariableAndReturn(int id) {
        return VariableOperand.CreateParamVariable(false,id);
    }

    public VariableOperand putReturnVariableAndReturn(boolean isConst) {
        VariableOperand variableOperand =  VariableOperand.CreateReturnVariable(isConst,ReturnVariableArray.size());
        ReturnVariableArray.add(variableOperand);
        return variableOperand;
    }

    public FuncOperand putFuncOperandAndReturn(String name,ArrayList<PrimaryOperand> FormalParams,PrimaryOperand ReturnVariable,boolean isMain) {
        FuncOperand funcOperand = new FuncOperand(name,FormalParams,ReturnVariable,isMain);
        NowBlock = new FuncBlock(funcOperand);
        functionMap.put(funcOperand,(FuncBlock)NowBlock);
        return funcOperand;
    }

    public FuncOperand getFuncOperandToCall(String name) {
        for(Map.Entry<FuncOperand,FuncBlock> operandFuncBlockEntry : functionMap.entrySet()) {
            if (operandFuncBlockEntry.getKey().getOperandName().equals(name)) {
                return operandFuncBlockEntry.getKey();
            }
        }
        ErrorMessage.handleError(null,-1,null);
        return null;
    }

    public TagOperand putTagOperandAndReturn(String tagName) {
        int Now_size = tagToID.size();
        TagOperand tagOperand = new TagOperand(tagName + "_" + Now_size);
        tagToID.put(tagOperand,Now_size);
        IDToTag.put(Now_size,tagOperand);
        return tagOperand;
    }

    public void AddIntermediateExpression(PrimaryElement primaryElement) {
        NowBlock.AddIntermediateExpression(primaryElement);
    }

    public void DivideFuncBlock() {
        if (!GlobalSetting.RunIntermediateCode) {
            return;
        }
        for (Map.Entry<FuncOperand, FuncBlock> entry:functionMap.entrySet()) {
            entry.getValue().finalizeFunction();
        }
    }

    public HashMap<FuncOperand, FuncBlock> getFunctionMap() {
        return functionMap;
    }

    public void toAssembly(Assembly assembly) {
        if (!GlobalSetting.RunObjectCode) {
            return;
        }
        //处理全局
        int space = GlobalBlock.CalculateSpace(0);

        //将全局重命名地址映射到原始命名
        GlobalVariableArray.forEach(GlobalVar -> {
            if (RenameMap.containsKey(GlobalVar)) {
                ArrayList<VariableOperand> Vars = RenameMap.get(GlobalVar);
                Vars.forEach(RenameVar -> RenameVar.setOffset(GlobalVar.getOffset()));
            }
        });

        assembly.getObjectCode().add(PrimaryInstruction.createSegment(".data",0));
        if (space != 0) {
            assembly.getObjectCode().add(PrimaryInstruction.createTag("global"));
            assembly.getObjectCode().add(PrimaryInstruction.createSpace(space << 2));
        }
        assembly.ProcessStringConst(StringToID);
        assembly.getObjectCode().add(PrimaryInstruction.createSegment(".text",0));
        if (space > 0) {
            assembly.getObjectCode().add(PrimaryInstruction.createLa("global",Register.gp.ordinal()));
        }
        if (!GlobalBlock.IntermediateExpression.isEmpty()) {
            assembly.getObjectCode().add(PrimaryInstruction.createComment("InitGlobal",1));
        }
        assembly.setProcessingGlobal(true);
        GlobalBlock.ToAssembly(assembly);
        assembly.setProcessingGlobal(false);
        functionMap.forEach((funcOperand, funcBlock) -> funcBlock.CalculateSpace());
        // 先处理 Main 函数
        for (Map.Entry<FuncOperand,FuncBlock> entry : functionMap.entrySet()) {
            if (entry.getKey().isMain()) {
                assembly.getObjectCode().add(PrimaryInstruction.createComment(entry.getKey().getOperandName(),1));
                assembly.getObjectCode().add(PrimaryInstruction.createTag(entry.getKey().getOperandName()));
                assembly.setCurrentFunction(entry.getKey());
                CalTemplate.addImmTemplate(Register.sp.ordinal(),Register.sp.ordinal(),(-entry.getKey().getSpace()) << 2,assembly.getObjectCode());
                entry.getValue().ToAssembly(assembly);
            }
        }

        // 再处理其他函数
        for (Map.Entry<FuncOperand,FuncBlock> entry : functionMap.entrySet()) {
            if (entry.getKey().isMain()) {
                continue;
            }
            assembly.getObjectCode().add(PrimaryInstruction.createComment(entry.getKey().getOperandName(),1));
            assembly.getObjectCode().add(PrimaryInstruction.createTag(entry.getKey().getOperandName()));
            if (GlobalSetting.RegisterOptimize) {
                assembly.InitFuncParam(entry.getKey());
            }
            assembly.setCurrentFunction(entry.getKey());
            entry.getValue().ToAssembly(assembly);
        }

        assembly.PrepareForDump();
    }
}
