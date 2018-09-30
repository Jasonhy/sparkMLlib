import com.google.gson.*;
import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 对gson进行封装
 */
public class GsonUtils {

    /**
     * 将Json数据解析成相应的映射对象
     *
     * @param jsonData
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T parseJsonWithGson(String jsonData, Class<T> type) {
        T result = null;
        if (!TextUtils.isEmpty(jsonData)) {
            Gson gson = new GsonBuilder().create();
            try {
                result = gson.fromJson(jsonData, type);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (gson != null) {
                    gson = null;
                }
            }
        }
        return result;
    }


    /**
     * 将对象转换成Json
     *
     * @param bean
     * @param <T>
     * @return
     */
    public static <T> String toJsonWithSerializeNulls(T bean) {
        bean.getClass();
        Gson gson = new GsonBuilder().serializeNulls().create();
        String result = "";
        try {
            result = gson.toJson(bean);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (gson != null) {
                gson = null;
            }
        }
        return result;
    }

}
