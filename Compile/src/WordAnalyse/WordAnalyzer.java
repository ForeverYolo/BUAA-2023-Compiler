package WordAnalyse;

import Tools.Combination;
import Tools.GlobalSetting;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordAnalyzer {
    private boolean CommentMark;

    private static final ArrayList<Combination<String,Word>> PatternArray = new ArrayList<>(){
        {
            add(new Combination<>(Reserved.Pattern,new Reserved("supporting",-1)));
            add(new Combination<>(Ident.Pattern,new Ident("supporting",-1)));
            add(new Combination<>(IntConst.Pattern,new IntConst("supporting",-1)));
            add(new Combination<>(FormatString.Pattern,new FormatString("supporting",-1)));
            add(new Combination<>(Delimiter.Pattern,new Delimiter("supporting",-1)));
        }
    };

    private int NowStartIndexInThisLine;

    public WordAnalyzer() {
        CommentMark = false;
        NowStartIndexInThisLine = 0;
    }
    public ArrayList<Word> RunWordAnalyzer(ArrayList<String> lines) {
        if (!GlobalSetting.RunWordAnalyzerResult) {
            return null;
        }
        ArrayList<Word> WordAnalyzerResult = new ArrayList<>();
        int NowLineIndex = 0;
        for(String line:lines) {
            NowLineIndex++;
            while(NowStartIndexInThisLine < line.length()) {
                skip(line);
                if (NowStartIndexInThisLine < line.length()) {
                    Word word = getNextWord(line.substring(NowStartIndexInThisLine),NowLineIndex);
                    WordAnalyzerResult.add(word);
                }
            }
            NowStartIndexInThisLine = 0;
        }
        return WordAnalyzerResult;
    }

    public Word getNextWord(String line,int NowLineIndex) {
        for(int i = 0; i < PatternArray.size();i++) {
           String Expression = PatternArray.get(i).getKey();
           Pattern pattern = Pattern.compile(Expression);
           Matcher matcher = pattern.matcher(line);
           if (matcher.find() && matcher.start() == 0) {
               if (i == 0 && tryToExtend(line,matcher.end())) {continue;}
               NowStartIndexInThisLine = NowStartIndexInThisLine + matcher.end();
               return PatternArray.get(i).getValue().makeAWordByNormalHandler(matcher.group(),NowLineIndex);
           }
        }
        return null;
    }


    public void skip(String line) {
        int LastStartIndexInThisLine;
        do {
            LastStartIndexInThisLine = NowStartIndexInThisLine;
            tryToSkipBlank(line);
            tryToSkipComment(line);
        } while (LastStartIndexInThisLine != NowStartIndexInThisLine);
    }

    public void tryToSkipBlank(String line) {
        while(NowStartIndexInThisLine < line.length() && (line.charAt(NowStartIndexInThisLine) == ' '
                || line.charAt(NowStartIndexInThisLine) == '\t'
                || line.charAt(NowStartIndexInThisLine) == '\n')) {
            NowStartIndexInThisLine++;
        }
    }

    public void tryToSkipComment(String line) {
        while (NowStartIndexInThisLine < line.length() && CommentMark) {
            if (NowStartIndexInThisLine < line.length() - 1 && line.charAt(NowStartIndexInThisLine) == '*' && line.charAt(NowStartIndexInThisLine + 1) == '/') {
                CommentMark = false;
                NowStartIndexInThisLine += 2;
            } else {
                NowStartIndexInThisLine++;
            }
        }
        if (NowStartIndexInThisLine < line.length() - 1) {
            if (line.charAt(NowStartIndexInThisLine) == '/' && line.charAt(NowStartIndexInThisLine + 1) == '/') {
                NowStartIndexInThisLine = line.length();
            } else if (line.charAt(NowStartIndexInThisLine) == '/' && line.charAt(NowStartIndexInThisLine + 1) == '*') {
                CommentMark = true;
                NowStartIndexInThisLine += 2;
            }
        }
    }

    public boolean tryToExtend(String line,int index) {
        if (index >= line.length()) {
            return false;
        } else {
            char check = line.charAt(index);
            return (check >= 65 && check <= 90) || (check >= 97 && check <= 122) || (check == 95) || (check >= 48 && check <= 57);
        }
    }
}
