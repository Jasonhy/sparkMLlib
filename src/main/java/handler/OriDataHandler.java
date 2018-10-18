package handler;

import config.Config;
import helper.ESHandler;
import helper.SimHash;
import model.THKMeans;
import org.apache.spark.sql.Row;
import util.FileUtil;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * 原始数据初次入库
 * 比较耗时
 */
public class OriDataHandler {
    public static void main(String[] args){
        // FileUtil fileUtil = FileUtil.getInstance();
        String trainPath = args[0];
        Config config = new Config();
        config.setTrainFilePath(trainPath);
        THKMeans thkMeans = new THKMeans();
        thkMeans.train(config,true);
        Map<Integer, Iterable<Row>> result = thkMeans.getKMeansResult();
        for (int i=0; i < Config.valK; i++){
            // 开启线性去执行
            int key = i;
            Thread thread = new Thread(){
                @Override
                public void run() {
                    // long stime = System.currentTimeMillis();
                    ArrayList<String> list = new ArrayList<>();
                    Iterable<Row> rows = result.get(key);
                    Iterator<Row> iterator = rows.iterator();
                    while (iterator.hasNext()){
                        boolean tag = true;
                        Row row = iterator.next();
                        String body = row.getString(0);
                        SimHash currHash = new SimHash(body,64);
                        String uid = row.getString(1);
                        if (list.size() == 0){
                            list.add(body);
                        }else{
                            for (int j=0; j<list.size(); j++){
                                SimHash oriHash = new SimHash(list.get(j), 64);
                                int d = currHash.hammingDistance(currHash.simHashVal, oriHash.simHashVal);
                                if (d <= 7) {
                                    tag = false;
                                    break;
                                }
                            }
                        }
                        if (tag){
                            list.add(body);
                            // System.out.println("============================");
                            // System.out.println("Type: " + key);
                            // System.out.println("Curr: " + body);
                            // System.out.println("List Size : " + list.size());
                            // 将数据插入ES
                            String jsonBean = thkMeans.getTrainDataByCache(uid);
                            try {
                                ESHandler.init();
                                String res = ESHandler.insertByJson("qq_unique", "qq_unique_type", jsonBean, uid);
                                System.out.println(">>>>>> Insert succ data:  " + res + " uid: " + uid + " <<<<<<<");
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                            // fileUtil.write( key + ".txt",uid,true,"UTF-8");
                        }
                    }
                    // System.out.println("Write end Type; " + key + "Spend Time: " + (System.currentTimeMillis()-stime));
                }
            };
            thread.start();
        }
    }
}
