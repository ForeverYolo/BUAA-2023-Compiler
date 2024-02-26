package Optimize.MidOpt;

import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Elements.CalculateElement;
import IntermediateCode.Elements.OtherElements;
import IntermediateCode.Elements.PhiElement;
import IntermediateCode.Elements.PrimaryElement;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import IntermediateCode.Operands.VariableType;
import Tools.Combination;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalValueNumbering {
    private final HashMap<Integer, HashMap<PrimaryElement,PrimaryOperand>> Hash;
    protected ArrayList<PrimaryElement> optimizeWhiteList;
    protected ArrayList<NormalBlock> normalBlocks;
    protected HashMap<NormalBlock, ArrayList<NormalBlock>> prevMap;
    protected HashMap<NormalBlock, ArrayList<NormalBlock>> nextMap;
    protected SSAConvert ssaConvert;
    protected ControlFlowGraph controlFlowGraph;
    protected ArrayList<NormalBlock> VisitOrder;
    protected HashMap<Integer,PrimaryOperand> ConstValueMap;
    protected ArrayList<NormalBlock> Visited;
    protected ArrayList<NormalBlock> Order;
    protected HashMap<PrimaryOperand,NormalBlock> DefBlockMap;
    public GlobalValueNumbering(SSAConvert ssaConvert,ArrayList<PrimaryElement> optimizeWhiteList) {
        Hash = new HashMap<>();
        this.ssaConvert = ssaConvert;
        this.controlFlowGraph = ssaConvert.controlFlowGraph;
        this.prevMap = ssaConvert.controlFlowGraph.prevMap;
        this.nextMap = ssaConvert.controlFlowGraph.nextMap;
        this.normalBlocks = ssaConvert.normalBlocks;
        this.VisitOrder = new ArrayList<>();
        this.ConstValueMap = new HashMap<>();
        this.Visited = new ArrayList<>();
        this.Order = new ArrayList<>();
        this.DefBlockMap = new HashMap<>();
        this.optimizeWhiteList = optimizeWhiteList;
    }

    public void GenerateVisitOrder() {
        HashMap<NormalBlock,HashSet<NormalBlock>> Dom = ssaConvert.StrictDomination;
        NormalBlock EntryBlock = ssaConvert.controlFlowGraph.EntryBlock;
        dfs_walk(EntryBlock);
        Collections.reverse(VisitOrder);
        //RPO(Dom,EntryBlock);
        //Collections.reverse(VisitOrder);
    }

    public void dfs_walk(NormalBlock normalBlock) {
        Visited.add(normalBlock);
        ArrayList<NormalBlock> nextBlocks = nextMap.get(normalBlock);
        if (nextBlocks != null) {
            for (NormalBlock nextBlock : nextBlocks) {
                if (!Visited.contains(nextBlock)) {
                    dfs_walk(nextBlock);
                }
            }
        }
        VisitOrder.add(normalBlock);
    }


    public HashSet<NormalBlock> CollectDirectDom(NormalBlock normalBlock) {
        HashSet<NormalBlock> DomBlocks = new HashSet<>();
        ssaConvert.DirectDomination.forEach((key, value) -> {
            if (value == normalBlock) {
                DomBlocks.add(key);
            }
        });
        return DomBlocks;
    }

    public void RPO(HashMap<NormalBlock,HashSet<NormalBlock>> Dom,NormalBlock normalBlock) {
        HashSet<NormalBlock> DomBlocks = CollectDirectDom(normalBlock);
        DomBlocks.stream().filter(srcBlock -> !VisitOrder.contains(srcBlock)).forEach(srcBlock -> RPO(Dom,srcBlock));
        VisitOrder.add(normalBlock);
    }

    public boolean ProcessGlobalValueNumbering() {
        boolean isChanged = true;
        boolean OptimizeAffect = false;
        while (isChanged) {
            isChanged = false;
            for(NormalBlock normalBlock : VisitOrder) {
                ArrayList<PrimaryElement> elements = new ArrayList<>(normalBlock.IntermediateExpression);
                Combination<Boolean,PrimaryElement> Result;
                for (PrimaryElement element : elements) {
                    if (element instanceof CalculateElement) {
                        Result = ProcessConstValueFold(normalBlock,element);
                        isChanged |= Result.getKey();
                        element = Result.getValue();
                        Result = ProcessIdentTransform(normalBlock,element);
                        isChanged |= Result.getKey();
                        element = Result.getValue();
                        Result = ProcessEquivalentInst(normalBlock,element);
                        isChanged |= Result.getKey();
                    } else if (!(element instanceof PhiElement)){
                        Result = ProcessConstValueFold(normalBlock,element);
                        isChanged |= Result.getKey();
                        element = Result.getValue();
                        Result = ProcessIdentTransform(normalBlock,element);
                        isChanged |= Result.getKey();
                    } else {
                        Result = ProcessIdentTransform(normalBlock,element);
                        isChanged |= Result.getKey();
                    }
                }
            }
            OptimizeAffect |= isChanged;
        }
        return OptimizeAffect;
    }


    public Combination<Boolean,PrimaryElement> ProcessEquivalentInst(NormalBlock normalBlock,PrimaryElement primaryElement) {
        if (primaryElement != null) {
            CalculateElement element = (CalculateElement)primaryElement;
            //有全局变量参与运算，不进行优化，因为优化后会改变全局变量的值。
            for (PrimaryOperand operand : element.getUsed()) {
                if (operand instanceof VariableOperand var && var.getVariableType() == VariableType.GLOBAL ) {
                    return new Combination<>(false,element);
                }
            }
            for (PrimaryOperand operand : element.getDefine()) {
                if (operand instanceof VariableOperand var && var.getVariableType() == VariableType.GLOBAL) {
                    return new Combination<>(false,element);
                }
            }
            //不对内联的传参语句进行优化
            if (optimizeWhiteList.contains(primaryElement)) {
                return new Combination<>(false,element);
            }
            boolean isChange = false;
            PrimaryOperand operand = searchOperandByHashElement(element);
            if (operand != null && operand != element.getDefine().get(0)) {
                VariableOperand Def = (VariableOperand) element.getDefine().get(0);
                CalculateElement newElement = CalculateElement.createAddElement(Def,operand,getConst(0));
                ReplaceElement(normalBlock,element,newElement);
                element = newElement;
                isChange = true;
            } else {
                AddHashElement(element);

            }
            return new Combination<>(isChange,element);
        }
        return new Combination<>(false,null);
    }

    public PrimaryOperand getConst(int result) {
        PrimaryOperand constValue;
        if (ConstValueMap.containsKey(result)) {
            constValue = ConstValueMap.get(result);
        } else {
            constValue = new ConstValue(result);
            ConstValueMap.put(result,constValue);
        }
        return constValue;
    }

    public Combination<Boolean,PrimaryElement> ProcessIdentTransform(NormalBlock normalBlock, PrimaryElement element) {
        boolean isChanged = false;
        ArrayList<PrimaryOperand> UsedOperands = element.getUsed();
        ArrayList<VariableOperand> DefineOperands = element.getDefineVariable();
        //寻找等价替换，删除被替换变量的定义语句(实现了Calculate和Phi)
        //ReturnOperand是例外，不对其进行替换。主要是它无内存位置，无法进行替换。得及时给他复制到有内存位置的变量。
        //GlobalOperand也是例外，不对其进行替换。主要是替换后会导致全局变量的值改变，这是不允许的。
        if (element instanceof CalculateElement calculateElement) {
            VariableOperand DefineOp = DefineOperands.get(0);
            PrimaryOperand UseOp1 = UsedOperands.get(0);
            PrimaryOperand UseOp2 = UsedOperands.get(1);
            if (UseOp1 instanceof VariableOperand op1Var && UseOp2 instanceof ConstValue) {
                if (calculateElement.Hash() == UseOp1.GetOperandHash()
                        && !op1Var.getVariableType().equals(VariableType.RETURN)
                            && DefineOp.getVariableType() != VariableType.GLOBAL
                                && EqualValid(calculateElement)
                                    && !optimizeWhiteList.contains(element)) {
                    AddHashElement(calculateElement.getDefine().get(0).GetOperandHash(),UseOp1,calculateElement.getDefine().get(0));
                    normalBlock.IntermediateExpression.remove(calculateElement);
                    isChanged = true;
                    return new Combination<>(isChanged,null);
                }
            }
            else if (UseOp2 instanceof VariableOperand op2Var && UseOp1 instanceof ConstValue) {
                if (calculateElement.Hash() == UseOp2.GetOperandHash()
                        && !op2Var.getVariableType().equals(VariableType.RETURN)
                            && DefineOp.getVariableType() != VariableType.GLOBAL
                                && EqualValid(calculateElement)
                                    && !optimizeWhiteList.contains(element)) {
                    AddHashElement(calculateElement.getDefine().get(0).GetOperandHash(),UseOp2,calculateElement.getDefine().get(0));
                    normalBlock.IntermediateExpression.remove(calculateElement);
                    isChanged = true;
                    return new Combination<>(isChanged,null);
                }
            }
        } else if (element instanceof PhiElement phiElement) {
            ArrayList<PrimaryOperand> uses = phiElement.getUsed();
            if (uses.size() == 1) {
                AddHashElement(phiElement.getDefine().get(0).GetOperandHash(),uses.get(0),phiElement.getDefine().get(0));
                normalBlock.IntermediateExpression.remove(phiElement);
                isChanged = true;
                return new Combination<>(isChanged,null);
            }
        }
        //寻找等价替换，将被删除的变量替换为新变量
        for (PrimaryOperand Origin : UsedOperands) {
            PrimaryOperand Target = searchOperandByHashOperand(Origin);
            if (Target != null) {
                element.ReplaceUseVariable(Origin,Target);
                isChanged = true;
            }
        }
        return new Combination<>(isChanged,element);
    }

    public boolean EqualValid(CalculateElement calculateElement) {
        return  calculateElement.getOperatorName().equals("Add") ||
                calculateElement.getOperatorName().equals("Sub") ||
                calculateElement.getOperatorName().equals("Mul") ||
                calculateElement.getOperatorName().equals("Div");
    }

    public Combination<Boolean,PrimaryElement> ProcessConstValueFold(NormalBlock normalBlock,PrimaryElement element) {
        boolean CanCalculate = element instanceof CalculateElement;
        boolean isChanged = false;
        ArrayList<PrimaryOperand> UsedOperands = element.getUsed();
        ArrayList<PrimaryOperand> DefineOperands = element.getDefine();
        HashMap<PrimaryOperand,PrimaryOperand> ConvertMap = new HashMap<>();

        for (PrimaryOperand OriginOperand : UsedOperands) {
            if (OriginOperand instanceof VariableOperand) {
                PrimaryOperand convert = searchOperandByHashOperand(OriginOperand);
                if (!(convert instanceof ConstValue)) {
                    CanCalculate = false;
                } else {
                    ConvertMap.put(OriginOperand,convert);
                }
            }
        }
        //有全局变量参与运算，不进行优化，因为优化后会改变全局变量的值。
        for (PrimaryOperand operand : UsedOperands) {
            if (operand instanceof VariableOperand var && var.getVariableType() == VariableType.GLOBAL) {
                CanCalculate = false;
            }
        }
        for (PrimaryOperand operand : DefineOperands) {
            if (operand instanceof VariableOperand var && var.getVariableType() == VariableType.GLOBAL) {
                CanCalculate = false;
            }
        }
        if (CanCalculate) {
            if (!ConvertMap.isEmpty()) {
                isChanged = true;
            }
            ConvertMap.forEach(element::ReplaceUseVariable);
            CalculateElement calculateElement = (CalculateElement) element;
            int result = calculateElement.Calculate();
            PrimaryOperand constValue = getConst(result);
            AddHashElement(calculateElement.getDefine().get(0).GetOperandHash(),constValue,calculateElement.getDefine().get(0));
            VariableOperand var = (VariableOperand) calculateElement.getDefine().get(0);
            CalculateElement newElement = CalculateElement.createAddElement(var,getConst(result),getConst(0));
            ReplaceElement(normalBlock,calculateElement,newElement);
            element = newElement;
        }
        return new Combination<>(isChanged,element);
    }

    public void ReplaceElement(NormalBlock normalBlock,CalculateElement element,CalculateElement newElement) {
        for (int i = 0; i < normalBlock.IntermediateExpression.size(); i++) {
            PrimaryElement Compare = normalBlock.IntermediateExpression.get(i);
            if (Compare == element) {
                normalBlock.IntermediateExpression.add(i,newElement);
                normalBlock.IntermediateExpression.remove(element);
                break;
            }
        }
    }

    public PrimaryOperand searchOperandByHashElement(CalculateElement element) {
        int HashCode = element.Hash();
        if (Hash.containsKey(HashCode)) {
            HashMap<PrimaryElement, PrimaryOperand> hashMap = Hash.get(HashCode);
            for (Map.Entry<PrimaryElement,PrimaryOperand> entry : hashMap.entrySet()) {
                PrimaryElement CompareElement = entry.getKey();
                if (CompareElement.getOperatorName().equals(element.getOperatorName())) {
                    ArrayList<PrimaryOperand> CompareUses = CompareElement.getUsed();
                    ArrayList<PrimaryOperand> OriginUses = element.getUsed();
                    boolean Compare = true;
                    for(PrimaryOperand operand : OriginUses) {
                        if (!CompareUses.contains(operand)) {
                            Compare = false;
                            break;
                        }
                    }
                    if (Compare) {
                        return entry.getValue();
                    }
                }
            }
        }
        return null;
    }


    //单Operand
    public void AddHashElement(int HashCode,PrimaryOperand result,PrimaryOperand operand) {
        if (!Hash.containsKey(HashCode)) {
            HashMap<PrimaryElement,PrimaryOperand> Temp = new HashMap<>();
            Hash.put(HashCode,Temp);
        }
        if (searchOperandByHashOperand(operand) == null) {
            Hash.get(HashCode).put(OtherElements.createOperandElement(operand),result);
        }
    }

    //表达式
    public void AddHashElement(CalculateElement element) {
        int HashCode = element.Hash();
        PrimaryOperand result = element.getDefine().get(0);
        ArrayList<PrimaryOperand> uses = element.getUsed();
        ArrayList<PrimaryOperand> ConstUse = uses.stream().filter(operand -> operand instanceof ConstValue).collect(Collectors.toCollection(ArrayList::new));
        if (uses.size() == ConstUse.size()) {
            return;
        }
        // 我们不加入两个都是常量的变换，即我们认为常量就是不动点
        if (!Hash.containsKey(HashCode)) {
            HashMap<PrimaryElement,PrimaryOperand> Temp = new HashMap<>();
            Hash.put(HashCode,Temp);
        }
        if(searchOperandByHashElement(element) == null) {
            Hash.get(HashCode).put(element,result);
        }
    }

    public PrimaryOperand searchOperandByHashOperand(PrimaryOperand operand) {
        int hashCode = operand.GetOperandHash();
        HashMap<PrimaryElement,PrimaryOperand> MayRightMap = Hash.getOrDefault(hashCode,new HashMap<>());
        for (Map.Entry<PrimaryElement,PrimaryOperand> entry : MayRightMap.entrySet()) {
            PrimaryElement Compares = entry.getKey();
            PrimaryOperand Result = entry.getValue();
            if (Compares instanceof OtherElements otherElement && otherElement.getOperand() == operand) {
                return Result;
            }
        }
        return null;
    }


}
