package IntermediateCode.Elements;

import IntermediateCode.Container.NormalBlock;
import IntermediateCode.Container.PrimaryBlock;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import ObjectCode.Assembly;
import Tools.Combination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class PhiElement extends PrimaryElement{
    protected VariableOperand Dst;
    protected ArrayList<PrimaryOperand> SrcArray;
    protected ArrayList<Combination<PrimaryOperand,NormalBlock>> OriDefBlockList;
    protected HashMap<NormalBlock,PrimaryOperand> OriBlockDefMap;
    protected VariableOperand Verify;
    public PhiElement(VariableOperand Verify) {
        super("Phi");
        SrcArray = new ArrayList<>();
        this.Verify = Verify;
        this.OriBlockDefMap = new HashMap<>();
        this.OriDefBlockList = new ArrayList<>();
    }

    @Override
    public void setPlaceMessage(PrimaryBlock block, int Index) {
        this.normalBlock = block;
        this.normalLineIndex = Index;
    }

    @Override
    public void setAddress(int address) {}

    @Override
    public int getSpace() {return 0;}

    @Override
    public void ToAssembly(Assembly assembly) {}

    @Override
    public String toString() {
        if (Dst != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Phi ").append(Dst).append(" ");
            SrcArray.forEach(Src -> sb.append(Src).append(" "));
            return sb.toString();
        } else {
            return "Phi " + Verify;
        }
    }

    public boolean VerifyPhi(VariableOperand verify) {
        return Verify == verify;
    }

    public ArrayList<VariableOperand> getDefineVariable() {
        ArrayList<VariableOperand> DefineVariable = new ArrayList<>();
        if (Dst != null) {
            DefineVariable.add(Dst);
        } else if (Verify != null) {
            DefineVariable.add(Verify);
        }
        return DefineVariable;
    }

    @Override
    public ArrayList<PrimaryOperand> getDefine() {
        ArrayList<PrimaryOperand> DefineVariable = new ArrayList<>();
        if (Dst != null) {
            DefineVariable.add(Dst);
        } else if (Verify != null) {
            DefineVariable.add(Verify);
        }
        return DefineVariable;
    }

    @Override
    public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {
        if (Dst == null || Dst == origin) {
            Dst = (VariableOperand) target;
        }
    }

    //先删再加，若没有，删不会有副作用，若有，删了再加，不会有重复。
    //这个Element中的Map所反映的块，不一定是这个Operand真正所在的块，但一定在支配路径上，所以按照他寻找一定可以走到前驱。
    @Override
    public void ReplaceUseVariable(PrimaryOperand operand, PrimaryOperand target) {
        SrcArray.remove(operand);
        SrcArray.remove(target);
        SrcArray.add(target);
        if (OriDefBlockListContainsOperand(operand)) {
            NormalBlock sourceBlock = OriDefBlockListGetBlockFromOperand(operand);
            OriBlockDefMap.remove(sourceBlock);
            RemoveOriDefBlockListElement(operand);
            AddOriDefBlockListElement(target,sourceBlock);
            OriBlockDefMap.put(sourceBlock,target);
        }
    }

    public boolean OriDefBlockListContainsOperand(PrimaryOperand operand) {
        for(Combination<PrimaryOperand,NormalBlock> combination: OriDefBlockList) {
            if (combination.getKey() == operand) {
                return true;
            }
        }
        return false;
    }

    public void RemoveOriDefBlockListElement(PrimaryOperand operand) {
        ArrayList<Combination<PrimaryOperand,NormalBlock>> Temp = new ArrayList<>(OriDefBlockList);
        Temp.forEach(combination -> {
            if (combination.getKey() == operand) {
                OriDefBlockList.remove(combination);
            }
        });
    }

    public void AddOriDefBlockListElement(PrimaryOperand operand, NormalBlock normalBlock) {
        OriDefBlockList.add(new Combination<>(operand,normalBlock));
    }

    public void AddUseVariable(PrimaryOperand operand,NormalBlock normalBlock) {
        SrcArray.add(operand);
        AddOriDefBlockListElement(operand,normalBlock);
        OriBlockDefMap.put(normalBlock,operand);
    }

    public NormalBlock OriDefBlockListGetBlockFromOperand(PrimaryOperand operand) {
        for(Combination<PrimaryOperand,NormalBlock> combination: OriDefBlockList) {
            if (combination.getKey() == operand) {
                return combination.getValue();
            }
        }
        return null;
    }

    public ArrayList<NormalBlock> OriDefBlockListGetBlockFromOperands(PrimaryOperand operand) {
        ArrayList<NormalBlock> normalBlocks = new ArrayList<>();
        for(Combination<PrimaryOperand,NormalBlock> combination: OriDefBlockList) {
            if (combination.getKey() == operand) {
                normalBlocks.add(combination.getValue());
            }
        }
        return normalBlocks;
    }


    public void OriDefBlockListChangeBlock(PrimaryOperand operand,NormalBlock newBlock) {
        for(Combination<PrimaryOperand,NormalBlock> combination: OriDefBlockList) {
            if (combination.getKey() == operand) {
                combination.setValue(newBlock);
                break;
            }
        }
    }

    public HashMap<NormalBlock, PrimaryOperand> getOriBlockDefMap() {
        return OriBlockDefMap;
    }


    public ArrayList<VariableOperand> getUsedVariable() {
        return SrcArray.stream().filter(operand -> operand instanceof VariableOperand)
                .map(operand -> (VariableOperand)operand)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<PrimaryOperand> getUsed() {
        return new ArrayList<>(SrcArray);
    }

    //主要是由于块的合并，导致Phi节点的合并，需要修改Phi节点的定义块
    public void ChangeMapBlock(NormalBlock oldBlock,NormalBlock newBlock) {
        if (OriBlockDefMap.containsKey(oldBlock)) {
            PrimaryOperand operand = OriBlockDefMap.get(oldBlock);
            OriBlockDefMap.remove(oldBlock);
            OriBlockDefMap.put(newBlock,operand);
            OriDefBlockListChangeBlock(operand,newBlock);
        }
    }
}
