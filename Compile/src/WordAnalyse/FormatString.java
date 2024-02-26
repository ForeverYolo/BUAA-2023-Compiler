package WordAnalyse;

import Tools.ErrorMessage;

public class FormatString extends Word{
    public static final String Pattern = "\".*?\"";
    public static final String Sign = "WordAnalyse.FormatString";

    public FormatString(String OriginWord,int NowLineIndex) {
        super(OriginWord,NowLineIndex);
    }

    @Override
    public String getCategoryCode() {
        return "STRCON";
    }

    @Override
    public String getPrintCategoryCode() {
        return "STRCON";
    }

    public Word makeAWordByNormalHandler(String OriginWord, int AtLine) {
        return new FormatString(OriginWord,AtLine);
    }

    public static String getClassRecognition() {
        return "FormatString";
    }
}
