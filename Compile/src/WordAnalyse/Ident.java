package WordAnalyse;

public class Ident extends Word{
    public static String Pattern = "[_a-zA-Z][_a-zA-Z0-9]*";
    public static final String Sign = "WordAnalyse.Ident";
    public Ident(String OriginWord,int NowLineIndex) {
        super(OriginWord,NowLineIndex);
    }
    @Override
    public String getCategoryCode() {
        return "IDENFR";
    }

    @Override
    public String getPrintCategoryCode() {
        return "IDENFR";
    }


    public Word makeAWordByNormalHandler(String OriginWord, int AtLine) {
        return new Ident(OriginWord,AtLine);
    }

    public static String getClassRecognition() {
        return "Ident";
    }
}
