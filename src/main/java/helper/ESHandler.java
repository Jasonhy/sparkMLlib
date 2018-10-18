package helper;

import config.Config;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.shield.ShieldPlugin;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 操作ES
 */
public class ESHandler {
    private static TransportClient client;
    /**
     * 初始化ES连接
     * @throws UnknownHostException
     */
    public static void init() throws UnknownHostException {
        // 配置ES
        Settings settings = Settings.builder()
                .put("cluster.name", "elasticsearch")
                .put("shield.user",Config.esUsernameAndPasswd)
                .build();

        client = TransportClient.builder()
                .addPlugin(ShieldPlugin.class)
                .settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(Config.esIp),Config.esPort));
        // 创建client
        // client = new PreBuiltTransportClient(settings);
        // 设置连接
        // client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(Config.esIp),Config.esPort));

        System.out.println(">>>>>>> ES成功连接 <<<<<<<");
    }

    /**
     * 指定id,插入json数据
     * @param index 索引
     * @param type  type
     * @param jsonStr json数据
     * @param id  id
     */
    public static String insertByJson(String index, String type,String jsonStr,String id){
        IndexResponse response = client.prepareIndex(index, type, id).setSource(jsonStr, XContentType.JSON).get();
        return "status: " + response.status() + " id: " + response.getId();
    }

    /**
     * 根据ID获取查询结果数据
     * @param index 索引
     * @param type type
     * @param id    id
     */
    public static GetResponse getDataById(String index, String type,String id){
        return client.prepareGet(index, type, id).get();
    }
}
