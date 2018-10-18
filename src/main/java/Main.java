import bean.BaseBean;
import bean.QQBean;
import config.Config;
import helper.ESHandler;
import helper.Segment;
import model.THKMeans;
import util.FileUtil;
import util.GsonUtil;

import java.net.UnknownHostException;
import java.util.List;

public class Main {
    public static void main(String[] args){
        FileUtil instance = FileUtil.getInstance();
        Segment segment = Segment.getInstance();
        Config config = new Config();
        config.setPredictFilePath("");
        config.setTrainFilePath("");
        List<String> datas = instance.readByList(config.getPredictFilePath(), "utf-8");
        THKMeans thkMeans = new THKMeans();
        thkMeans.train(config,false);
        for(int i=0; i<datas.size(); i++){
            String beanStr = datas.get(i);
            BaseBean baseBean = GsonUtil.parseJsonWithGson(beanStr, BaseBean.class);
            baseBean.setWords(segment.nlp(baseBean.getBody()));
            QQBean qqBean = GsonUtil.parseJsonWithGson(beanStr, QQBean.class);
            if(baseBean.getWords().size() < 3 || beanStr.contains("加入本群")){
                continue;
            }
            System.out.println("======= " + baseBean.getWords());
            // 如果tag为true,则将数据插入es,否则不插入
            boolean tag = thkMeans.predict(baseBean);
            if (tag){
                try {
                    // 数据插入ES
                    ESHandler.init();
                    String res = ESHandler.insertByJson("qq_unique", "qq_unique_type", beanStr, qqBean.getUid());
                    System.out.println(">>> " + res);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
            break;
        }
    }
}
