package WordAnalyse;

import java.util.*;

public class Delimiter extends Word{
    public static final String Sign = "WordAnalyse.Delimiter";
    public static final HashMap<String,String> TwoLevelTransformMapTo = new HashMap<>();
    public static final HashMap<String,String> TwoLevelTransformMapBack = new HashMap<>();
    public static final HashMap<String,String> TransformMap = new HashMap<>(){
        {
            put("!", "NOT");
            put("&&", "AND");
            put("||", "OR");
            put("+", "PLUS");
            put("-", "MINU");
            put("*", "MULT");
            put("/", "DIV");
            put("%", "MOD");
            put("<", "LSS");
            put("<=", "LEQ");
            put(">", "GRE");
            put(">=", "GEQ");
            put("==", "EQL");
            put("!=", "NEQ");
            put("=", "ASSIGN");
            put(";", "SEMICN");
            put(",", "COMMA");
            put("(", "LPARENT");
            put(")", "RPARENT");
            put("[", "LBRACK");
            put("]", "RBRACK");
            put("{", "LBRACE");
            put("}", "RBRACE");
        }
    };

    static {
        TwoLevelTransformMapTo.forEach((s, s2) -> TwoLevelTransformMapBack.put(s2,s));
    }

    public static final HashMap<String,String> NeedProcessMap = new HashMap<>(){
        {
            put("||","\\|\\|");
            put("+","\\+");
            put("*","\\*");
            put("(", "\\(");
            put(")", "\\)");
            put("[", "\\[");
            put("]", "\\]");
            put("{", "\\{");
            put("}", "\\}");
        }
    };
    public static final String Pattern = ProcessedPattern();
    public String type;
    public Delimiter(String OriginWord,int NowLineIndex) {
        super(TwoLevelTransformMapTo.getOrDefault(OriginWord,OriginWord),NowLineIndex);
        type = TwoLevelTransformMapTo.getOrDefault(TransformMap.get(OriginWord),TransformMap.get(OriginWord));
        //super(OriginWord,NowLineIndex);
        //type = TransformMap.get(OriginWord);
    }

    @Override
    public String getCategoryCode() {
        return type;
    }

    @Override
    public String getPrintCategoryCode() {
        return TwoLevelTransformMapBack.getOrDefault(type,type);
    }

    public Word makeAWordByNormalHandler(String OriginWord, int AtLine) {
        return new Delimiter(OriginWord,AtLine);
    }

    public static String ProcessedPattern() {
        StringBuilder sb = new StringBuilder();
        ArrayList<String> sortArray = new ArrayList<>(TransformMap.keySet());
        sortArray.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });
        for (String sign : sortArray) {
            sb.append(NeedProcessMap.getOrDefault(sign, sign)).append("|");
        }
        return sb.deleteCharAt(sb.length()-1).toString();
    }

    public static String getClassRecognition() {
        return "Delimiter";
    }
}
