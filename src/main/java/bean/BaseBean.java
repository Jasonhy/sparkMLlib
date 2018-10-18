package bean;

import java.io.Serializable;
import java.util.List;

/**
 * Bean 基类
 * 为了方便处理,从ES中导出的数据字段不管是
 * body还是content,统一定义为body
 */
public class BaseBean implements Serializable {
    /**
     * words: ["国内","满月","白"]
     * body: 国内满月白
     * uid : eb8767569f772f0355d2e43ec5fbb0a5
     */
    private List<String> words;
    private String body;
    private String uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
