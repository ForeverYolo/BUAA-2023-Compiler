package WordAnalyse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Reserved extends Word{
    public static final String Sign = "WordAnalyse.Reserved";
    public static final HashMap<String,String> TwoLevelTransformMapTo = new HashMap<>() {
        {
            //put("integer", "int");
            //put("INTEGERTK","INTTK");
        }
    };
    public static final HashMap<String,String> TwoLevelTransformMapBack = new HashMap<>();
    public static final HashMap<String,String> TransformMap = new HashMap<>(){
        {
            put("main","MAINTK");
            put("const", "CONSTTK");
            put("int", "INTTK");
            put("break", "BREAKTK");
            put("continue", "CONTINUETK");
            put("if", "IFTK");
            put("else", "ELSETK");
            put("for", "FORTK");
            put("getint", "GETINTTK");
            put("printf", "PRINTFTK");
            put("return", "RETURNTK");
            put("void", "VOIDTK");
        }
    };

    static { //根据TwoLevelTransformMapTo自动初始化TwoLevelTransformMapBac
        TwoLevelTransformMapTo.forEach((s, s2) -> TwoLevelTransformMapBack.put(s2,s));
    }

    //public static final String Pattern = "main|const|int|break|continue|if|else|for|getint|printf|return|void";
    public static final String Pattern = ProcessedPattern();
    public String type;

    public Reserved(String OriginWord,int NowLineIndex) {
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
        return new Reserved(OriginWord,AtLine);
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
            sb.append(sign).append("|");
        }
        return sb.deleteCharAt(sb.length()-1).toString();
    }

    public static String getClassRecognition() {
        return "Reserved";
    }


}
