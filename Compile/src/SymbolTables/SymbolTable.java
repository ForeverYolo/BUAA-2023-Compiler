package SymbolTables;

import Tools.ErrorMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SymbolTable {
    private SymbolTable Father;
    private ArrayList<SymbolTable> Children;
    private HashMap<String,PrimaryTuple> tuples;
    private int OffsetRecord;

    public SymbolTable(SymbolTable father) {
        Father = father;
        tuples = new HashMap<>();
        Children = new ArrayList<>();
        OffsetRecord = 0;
    }

    public HashMap<String, PrimaryTuple> getTuples() {
        return tuples;
    }

    public SymbolTable getFather() {
        return Father;
    }

    public void addTuple(String name, PrimaryTuple primaryTuple) {
        tuples.put(name,primaryTuple);
    }

    public int getOffsetRecord() {
        return OffsetRecord;
    }

    public void AddChildren(SymbolTable symbolTable) {
        Children.add(symbolTable);
    }

    public void updateOffset(PrimaryTuple tuple) {
        if (tuple instanceof SimpleVarDefTuple || tuple instanceof FormalArrayTuple) {
            OffsetRecord += 4;
        } else if (tuple instanceof RealArrayTuple) {
            OffsetRecord += ((RealArrayTuple) tuple).CalculateOccupySize();
        }
    }

    public int queryConstVariableValue(String name, ArrayList<Integer> index) {
        Optional<Integer> value = SearchConstValueInThisTable(this,name,index);
        if (value.isPresent()) {
            return value.get();
        }
        SymbolTable symbolTable = this.Father;
        while(symbolTable != null) {
            value = SearchConstValueInThisTable(symbolTable,name,index);
            if (value.isPresent()) {
                return value.get();
            }
            symbolTable = symbolTable.Father;
        }
        ErrorMessage.handleError(null,-1,null);
        return -1;
    }

    public PrimaryTuple queryTupleFromAllRelativeSymbolTable(String name,boolean CheckValid) {
        SymbolTable symbolTable = this;
        HashMap<String,PrimaryTuple> TuplesForThisTable;
        do {
            TuplesForThisTable = symbolTable.tuples;
            for(Map.Entry<String,PrimaryTuple> entry : TuplesForThisTable.entrySet()) {
                if(entry.getKey().equals(name)
                        && (!CheckValid || entry.getValue().isVaild())) {
                    return entry.getValue();
                }
            }
            symbolTable = symbolTable.Father;
        } while(symbolTable != null);
        return null;
    }

    public PrimaryTuple queryTupleFromCurrentSymbolTable(String name,boolean CheckParams) {
        SymbolTable symbolTable = this;
        HashMap<String,PrimaryTuple> TuplesForThisTable = symbolTable.tuples;
        for(Map.Entry<String,PrimaryTuple> entry : TuplesForThisTable.entrySet()) {
            if(entry.getKey().equals(name)) {
                return entry.getValue();
            } else if(entry.getValue() instanceof FuncDefTuple funcDefTuple) {
                if (CheckParams) {
                    ArrayList<PrimaryTuple> primaryTuples = funcDefTuple.getFuncFParamsType();
                    for (PrimaryTuple formalParam : primaryTuples) {
                        if(formalParam.getIdentName().equals(name)) {
                            return formalParam;
                        }
                    }
                }
            }
        }
        return null;
    }

    public Optional<Integer> SearchConstValueInThisTable(SymbolTable symbolTable, String name, ArrayList<Integer> index) {
        HashMap<String,PrimaryTuple> TuplesForThisTable = symbolTable.getTuples();
        for (Map.Entry<String,PrimaryTuple> entry : TuplesForThisTable.entrySet()) {
            if (entry.getKey().equals(name)) {
                if (entry.getValue() instanceof SimpleVarDefTuple) {
                    return Optional.of(((SimpleVarDefTuple) entry.getValue()).queryConstVarValue());
                } else if (entry.getValue() instanceof RealArrayTuple) {
                    return Optional.of(((RealArrayTuple) entry.getValue()).queryArrayValue(index));
                } else {
                   return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    public void ResetSymbolTableState() {
        tuples.forEach((s, primaryTuple) -> {
            if (!(primaryTuple instanceof FuncDefTuple)) {
                primaryTuple.setVaild(false);
            }
        });
        Children.forEach(SymbolTable::ResetSymbolTableState);
    }
}
