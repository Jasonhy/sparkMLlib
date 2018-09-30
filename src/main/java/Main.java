import java.util.List;

public class Main {
    public static void main(String[] args){
        String words = "50832任何一个销售行业都需要人脉，人脉的多少决定了您产品的收益。太多创业者无法实现盈利，最主要的原因不是产品不好，而是没有客户。智能营销机器人帮助你寻找意向客户，月加30W客户。代理或了解产品添加2492249308,支持实地考察！";
        Segment segment = Segment.getInstance();
        List<String> nlp = segment.nlp(words);
        // System.out.println(nlp);
        String filePath = "D:\\java_workplace\\sparkMLlib\\src\\data\\es_data.json";
        DataHandler handler = DataHandler.getInstance();
        handler.handlerData(filePath);
    }
}
