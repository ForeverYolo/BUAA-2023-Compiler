package IntermediateCode.Operands;

import java.util.HashMap;

public abstract class PrimaryOperand {
    protected static HashMap<VariableType,String> TypeToString = new HashMap<>(){
        {
            put(VariableType.CONST,"Const_");
            put(VariableType.GLOBAL,"Global_");
            put(VariableType.PARAM,"Param_");
            put(VariableType.RETURN,"Return_");
            put(VariableType.VARIABLE,"Variable_");
            put(VariableType.TEMP,"Temp_");
        }
    };
    protected String OperandName;

    public PrimaryOperand(String name) {
        OperandName = name;
    }

    public String getOperandName() {
        return OperandName;
    }

    @Override
    public String toString() {
        return OperandName;
    }

    @Override
    public int hashCode() {
        return OperandName.hashCode();
    }

    public abstract int getSpace();

    public int GetOperandHash() {
        if (this instanceof ConstValue constValue) {
            return constValue.getValue();
        } else {
            return this.getOperandName().hashCode();
        }
    }
}
