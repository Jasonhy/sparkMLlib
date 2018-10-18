package bean;

/**
 * 处理QQ情报数据
 */
public class QQBean extends BaseBean{
    /**
     * name : 腾讯微信(758963478)
     * group_no : 微信 陌陌 注册群
     * group_desc :
     * group_name : 微信 陌陌 注册群
     * time : 2017-10-26 08:39:41
     */
    private String name;
    private String group_no;
    private String group_desc;
    private String group_name;
    private String time;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup_no() {
        return group_no;
    }

    public void setGroup_no(String group_no) {
        this.group_no = group_no;
    }

    public String getGroup_desc() {
        return group_desc;
    }

    public void setGroup_desc(String group_desc) {
        this.group_desc = group_desc;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "QQBean{" +
                "name='" + name + '\'' +
                ", group_no='" + group_no + '\'' +
                ", group_desc='" + group_desc + '\'' +
                ", group_name='" + group_name + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
