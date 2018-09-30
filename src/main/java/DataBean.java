import java.io.Serializable;
import java.util.List;

public class DataBean implements Serializable {

    /**
     * content : 修正了
     * uid : 4130670479274854a95a8d3501f7cf4f
     */

    private String body;
    private String uid;
    // 对content进行分词
    private List<String> words;

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String content) {
        this.body = content;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "DataBean{" +
                "content='" + body + '\'' +
                ", uid='" + uid + '\'' +
                ", words=" + words +
                '}';
    }
}
