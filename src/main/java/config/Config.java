package config;


/**
 * 配置文件
 */
public class Config {
    private String trainFilePath;
    private String predictFilePath;
    // 设置分类数量
    public static int valK = 200;
    // ES配置信息
    public static String esIp = "es-cn-v0h0m3r910001wzha.elasticsearch.aliyuncs.com";
    public static String esUsernameAndPasswd = "elastic:1fW9Wo6DFPCuz@2R";
    public static int esPort = 9300;

    public String getTrainFilePath() {
        return trainFilePath;
    }

    public void setTrainFilePath(String trainFilePath) {
        this.trainFilePath = trainFilePath;
    }

    public String getPredictFilePath() {
        return predictFilePath;
    }

    public void setPredictFilePath(String predictFilePath) {
        this.predictFilePath = predictFilePath;
    }
}
