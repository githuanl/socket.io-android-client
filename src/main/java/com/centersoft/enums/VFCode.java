package com.centersoft.enums;


public enum VFCode {

    REQ_NONETWORK(0, "网络未连接"),  //网络未连接

    REQ_FileUploadERROR(1, "文件上传失败"),  //文件上传失败
    REQ_FileERROR(2, "文件不存在"),         //文件不存在

    REQ_ORTHERERROR(3, "未知错误"),         //未知错误
    REQ_REQERROR(4, "请求错误"),            //请求错误
    REQ_TIMEOUT(5, "连接超时"),             //连接超时
    REQ_CANCLE(6, "请求取消"),              //请求取消
    REQ_CONNEXCEPTION(7, "服务器连接失败");  //服务器连接失败

    
    private int value;
    private String text;

    private VFCode(int var, String text) {
        this.value = var;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
