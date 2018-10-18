package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件处理
 */
public class FileUtil {
    private volatile static FileUtil instance;
    private String newLine;

    private FileUtil() {
        // 根据操作系统,处理换行符
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            newLine = "\r\n";
        } else {
            newLine = "\n";
        }
    }

    public static FileUtil getInstance() {
        if (instance == null) {
            synchronized (FileUtil.class) {
                if (instance == null) {
                    instance = new FileUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 将数据写入文件
     * @param path 文件路径
     * @param str 需要写入的内容
     * @param is_append 是否以追加的形式
     * @param encode 编码格式
     */
    public void write(String path, String str, boolean is_append, String encode) {
        try {
            File file = new File(path);
            if (!file.exists()){
                boolean newFile = file.createNewFile();
                System.out.println("Create new File: " + path);
            }
            //true表示追加
            FileOutputStream out = new FileOutputStream(file, is_append);
            StringBuffer sb = new StringBuffer();
            sb.append(str + newLine);
            out.write(sb.toString().getBytes(encode));
            out.close();
        } catch (IOException ex) {
            System.out.println(ex.getStackTrace());
        }
    }

    /**
     * 从文件中读取数据
     * @param path 文件路径
     * @param encode 文件编码
     * @return List
     */
    public List<String> readByList(String path,String encode) {
        List<String> lines = new ArrayList<String>();
        String tempstr = null;
        try {
            File file = new File(path);
            if(!file.exists()) {
                throw new FileNotFoundException();
            }
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, encode));
            while((tempstr = br.readLine()) != null) {
                lines.add(tempstr.toString());
            }
        } catch(IOException ex) {
            System.out.println(ex.getStackTrace());
        }
        return lines;
    }
}
