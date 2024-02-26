package IntermediateCode.Elements;

import IntermediateCode.Container.PrimaryBlock;
import IntermediateCode.Operands.ConstValue;
import IntermediateCode.Operands.PrimaryOperand;
import IntermediateCode.Operands.VariableOperand;
import ObjectCode.Assembly;
import ObjectCode.Template.CalTemplate;
import ObjectCode.Template.MemTemplate;

import java.util.ArrayList;

public abstract class MemoryElements extends PrimaryElement{
    protected PrimaryOperand addr;
    protected PrimaryOperand value;

    public MemoryElements(String name,PrimaryOperand value,PrimaryOperand operand) {
        super(name);
        this.addr = operand;
        this.value = value;
    }



    public void setPlaceMessage(PrimaryBlock block, int Index) {
        this.normalBlock = block;
        this.normalLineIndex = Index;
        if (addr != null && addr instanceof VariableOperand) {
            ((VariableOperand) addr).setPlaceMessage(block,Index);
        }
        if (value != null && value instanceof VariableOperand) {
            ((VariableOperand) value).setPlaceMessage(block,Index);
        }
    }

    public static MemoryElements CreateAllocElement(PrimaryOperand value,PrimaryOperand size) {
        return new MemoryElements("Alloc",value,size) {
            @Override
            public void setAddress(int address) {
                ((VariableOperand)this.value).setOffset(address);
            }

            @Override
            public int getSpace() {
                return ((ConstValue)size).getValue() + 1;
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                CalTemplate.loadAddrTemplate(value,((VariableOperand)this.value).getOffset() + 1,assembly,assembly.getObjectCode());
                ((VariableOperand)value).setNeedWriteBack(assembly);
            }

            public ArrayList<VariableOperand> getDefineVariable() {
                ArrayList<VariableOperand> DefineVariable = new ArrayList<>();
                if (value instanceof VariableOperand v_value) {
                    DefineVariable.add(v_value);
                }
                return DefineVariable;
            }

            @Override
            public ArrayList<PrimaryOperand> getDefine() {
                ArrayList<PrimaryOperand> DefineVariable = new ArrayList<>();
                DefineVariable.add(value);
                return DefineVariable;
            }

            public ArrayList<VariableOperand> getUsedVariable() {
                ArrayList<VariableOperand> UsedVariable = new ArrayList<>();
                if (addr != null && addr instanceof VariableOperand v_addr) {
                    UsedVariable.add(v_addr);
                }
                return UsedVariable;
            }

            @Override
            public ArrayList<PrimaryOperand> getUsed() {
                ArrayList<PrimaryOperand> UsedVariable = new ArrayList<>();
                UsedVariable.add(addr);
                return UsedVariable;
            }

            @Override
            public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {
                if (value != null && value == origin) {
                    value = target;
                }
            }

            @Override
            public void ReplaceUseVariable(PrimaryOperand operand, PrimaryOperand target) {
                if (addr != null && addr == operand) {
                    addr = target;
                }
            }
        };
    }

    public static MemoryElements CreateStoreElement(PrimaryOperand value,PrimaryOperand addr) {
        return new MemoryElements("Store",value,addr) {
            @Override
            public void setAddress(int address) {}

            @Override
            public int getSpace() {
                return 0;
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                MemTemplate.storeWordTemplate(value,addr,assembly,assembly.getObjectCode());
            }

            public ArrayList<VariableOperand> getUsedVariable() {
                ArrayList<VariableOperand> UsedVariable = new ArrayList<>();
                if (value instanceof VariableOperand v_value) {
                    UsedVariable.add(v_value);
                }
                if (addr != null && addr instanceof VariableOperand v_addr) {
                    UsedVariable.add(v_addr);
                }
                return UsedVariable;
            }

            @Override
            public ArrayList<PrimaryOperand> getUsed() {
                ArrayList<PrimaryOperand> UsedVariable = new ArrayList<>();
                UsedVariable.add(addr);
                UsedVariable.add(value);
                return UsedVariable;
            }

            @Override
            public ArrayList<VariableOperand> getDefineVariable() {
                return new ArrayList<>();
            }

            @Override
            public ArrayList<PrimaryOperand> getDefine() {
                return new ArrayList<>();
            }

            @Override
            public void ReplaceUseVariable(PrimaryOperand origin, PrimaryOperand target) {
                if (value != null && value == origin) {
                    value = target;
                }
                if (addr != null && addr == origin) {
                    addr = target;
                }
            }

            @Override
            public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {}
        };
    }

    public static MemoryElements CreateLoadElement(PrimaryOperand value,PrimaryOperand addr) {
        return new MemoryElements("Load",value,addr) {
            @Override
            public void setAddress(int address) {
                ((VariableOperand)value).setOffset(address);
            }

            @Override
            public int getSpace() {
                return value.getSpace();
            }

            @Override
            public void ToAssembly(Assembly assembly) {
                MemTemplate.loadWordTemplate(value,addr,assembly,assembly.getObjectCode());
                ((VariableOperand)value).setNeedWriteBack(assembly);
            }

            public ArrayList<VariableOperand> getDefineVariable() {
                ArrayList<VariableOperand> DefineVariable = new ArrayList<>();
                if (value instanceof VariableOperand v_value) {
                    DefineVariable.add(v_value);
                }
                return DefineVariable;
            }

            @Override
            public ArrayList<PrimaryOperand> getDefine() {
                ArrayList<PrimaryOperand> DefineVariable = new ArrayList<>();
                DefineVariable.add(value);
                return DefineVariable;
            }

            public ArrayList<VariableOperand> getUsedVariable() {
                ArrayList<VariableOperand> UsedVariable = new ArrayList<>();
                if (addr != null && addr instanceof VariableOperand v_addr) {
                    UsedVariable.add(v_addr);
                }
                return UsedVariable;
            }

            @Override
            public ArrayList<PrimaryOperand> getUsed() {
                ArrayList<PrimaryOperand> UsedVariable = new ArrayList<>();
                UsedVariable.add(addr);
                return UsedVariable;
            }

            @Override
            public void ReplaceDefineVariable(PrimaryOperand origin, PrimaryOperand target) {
                if (value != null && value == origin) {
                    value = target;
                }
            }

            @Override
            public void ReplaceUseVariable(PrimaryOperand operand, PrimaryOperand target) {
                if (addr != null && addr == operand) {
                    addr = target;
                }
            }
        };
    }

    public PrimaryOperand getValue() {
        return value;
    }

    public static boolean isLoad(MemoryElements element) {
        return element.operatorName.equals("Load");
    }

    public static boolean isStore(MemoryElements elements) {return elements.operatorName.equals("Store");}

    public void setAddr(PrimaryOperand addr) {
        this.addr = addr;
    }

    public void setValue(PrimaryOperand value) {
        this.value = value;
    }

    public static boolean isAlloc(MemoryElements elements) {
        return elements.operatorName.equals("Alloc");
    }

    public static boolean isKeyElement(PrimaryElement element) {
        if (element instanceof MemoryElements memoryElement) {
            return isStore(memoryElement);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.operatorName + " " + value.toString() + " (" + addr.toString() + ")";
    }
}
