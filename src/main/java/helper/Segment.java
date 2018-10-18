package helper;

import com.hankcs.hanlp.tokenizer.NLPTokenizer;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;

import java.util.ArrayList;
import java.util.List;

/**
 * 分词处理
 * 创建单例模式
 */
public class Segment {
    private volatile static Segment instance;

    private Segment() {

    }

    public static Segment getInstance() {
        if (instance == null) {
            synchronized (Segment.class) {
                if (instance == null) {
                    instance = new Segment();
                }
            }
        }
        return instance;
    }

    /**
     * 使用nlp来作为分词器
     *
     * @param words
     */
    public List<String> nlp(String words) {
        words = words.replace(" ", "").replace("\n", "")
                .replace("\t", "").replace("\r", "")
                .replace("[表情]", "")
                .replace("表情","")
                .replace("[图片]","")
                .replace("图片","");
        List<String> wordList = new ArrayList<String>();
        List<Term> segmentList = NLPTokenizer.segment(words);
        // 应用停用词
        CoreStopWordDictionary.apply(segmentList);
        for (int i = 0; i < segmentList.size(); i++) {
            wordList.add(segmentList.get(i).word);
        }
        return wordList;
    }
}
