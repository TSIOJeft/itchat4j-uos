package com.farplace.farpush.itchat.controller;

import com.farplace.farpush.itchat.api.WechatTools;
import com.farplace.farpush.itchat.core.Core;
import com.farplace.farpush.itchat.service.ILoginService;
import com.farplace.farpush.itchat.service.impl.LoginServiceImpl;
import com.farplace.farpush.itchat.utils.SleepUtils;
import com.farplace.farpush.itchat.utils.tools.CommonTools;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.farplace.farpush.itchat.utils.Config.DATA_STORAGE_PATH;
import static com.farplace.farpush.itchat.utils.MyHttpClient.cookieManager;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.io.*;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * 登陆控制器
 *
 * @author https://github.com/yaphone
 * @version 1.0
 * @date 创建时间：2017年5月13日 下午12:56:07
 */
public class LoginController {
    private static Logger LOG = LoggerFactory.getLogger(LoginController.class);
    private ILoginService loginService;
    private Core core = Core.getInstance();
    public Handler handler;
    private Context context;

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public LoginController() {
        loginService = new LoginServiceImpl();
    }

    public LoginController(Context context) {
        this.context = context;
        loginService = new LoginServiceImpl(context);
    }

    public void  login() {
        String qrPath = DATA_STORAGE_PATH + File.separator + "qr_code.jpg";
        if (core.isAlive()) { // 已登陆
            LOG.info("itchat4j已登陆");
            sendNotice("正在为您心跳");
            Message message = Message.obtain();
            message.what = 3;
            handler.sendMessage(message);
            return;
        }
        sendNotice("初始化数据");
        hotReload();
        initCookie();
        initMember();
        if (core.loginInfo.size() == 0) {
            sendNotice("扫描二维码");
            initWeChatQrCode(qrPath);
            if (!core.isAlive()) {
                loginService.login();
                core.setAlive(true);
                sendNotice("登录成功");
            }
//                System.out.println("4. 登陆超时，请重新扫描二维码图片");

        }
        sendNotice("登陆成功，微信初始化");
        if (!loginService.webWxInit()) {
            sendNotice(" 微信初始化异常，请重新登录");
            core.setAlive(false);
            //重新加载重新登陆
            new File(DATA_STORAGE_PATH, "info.txt").delete();
            core.loginInfo = new HashMap<>();
            login();
            return;
        }
//
//        sendNotice("开启微信状态通知");
//        loginService.wxStatusNotify();

        sendNotice("清除");
        CommonTools.clearScreen();
        core.getContactList().clear();
        sendNotice(String.format("欢迎回来， %s", core.getNickName()));

        sendNotice("获取联系人信息");
        loginService.webWxGetContact();

        sendNotice("获取群好友及群好友列表");
        loginService.WebWxBatchGetContact();

        sendNotice("缓存本次登陆好友相关消息");
        WechatTools.setUserInfo(); // 登陆成功后缓存本次登陆好友相关消息（NickName, UserName）

        sendNotice("开启微信状态检测线程");
        new Thread(new MainServiceThread()).start();

        sendNotice("开始接收消息");
        loginService.startReceiving();
        sendNotice("正在为您心跳");
        Message message = Message.obtain();
        message.what = 3;
        handler.sendMessage(message);
    }

    private void hotReload() {
        File file = new File(DATA_STORAGE_PATH, "info.txt");
        if (file.exists() && file.canRead()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                StringBuilder stringBuilder = new StringBuilder();
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                Core core_temp = new Gson().fromJson(stringBuilder.toString(), Core.class);
                bufferedReader.close();
                if (core_temp != null && core_temp.loginInfo != null && core_temp.loginInfo.size() > 0) {
                    core.copy(core_temp);
                    core.setLastNormalRetcodeTime(System.currentTimeMillis());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initCookie() {
        File file = new File(DATA_STORAGE_PATH, "cookie.txt");
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = "";
            StringBuffer stringBuffer = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            bufferedReader.close();
            List<HttpCookie> cookies = new Gson().fromJson(stringBuffer.toString(), new TypeToken<List<HttpCookie>>() {
            }.getType());
            if (cookies == null) return;
            for (HttpCookie cookie : cookies) {
                if (cookie.getValue() == null) return;
                if (cookie.getValue().contains("expired")) {
                    cookie.setValue(System.currentTimeMillis() + 60 * 1000 * 60 + "_expired");
                }
                cookieManager.getCookieStore().add(new URI(cookie.getDomain()), cookie);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    //加载联系人映射表
    public void initMember() {
        File file = new File(DATA_STORAGE_PATH + File.separator + "member.txt");
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = "";
            StringBuffer stringBuffer = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            Map<String, String> member = new Gson().fromJson(stringBuffer.toString(), new TypeToken<Map<String, String>>() {
            }.getType());
            bufferedReader.close();
            if (member.size() > 0) {
                core.setMemberMaps(member);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initWeChatQrCode(String qrPath) {
        for (int count = 0; count < 10; count++) {
            sendNotice("获取UUID");
            while (loginService.getUuid() == null) {
                sendNotice("获取微信UUID");
                while (loginService.getUuid() == null) {
                    sendNotice("获取微信UUID失败，两秒后重新获取");
                    SleepUtils.sleep(2000);
                }
            }

           sendNotice("获取登陆二维码图片");
            if (loginService.getQR(qrPath)) {
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
                break;
            }
        }
    }

    public void sendNotice(String notice) {
        Message message = Message.obtain();
        message.obj = notice;
        message.what = 1;
        handler.sendMessage(message);
    }

    public class MainServiceThread implements Runnable {
        private final Core core = Core.getInstance();

        @Override
        public void run() {
//            //监听消息
//            loginService.startReceiving();
            //监听掉线
            while (core.isAlive()) {
                long t1 = System.currentTimeMillis(); // 秒为单位
                if (t1 - core.getLastNormalRetcodeTime() > 10 * 60 * 1000) { // 超过15min，判为离线
                    core.setAlive(false);
                    LOG.info("微信已离线");
                }
                try {
                    Thread.sleep(3 * 60 * 1000); // 休眠180秒
                    loginService.webWxSync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Message message = new Message();
            message.what = 5;
            handler.sendMessage(message);
        }
    }
}