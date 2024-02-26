package WordAnalyse;

public class IntConst extends Word{
    public static final String Pattern = "([1-9][0-9]*)|(0)";
    public static final String Sign = "WordAnalyse.IntConst";

    public IntConst(String OriginWord,int NowLineIndex) {
        super(OriginWord,NowLineIndex);
    }

    @Override
    public String getCategoryCode() {
        return "INTCON";
    }

    @Override
    public String getPrintCategoryCode() {
        return "INTCON";
    }

    public Word makeAWordByNormalHandler(String OriginWord, int AtLine) {
        return new IntConst(OriginWord,AtLine);
    }

    public static String getClassRecognition() {
        return "IntConst";
    }
}
