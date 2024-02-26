package Tools;

import WordAnalyse.Word;

import java.util.ArrayList;

public class CheckPoint {
    public ArrayList<Word> WaitPrintQueue;
    public ArrayList<String> WriteBuffer;
    public int NowIndex = 0;
    public CheckPoint(ArrayList<Word> WaitPrintQueue,ArrayList<String> WriteBuffer,int NowIndex) {
        this.NowIndex = NowIndex;
        this.WaitPrintQueue = new ArrayList<>();
        this.WaitPrintQueue.addAll(WaitPrintQueue);
        this.WriteBuffer = new ArrayList<>();
        this.WriteBuffer.addAll(WriteBuffer);
    }

    public void Restore() {
        WordProvider.WriteBuffer = WriteBuffer;
        WordProvider.WaitPrintQueue = WaitPrintQueue;
        WordProvider.NowIndex = NowIndex;
    }
}
