//package com.hd.calculator.app.constant.log;
//
//import com.hd.calculator.app.business.AccountManager;
//import com.hd.calculator.app.function.db.entity.AccountEntity;
//
//public class TypeContent {
//    private long userId;
//    private String userName;
//    private int type;
//
//    private String typeDescription;
//
//    public long getUserId() {
//        return userId;
//    }
//
//    public void setUserId(long userId) {
//        this.userId = userId;
//    }
//
//    public String getUserName() {
//        return userName;
//    }
//
//    public void setUserName(String userName) {
//        this.userName = userName;
//    }
//
//    public String getTypeDescription() {
//        return typeDescription;
//    }
//
//    public void setTypeDescription(String typeDescription) {
//        this.typeDescription = typeDescription;
//    }
//
//    public int getType() {
//        return type;
//    }
//
//    public void setType(int type) {
//        this.type = type;
//    }
//
//    public static TypeContent createInstance(int type) {
//        TypeContent typeContent = new TypeContent();
//        typeContent.setType(type);
//        typeContent.setTypeDescription(ActionType.getTypeName(type));
//        AccountEntity account = AccountManager.getIns().getAccount();
//        if (account != null) {
//            typeContent.setUserName(account.getUserName());
//            typeContent.setUserId(account.getId());
//        }
//        return typeContent;
//    }
//
//
//}
