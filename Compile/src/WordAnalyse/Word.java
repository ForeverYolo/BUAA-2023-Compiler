package WordAnalyse;

public abstract class Word {
    public String OriginWord;
    public int AtLine;

    public Word(String OriginWord,int AtLine) {
        this.OriginWord = OriginWord;
        this.AtLine =  AtLine;
    }

    public abstract String getCategoryCode();

    public abstract String getPrintCategoryCode();

    public String getOriginWord() {
        return OriginWord;
    }

    public abstract Word makeAWordByNormalHandler(String OriginWord, int AtLine);
}
