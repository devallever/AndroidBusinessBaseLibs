package com.hd.calculator.app.function.network.post;


public class PostUserLog {
    /**
     * 用户操作类型, ActionType
     */
    private int type;

    private String content; //没用

    private String text; //当次行为上传的数据 /api/app/dining/table/use/upload/data 这个接口的参数对象json
    private String allMsg;//数据库数据 List<OrderWithDishesRef> 的json，
    // package com.hd.calculator.app.function.db.entity.operation.OrderWithDishesRef

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAllMsg() {
        return allMsg;
    }

    public void setAllMsg(String allMsg) {
        this.allMsg = allMsg;
    }
}
