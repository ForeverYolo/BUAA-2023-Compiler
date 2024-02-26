package Tools;

import GrammarAnalyse.TreeNode;
import WordAnalyse.Word;

import java.util.ArrayList;

public class WordProvider {
    public static ArrayList<Word> words;
    public static ArrayList<Word> WaitPrintQueue = new ArrayList<>();
    public static ArrayList<String> WriteBuffer = new ArrayList<>();
    public static int NowIndex = 0;
    public WordProvider() {
    }

    public static void setWordProvider(ArrayList<Word> words) {
        WordProvider.words = words;
        WordProvider.NowIndex = 0;
    }

    public static Word GetLastWord() {
        if (NowIndex > 0) {
            return words.get(NowIndex - 1);
        } else {
            return null;
        }
    }

    public static Word GetNextWord() {
        if (NowIndex <= words.size()) {
            Word word = words.get(NowIndex);
            NowIndex++;
            WaitPrintQueue.add(word);
            return word;
        } else {
            return null;
        }
    }

    public static void RollBackWord(int step) {
        for(int i = 0; i < step; i++) {
            NowIndex = NowIndex - 1;
            WaitPrintQueue.remove(WaitPrintQueue.size() - 1);
        }
    }

    public static void ClearAndPrintWaitPrintQueue() {
        for(Word word: WaitPrintQueue) {
            System.out.println(word.getCategoryCode() + " " + word.getOriginWord());
        }
        WordProvider.WaitPrintQueue.clear();
    }

    public static void FileAndClearWaitPrintQueue() {
        for(Word word: WaitPrintQueue) {
            WriteBuffer.add(word.getPrintCategoryCode() + " " + word.getOriginWord());
        }
        WordProvider.WaitPrintQueue.clear();
    }

    public static Word CheckEndSign(Word ReferenceWord,Class<? extends TreeNode> srcClass,int ErrorIdInThisClass, String... EndSign) {
        Word word = GetNextWord();
        boolean flag = false;
        for (String AEndSign : EndSign) {
            if (word != null && word.getCategoryCode().equals(AEndSign)) {
                flag = true;
            }
        }
        if (flag) {
            return word;
        } else {
            RollBackWord(1);
            if (ReferenceWord != null) {
                ErrorMessage.handleError(srcClass,ErrorIdInThisClass,WordProvider.GetLastWord());
            } else {
                ErrorMessage.handleError(srcClass,ErrorIdInThisClass,word);
            }
            return word;
        }
    }

    public static boolean FindInExpFirst(Word word) {
        return word.getCategoryCode().equals("PLUS") || word.getCategoryCode().equals("MINU") || word.getCategoryCode().equals("IDENFR") ||
                word.getCategoryCode().equals("LPARENT") || word.getCategoryCode().equals("INTCON");
    }

    public static boolean FindInCondFirst(Word word) {
        return word.getCategoryCode().equals("PLUS") || word.getCategoryCode().equals("MINU") || word.getCategoryCode().equals("IDENFR") ||
                word.getCategoryCode().equals("LPARENT") || word.getCategoryCode().equals("INTCON") || word.getCategoryCode().equals("NOT");
    }


    public static void PrintWordAnalyzerResult(ArrayList<Word> words) {
        if (!GlobalSetting.PrintWordAnalyzerResult) {
            return;
        }
        for (Word word:words) {
            System.out.println(word.getCategoryCode() + " " + word.getOriginWord());
        }
    }


    public static boolean PreSearchAssignUntilSEMICN() {
        int now_index = NowIndex;
        Word word = WordProvider.GetNextWord();
        while (word != null && !word.getCategoryCode().equals("SEMICN")) {
            if (word.getCategoryCode().equals("ASSIGN")) {
                return true;
            }
        }
        int end_index = NowIndex;
        WordProvider.RollBackWord(end_index - now_index);
        return false;
    }
}
