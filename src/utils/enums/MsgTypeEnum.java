package com.farplace.farpush.itchat.utils.enums;


import java.util.Objects;

/**
 * 消息类型枚举类
 *
 * @author https://github.com/yaphone
 * @version 1.0
 * @date 创建时间：2017年5月13日 下午11:53:00
 */
public enum MsgTypeEnum {
    TEXT("Text", "文本消息"),
    PIC("Pic", "图片消息"),
    VOICE("Voice", "语音消息"),
    VIEDO("Viedo", "小视频消息"),
    NAMECARD("NameCard", "名片消息"),
    SYS("Sys", "系统消息"),
    VERIFYMSG("VerifyMsg", "添加好友"),
    MEDIA("app", "文件消息");

    private String type;
    private String code;

    MsgTypeEnum(String type, String code) {
        this.type = type;
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public String getCode() {
        return code;
    }


    public static MsgTypeEnum fromType(String type) {
        if (type != null) {
            for (MsgTypeEnum value : values()) {
                if (Objects.equals(type, value.getType())) {
                    return value;
                }
            }
        }
        return null;
    }
}
