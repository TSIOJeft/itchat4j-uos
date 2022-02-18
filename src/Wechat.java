package com.farplace.farpush.itchat;

import android.content.Context;
import android.os.Handler;

import com.farplace.farpush.itchat.controller.LoginController;
import com.farplace.farpush.itchat.core.MsgCenter;
import com.farplace.farpush.itchat.face.IMsgHandlerFace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Wechat {
    private static final Logger LOG = LoggerFactory.getLogger(Wechat.class);
    private IMsgHandlerFace msgHandler;
    private Handler handler;
    private LoginController loginController;
    private Context context;
    public static final String RECEIVE_MSG = "RECEIVE_MSG";

    public void setHandler(Handler handler) {
        this.handler = handler;
        if (loginController != null) {
            loginController.handler = handler;
        }
    }

    public Wechat(IMsgHandlerFace msgHandler, Handler handler, Context context) {
        System.setProperty("jsse.enableSNIExtension", "false"); // 防止SSL错误
        this.msgHandler = msgHandler;
        this.context = context;
        // 登陆
        loginController = new LoginController(context);
        loginController.setHandler(handler);
        loginController.login();
    }

    public void start() {

        MsgCenter.handleMsg(msgHandler);
    }

}
