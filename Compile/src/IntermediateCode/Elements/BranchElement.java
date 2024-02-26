package IntermediateCode.Elements;

import IntermediateCode.Container.PrimaryBlock;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import IntermediateCode.Operands.TagOperand;
import ObjectCode.Assembly;
import ObjectCode.Template.BranchTemplate;

import java.util.ArrayList;

public abstract class BranchElement extends PrimaryElement {
    protected TagOperand tag1;
    protected TagOperand tag2;
    protected PrimaryOperand src1;
    protected PrimaryOperand src2;

    @Override
    public ArrayList<VariableOperand> getDefineVariable() {
        return new ArrayList<>();
    }

    @Override
    public void ReplaceUseVariable(PrimaryOperand origin, PrimaryOperand target) {
        if (src1 == origin) {
            src1 = target;
        }
        if (src2 == origin) {
            src2 = target;
        }
    }

    @Override
    public ArrayList<PrimaryOperand> getDefine() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<PrimaryOperand> getUsed() {
        ArrayList<PrimaryOperand> UsedVariable = new ArrayList<>();
        if (src1 != null) {
            UsedVariable.add(src1);
        }
        if (src2 != null) {
            UsedVariable.add(src2);
        }
        return UsedVariable;
    }

    @Override
    public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {}

    @Override
    public ArrayList<VariableOperand> getUsedVariable() {
        ArrayList<VariableOperand> UsedVariable = new ArrayList<>();
        if (src1 != null && src1 instanceof VariableOperand src_1) {
            UsedVariable.add(src_1);
        }
        if (src2 != null && src2 instanceof VariableOperand src_2) {
            UsedVariable.add(src_2);
        }
        return UsedVariable;
    }

    public BranchElement(String name, PrimaryOperand src1, PrimaryOperand src2, TagOperand tag1, TagOperand tag2) {
        super(name);
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.src1 = src1;
        this.src2 = src2;
    }

    public BranchElement(String name,PrimaryOperand src1,TagOperand tag1,TagOperand tag2) {
        super(name);
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.src1 = src1;
        this.src2 = null;
    }

    public TagOperand getTag1() {
        return tag1;
    }

    public TagOperand getTag2() {
        return tag2;
    }

    public void setPlaceMessage(PrimaryBlock block, int Index) {
        this.normalBlock = block;
        this.normalLineIndex = Index;
        if (src1 != null && src1 instanceof VariableOperand) {
            ((VariableOperand) src1).setPlaceMessage(block,Index);
        }
        if (src2 != null && src2 instanceof VariableOperand) {
            ((VariableOperand) src2).setPlaceMessage(block,Index);
        }
    }


    public static BranchElement createBnzElement(TagOperand tag1, TagOperand tag2, PrimaryOperand src) {
        return new BranchElement("bnz",src,tag1,tag2){
            @Override
            public void setAddress(int address) {
                if (src1 instanceof VariableOperand) {
                    ((VariableOperand) src1).setOffset(address);
                }
            }

            @Override
            public int getSpace() {
                return src1.getSpace();
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                BranchTemplate.BnzTemplate(src1,tag1.getOperandName(),tag2.getOperandName(),assembly.getObjectCode(),assembly);
            }
        };
    }

    public static BranchElement createBeqzElement(TagOperand tag1, TagOperand tag2, PrimaryOperand src) {
        return new BranchElement("beqz",src,tag1,tag2){
            @Override
            public void setAddress(int address) {
                if (src1 instanceof VariableOperand) {
                    ((VariableOperand) src1).setOffset(address);
                }
            }

            @Override
            public int getSpace() {
                return src1.getSpace();
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                BranchTemplate.BeqzTemplate(src1,tag1.getOperandName(),tag2.getOperandName(),assembly.getObjectCode(),assembly);
            }
        };
    }

    public static BranchElement createBneElement(TagOperand tag1,TagOperand tag2,PrimaryOperand src1,PrimaryOperand src2) {
        return new BranchElement("bne",src1,src2,tag1,tag2){
            @Override
            public void setAddress(int address) {}

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                BranchTemplate.BneTemplate(src1,src2,tag1.getOperandName(),tag2.getOperandName(),assembly.getObjectCode(),assembly);
            }
        };
    }

    public static BranchElement createBeqElement(TagOperand tag1,TagOperand tag2,PrimaryOperand src1,PrimaryOperand src2) {
        return new BranchElement("beq",src1,src2,tag1,tag2){
            @Override
            public void setAddress(int address) {}

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                BranchTemplate.BeqTemplate(src1,src2,tag1.getOperandName(),tag2.getOperandName(),assembly.getObjectCode(),assembly);
            }
        };
    }

    public static BranchElement createBgezElement(TagOperand tag1,TagOperand tag2,PrimaryOperand src) {
        return new BranchElement("bgez",src,tag1,tag2){
            @Override
            public void setAddress(int address) {}

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                BranchTemplate.BgezTemplate(src1,tag1.getOperandName(),tag2.getOperandName(),assembly.getObjectCode(),assembly);
            }
        };
    }

    public static BranchElement createBgtzElement(TagOperand tag1,TagOperand tag2,PrimaryOperand src) {
        return new BranchElement("bgtz",src,tag1,tag2){
            @Override
            public void setAddress(int address) {}

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                BranchTemplate.BgtzTemplate(src1,tag1.getOperandName(),tag2.getOperandName(),assembly.getObjectCode(),assembly);
            }
        };
    }

    public static BranchElement createBlezElement(TagOperand tag1,TagOperand tag2,PrimaryOperand src) {
        return new BranchElement("blez",src,tag1,tag2) {
            @Override
            public void setAddress(int address) {}

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                BranchTemplate.BlezTemplate(src1,tag1.getOperandName(),tag2.getOperandName(),assembly.getObjectCode(),assembly);
            }
        };
    }

    public static BranchElement createBltzElement(TagOperand tag1,TagOperand tag2,PrimaryOperand src) {
        return new BranchElement("bltz",src,tag1,tag2) {
            @Override
            public void setAddress(int address) {}

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                BranchTemplate.BltzTemplate(src1,tag1.getOperandName(),tag2.getOperandName(),assembly.getObjectCode(),assembly);
            }
        };
    }

    @Override
    public String toString() {
        if (src2 == null) {
            return this.operatorName + " " + src1.toString() + " " + tag1.toString() + " " + tag2.toString();
        } else {
            return this.operatorName + " " + src1.toString() + " " + src2.toString() + " " + tag1.toString() + " " + tag2.toString();
        }
    }

    public void setSrc2(PrimaryOperand src2) {
        this.src2 = src2;
    }

    public void setSrc1(PrimaryOperand src1) {
        this.src1 = src1;
    }

    public void ChangeTag(TagOperand oldTag,TagOperand NewTag) {
        if (tag1 == oldTag) {
            tag1 = NewTag;
        } else if (tag2 == oldTag) {
            tag2 = NewTag;
        }
    }
}
