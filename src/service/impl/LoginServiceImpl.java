package com.farplace.farpush.itchat.service.impl;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static com.farplace.farpush.itchat.utils.Config.DATA_STORAGE_PATH;
import static com.farplace.farpush.itchat.utils.MyHttpClient.cookieManager;
import static com.farplace.farpush.itchat.utils.MyHttpClient.cookieStore;
import static com.farplace.farpush.view.MainActivity.NOTICE_RECEIVER_FILTER;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.*;
import java.net.CookieHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;

import com.farplace.farpush.itchat.beans.BaseMsg;
import com.farplace.farpush.itchat.core.Core;
import com.farplace.farpush.itchat.core.MsgCenter;
import com.farplace.farpush.itchat.service.ILoginService;
import com.farplace.farpush.itchat.utils.Config;
import com.farplace.farpush.itchat.utils.MyHttpClient;
import com.farplace.farpush.itchat.utils.SleepUtils;
import com.farplace.farpush.itchat.utils.enums.ResultEnum;
import com.farplace.farpush.itchat.utils.enums.RetCodeEnum;
import com.farplace.farpush.itchat.utils.enums.StorageLoginInfoEnum;
import com.farplace.farpush.itchat.utils.enums.URLEnum;
import com.farplace.farpush.itchat.utils.enums.parameters.BaseParaEnum;
import com.farplace.farpush.itchat.utils.enums.parameters.LoginParaEnum;
import com.farplace.farpush.itchat.utils.enums.parameters.StatusNotifyParaEnum;
import com.farplace.farpush.itchat.utils.enums.parameters.UUIDParaEnum;
import com.farplace.farpush.itchat.utils.tools.CommonTools;
import com.farplace.farpush.service.FarPushMessageService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


/**
 * 登陆服务实现类
 *
 * @author https://github.com/yaphone
 * @version 1.0
 * @date 创建时间：2017年5月13日 上午12:09:35
 */

/***
 * 修改时间
 * @date 2021年11月15日
 * modify by FarPlace
 */
public class LoginServiceImpl implements ILoginService {
    private static Logger LOG = LoggerFactory.getLogger(LoginServiceImpl.class);
    public Core core = Core.getInstance();
    private MyHttpClient httpClient = core.getMyHttpClient();
    private MyHttpClient myHttpClient = core.getMyHttpClient();
    private Context context;

    public LoginServiceImpl() {

    }

    public LoginServiceImpl(Context context) {
        this.context = context;
    }

    @Override
    public boolean login() {

        boolean isLogin = false;
        // 组装参数和URL
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(LoginParaEnum.LOGIN_ICON.para(), LoginParaEnum.LOGIN_ICON.value()));
        params.add(new BasicNameValuePair(LoginParaEnum.UUID.para(), core.getUuid()));
        params.add(new BasicNameValuePair(LoginParaEnum.TIP.para(), LoginParaEnum.TIP.value()));
        String result = "";
        // long time = 4000;
        while (!isLogin) {
            SleepUtils.sleep(1000);
            long millis = System.currentTimeMillis();
            params.add(new BasicNameValuePair(LoginParaEnum.R.para(), String.valueOf(millis / 1579L)));
            params.add(new BasicNameValuePair(LoginParaEnum.U.para(), String.valueOf(millis)));
            HttpEntity entity = httpClient.doGet(URLEnum.LOGIN_URL.getUrl(), params, true, null);

            try {
                result = EntityUtils.toString(entity);
                String status = checklogin(result);
                if (ResultEnum.SUCCESS.getCode().equals(status)) {
                    isLogin = true;
                    break;
                }
                if (ResultEnum.WAIT_CONFIRM.getCode().equals(status)) {
                    LOG.info("请点击微信确认按钮，进行登陆");
                }

            } catch (Exception e) {
                LOG.error("微信登陆异常！", e);
            }
        }
        processLoginInfo(result); // 处理结果
        core.setAlive(isLogin);
        return isLogin;
    }

    @Override
    public String getUuid() {
        // 组装参数和URL
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(UUIDParaEnum.APP_ID.para(), UUIDParaEnum.APP_ID.value()));
        params.add(new BasicNameValuePair(UUIDParaEnum.FUN.para(), UUIDParaEnum.FUN.value()));
        params.add(new BasicNameValuePair(UUIDParaEnum.LANG.para(), UUIDParaEnum.LANG.value()));
        params.add(new BasicNameValuePair(UUIDParaEnum.REDIRECT_URL.para(), UUIDParaEnum.REDIRECT_URL.value()));
        params.add(new BasicNameValuePair(UUIDParaEnum.U.para(), String.valueOf(System.currentTimeMillis())));

        HttpEntity entity = httpClient.doGet(URLEnum.UUID_URL.getUrl(), params, true, null);

        try {
            String result = EntityUtils.toString(entity);
            String regEx = "window.QRLogin.code = (\\d+); window.QRLogin.uuid = \"(\\S+?)\";";
            Matcher matcher = CommonTools.getMatcher(regEx, result);
            if (matcher.find()) {
                if ((ResultEnum.SUCCESS.getCode().equals(matcher.group(1)))) {
                    core.setUuid(matcher.group(2));
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return core.getUuid();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean getQR(String qrPath) {
//		String qrUrl = URLEnum.QRCODE_URL.getUrl() + core.getUuid();
        String qrUrl = "https://login.weixin.qq.com/l/" + core.getUuid();
        System.out.println(core.getUuid());
        try {
            File file = new File(qrPath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                if (!file.createNewFile()) {
                    return false;
                }
            }
            OutputStream outputStream = new FileOutputStream(file);
            Bitmap bitmap = textToImage(qrUrl, 200, 200);
            if (bitmap == null) {
                return false;
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Bitmap textToImage(String text, int width, int height) throws WriterException, NullPointerException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.DATA_MATRIX.QR_CODE,
                    width, height, null);
        } catch (IllegalArgumentException Illegalargumentexception) {
            return null;
        }

        int bitMatrixWidth = bitMatrix.getWidth();
        int bitMatrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        int colorWhite = 0xFFFFFFFF;
        int colorBlack = 0xFF000000;

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;
            for (int x = 0; x < bitMatrixWidth; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? colorBlack : colorWhite;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, width, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    @Override
    public boolean webWxInit() {
        core.setAlive(true);
        core.setLastNormalRetcodeTime(System.currentTimeMillis());
        // 组装请求URL和参数
        String url = String.format(URLEnum.INIT_URL.getUrl(),
                core.getLoginInfo().get(StorageLoginInfoEnum.url.getKey()),
                String.valueOf(System.currentTimeMillis() / 3158L),
                core.getLoginInfo().get(StorageLoginInfoEnum.pass_ticket.getKey()));

        Map<String, Object> paramMap = core.getParamMap();

        // 请求初始化接口
        HttpEntity entity = httpClient.doPost(url, JSON.toJSONString(paramMap));
        try {
            String result = EntityUtils.toString(entity, Consts.UTF_8.toString());
            JSONObject obj = JSON.parseObject(result);
            JSONObject user = obj.getJSONObject(StorageLoginInfoEnum.User.getKey());
            JSONObject syncKey = obj.getJSONObject(StorageLoginInfoEnum.SyncKey.getKey());

            core.getLoginInfo().put(StorageLoginInfoEnum.InviteStartCount.getKey(),
                    obj.getInteger(StorageLoginInfoEnum.InviteStartCount.getKey()));
            core.getLoginInfo().put(StorageLoginInfoEnum.SyncKey.getKey(), syncKey);

            JSONArray syncArray = syncKey.getJSONArray("List");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < syncArray.size(); i++) {
                sb.append(syncArray.getJSONObject(i).getString("Key") + "_"
                        + syncArray.getJSONObject(i).getString("Val") + "|");
            }
            // 1_661706053|2_661706420|3_661706415|1000_1494151022|
            String synckey = sb.toString();

            // 1_661706053|2_661706420|3_661706415|1000_1494151022
            core.getLoginInfo().put(StorageLoginInfoEnum.synckey.getKey(), synckey.substring(0, synckey.length() - 1));// 1_656161336|2_656161626|3_656161313|11_656159955|13_656120033|201_1492273724|1000_1492265953|1001_1492250432|1004_1491805192
            core.setUserName(user.getString("UserName"));
            core.setNickName(user.getString("NickName"));
            core.setUserSelf(obj.getJSONObject("User"));

            String chatSet = obj.getString("ChatSet");
            String[] chatSetArray = chatSet.split(",");
            for (int i = 0; i < chatSetArray.length; i++) {
                if (chatSetArray[i].indexOf("@@") != -1) {
                    // 更新GroupIdList
                    core.getGroupIdList().add(chatSetArray[i]); //
                }
            }
            // JSONArray contactListArray = obj.getJSONArray("ContactList");
            // for (int i = 0; i < contactListArray.size(); i++) {
            // JSONObject o = contactListArray.getJSONObject(i);
            // if (o.getString("UserName").indexOf("@@") != -1) {
            // core.getGroupIdList().add(o.getString("UserName")); //
            // // 更新GroupIdList
            // core.getGroupList().add(o); // 更新GroupList
            // core.getGroupNickNameList().add(o.getString("NickName"));
            // }
            // }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void wxStatusNotify() {
        // 组装请求URL和参数
        String url = String.format(URLEnum.STATUS_NOTIFY_URL.getUrl(),
                core.getLoginInfo().get(StorageLoginInfoEnum.pass_ticket.getKey()));

        Map<String, Object> paramMap = core.getParamMap();
        paramMap.put(StatusNotifyParaEnum.CODE.para(), StatusNotifyParaEnum.CODE.value());
        paramMap.put(StatusNotifyParaEnum.FROM_USERNAME.para(), core.getUserName());
        paramMap.put(StatusNotifyParaEnum.TO_USERNAME.para(), core.getUserName());
        paramMap.put(StatusNotifyParaEnum.CLIENT_MSG_ID.para(), System.currentTimeMillis());
        String paramStr = JSON.toJSONString(paramMap);

        try {
            HttpEntity entity = httpClient.doPost(url, paramStr);
            EntityUtils.toString(entity, Consts.UTF_8.toString());
        } catch (Exception e) {
            LOG.error("微信状态通知接口失败！", e);
        }

    }

    @Override
    public void startReceiving() {
        new Thread(new Runnable() {
            int retryCount = 0;

            @Override
            public void run() {
                while (core.isAlive()) {
                    try {
                        Map<String, String> resultMap = syncCheck();
                        String retcode = resultMap.get("retcode");
                        String selector = resultMap.get("selector");
                        if (retcode.equals(RetCodeEnum.UNKOWN.getCode())) {
                            LOG.info(RetCodeEnum.UNKOWN.getType());
                            continue;
                        } else if (retcode.equals(RetCodeEnum.LOGIN_OUT.getCode())) { // 退出
                            LOG.info(RetCodeEnum.LOGIN_OUT.getType());
                            core.setAlive(false);
                            //清楚cookie需要重新登陆
                            new File(DATA_STORAGE_PATH + File.separator + "cookie.txt").delete();
                            new File(DATA_STORAGE_PATH + File.separator + "info.txt").delete();
                            break;
                        } else if (retcode.equals(RetCodeEnum.LOGIN_OTHERWHERE.getCode())) { // 其它地方登陆
                            LOG.info(RetCodeEnum.LOGIN_OTHERWHERE.getType());
                            break;
                        } else if (retcode.equals(RetCodeEnum.MOBILE_LOGIN_OUT.getCode())) { // 移动端退出
                            LOG.info(RetCodeEnum.MOBILE_LOGIN_OUT.getType());
                            break;
                        } else if (retcode.equals(RetCodeEnum.NORMAL.getCode())) {
                            core.setLastNormalRetcodeTime(System.currentTimeMillis()); // 最后收到正常报文时间
//                            JSONObject msgObj = webWxSync();
                            if (selector.equals("2")) {
                                JSONObject msgObj = webWxSync();
                                if (msgObj != null) {
                                    try {
                                        JSONArray msgList = new JSONArray();
                                        msgList = msgObj.getJSONArray("AddMsgList");
                                        msgList = MsgCenter.produceMsg(msgList);
                                        for (int j = 0; j < msgList.size(); j++) {
                                            BaseMsg baseMsg = JSON.toJavaObject(msgList.getJSONObject(j),
                                                    BaseMsg.class);
                                            core.getMsgList().add(baseMsg);
                                        }
                                    } catch (Exception e) {
                                        LOG.info(e.getMessage());
                                    }
                                }
                                continue;
                            } else if (selector.equals("7")) {
                                //进入/退出新的聊天界面
                                webWxSync();
                            } else if (selector.equals("4")) {
                                continue;
                            } else if (selector.equals("3")) {
                                continue;
                            } else if (selector.equals("6")) {
//                                if (msgObj != null) {
//                                    try {
//                                        JSONArray msgList = new JSONArray();
//                                        msgList = msgObj.getJSONArray("AddMsgList");
//                                        JSONArray modContactList = msgObj.getJSONArray("ModContactList"); // 存在删除或者新增的好友信息
//                                        msgList = MsgCenter.produceMsg(msgList);
//                                        for (int j = 0; j < msgList.size(); j++) {
//                                            JSONObject userInfo = modContactList.getJSONObject(j);
//                                            // 存在主动加好友之后的同步联系人到本地
//                                            core.getContactList().add(userInfo);
//                                        }
//                                    } catch (Exception e) {
//                                        LOG.info(e.getMessage());
//                                    }
//                                }
                                continue;
                            } else {
                                //无消息但正常报文收到
                                Thread.sleep(Config.SYNC_HZ * 1000L);
                            }
                        } else {
                            //收不到正常报文 刷新数据
                            JSONObject obj = webWxSync();
                            //休眠10s再拉取
                            Thread.sleep(10 * 1000L);
                        }
                    } catch (Exception e) {
                        LOG.info(e.getMessage());
                        //无法获取返回值 可能掉线了
                        retryCount += 1;
                        try {
                            //休眠60s
                            Thread.sleep(60 * 1000L);
                        } catch (Exception e1) {
                            LOG.info(e.getMessage());
                        }
//                        if (core.getReceivingRetryCount() < retryCount) {
//                            core.setAlive(false);
//                        } else {
//
//                        }
                    }

                }

            }

        }).start();

    }

    @Override
    public void webWxGetContact() {
        String url = String.format(URLEnum.WEB_WX_GET_CONTACT.getUrl(),
                core.getLoginInfo().get(StorageLoginInfoEnum.url.getKey()));
        Map<String, Object> paramMap = core.getParamMap();
        HttpEntity entity = httpClient.doPost(url, JSON.toJSONString(paramMap));

        try {
            String result = EntityUtils.toString(entity, Consts.UTF_8.toString());
            JSONObject fullFriendsJsonList = JSON.parseObject(result);
            // 查看seq是否为0，0表示好友列表已全部获取完毕，若大于0，则表示好友列表未获取完毕，当前的字节数（断点续传）
            long seq = 0;
            long currentTime = 0L;
            List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
            if (fullFriendsJsonList.get("Seq") != null) {
                seq = fullFriendsJsonList.getLong("Seq");
                currentTime = new Date().getTime();
            }
            core.setMemberCount(fullFriendsJsonList.getInteger(StorageLoginInfoEnum.MemberCount.getKey()));
            JSONArray member = fullFriendsJsonList.getJSONArray(StorageLoginInfoEnum.MemberList.getKey());
            // 循环获取seq直到为0，即获取全部好友列表 ==0：好友获取完毕 >0：好友未获取完毕，此时seq为已获取的字节数
            while (seq > 0) {
                // 设置seq传参
                params.add(new BasicNameValuePair("r", String.valueOf(currentTime)));
                params.add(new BasicNameValuePair("seq", String.valueOf(seq)));
                entity = httpClient.doGet(url, params, false, null);

                params.remove(new BasicNameValuePair("r", String.valueOf(currentTime)));
                params.remove(new BasicNameValuePair("seq", String.valueOf(seq)));

                result = EntityUtils.toString(entity, Consts.UTF_8.toString());
                fullFriendsJsonList = JSON.parseObject(result);

                if (fullFriendsJsonList.get("Seq") != null) {
                    seq = fullFriendsJsonList.getLong("Seq");
                    currentTime = new Date().getTime();
                }

                // 累加好友列表
                member.addAll(fullFriendsJsonList.getJSONArray(StorageLoginInfoEnum.MemberList.getKey()));
            }
            core.setMemberCount(member.size());
            Map<String, String> members = new HashMap<>();
            for (Iterator<?> iterator = member.iterator(); iterator.hasNext(); ) {
                JSONObject o = (JSONObject) iterator.next();
                members.put(o.getString("UserName"), o.getString("NickName"));
                if ((o.getInteger("VerifyFlag") & 8) != 0) { // 公众号/服务号
                    core.getPublicUsersList().add(o);
                } else if (Config.API_SPECIAL_USER.contains(o.getString("UserName"))) { // 特殊账号
                    core.getSpecialUsersList().add(o);
                } else if (o.getString("UserName").indexOf("@@") != -1) { // 群聊
                    if (!core.getGroupIdList().contains(o.getString("UserName"))) {
                        core.getGroupNickNameList().add(o.getString("NickName"));
                        core.getGroupIdList().add(o.getString("UserName"));
                        core.getGroupList().add(o);
                    }
                } else if (o.getString("UserName").equals(core.getUserSelf().getString("UserName"))) { // 自己
                    core.getContactList().remove(o);
                } else { // 普通联系人
                    core.getContactList().add(o);
                }
            }
            //存储映射表
            core.setMemberMaps(members);
            storeMember();
            return;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return;
    }

    @Override
    public void WebWxBatchGetContact() {
        String url = String.format(URLEnum.WEB_WX_BATCH_GET_CONTACT.getUrl(),
                core.getLoginInfo().get(StorageLoginInfoEnum.url.getKey()), new Date().getTime(),
                core.getLoginInfo().get(StorageLoginInfoEnum.pass_ticket.getKey()));
        Map<String, Object> paramMap = core.getParamMap();
        paramMap.put("Count", core.getGroupIdList().size());
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (int i = 0; i < core.getGroupIdList().size(); i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("UserName", core.getGroupIdList().get(i));
            map.put("EncryChatRoomId", "");
            list.add(map);
        }
        paramMap.put("List", list);
        HttpEntity entity = httpClient.doPost(url, JSON.toJSONString(paramMap));
        try {
            String text = EntityUtils.toString(entity, Consts.UTF_8.toString());
            JSONObject obj = JSON.parseObject(text);
            JSONArray contactList = obj.getJSONArray("ContactList");
            for (int i = 0; i < contactList.size(); i++) { // 群好友
                if (contactList.getJSONObject(i).getString("UserName").indexOf("@@") > -1) { // 群
                    core.getGroupNickNameList().add(contactList.getJSONObject(i).getString("NickName")); // 更新群昵称列表
                    core.getGroupList().add(contactList.getJSONObject(i)); // 更新群信息（所有）列表
                    core.getGroupMemeberMap().put(contactList.getJSONObject(i).getString("UserName"),
                            contactList.getJSONObject(i).getJSONArray("MemberList")); // 更新群成员Map
                }
            }
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }
    }

    /**
     * 检查登陆状态
     *
     * @param result
     * @return
     */
    public String checklogin(String result) {
        String regEx = "window.code=(\\d+)";
        Matcher matcher = CommonTools.getMatcher(regEx, result);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 处理登陆信息
     *
     * @author https://github.com/yaphone
     * @date 2017年4月9日 下午12:16:26
     */
    void processLoginInfo(String loginContent) {
        String regEx = "window.redirect_uri=\"(\\S+)\";";
        Matcher matcher = CommonTools.getMatcher(regEx, loginContent);
        if (matcher.find()) {
            String originalUrl = matcher.group(1);
            String url = originalUrl.substring(0, originalUrl.lastIndexOf('/')); // https://wx2.qq.com/cgi-bin/mmwebwx-bin
            core.getLoginInfo().put("url", url);
            Map<String, List<String>> possibleUrlMap = this.getPossibleUrlMap();
            Iterator<Entry<String, List<String>>> iterator = possibleUrlMap.entrySet().iterator();
            Map.Entry<String, List<String>> entry;
            String fileUrl;
            String syncUrl;
            while (iterator.hasNext()) {
                entry = iterator.next();
                String indexUrl = entry.getKey();
                fileUrl = "https://" + entry.getValue().get(0) + "/cgi-bin/mmwebwx-bin";
                syncUrl = "https://" + entry.getValue().get(1) + "/cgi-bin/mmwebwx-bin";
                if (core.getLoginInfo().get("url").toString().contains(indexUrl)) {
                    core.setIndexUrl(indexUrl);
                    core.getLoginInfo().put("fileUrl", fileUrl);
                    core.getLoginInfo().put("syncUrl", syncUrl);
                    break;
                }
            }
            if (core.getLoginInfo().get("fileUrl") == null && core.getLoginInfo().get("syncUrl") == null) {
                core.getLoginInfo().put("fileUrl", url);
                core.getLoginInfo().put("syncUrl", url);
            }
            core.getLoginInfo().put("deviceid", "e" + String.valueOf(new Random().nextLong()).substring(1, 16)); // 生成15位随机数
            core.getLoginInfo().put("BaseRequest", new ArrayList<String>());
            String text = "";
            Map<String, String> header = new HashMap<>();
            header.put("client-version", "2.0.0");
            header.put("extspam", "Gp8ICJkIEpkICggwMDAwMDAwMRAGGoAI1GiJSIpeO1RZTq9QBKsRbPJ" +
                    "di84ropi16EYI10WB6g74sGmRwSNXjPQnYUKYotKkvLGpshucCaeWZMOylnc6o2AgDX9grhQ" +
                    "Qx7fm2DJRTyuNhUlwmEoWhjoG3F0ySAWUsEbH3bJMsEBwoB//0qmFJob74ffdaslqL+IrSy7LJ76" +
                    "/G5TkvNC+J0VQkpH1u3iJJs0uUYyLDzdBIQ6Ogd8LDQ3VKnJLm4g/uDLe+G7zzzkOPzCjXL+70naaQ9" +
                    "medzqmh+/SmaQ6uFWLDQLcRln++wBwoEibNpG4uOJvqXy+ql50DjlNchSuqLmeadFoo9/mDT0q3G7" +
                    "o/80P15ostktjb7h9bfNc+nZVSnUEJXbCjTeqS5UYuxn+HTS5nZsPVxJA2O5GdKCYK4x8lTTKShRstqPfbQp" +
                    "plfllx2fwXcSljuYi3YipPyS3GCAqf5A7aYYwJ7AvGqUiR2SsVQ9Nbp8MGHET1GxhifC692APj6SJxZD3i1drSYZP" +
                    "MMsS9rKAJTGz2FEupohtpf2tgXm6c16nDk/cw+C7K7me5j5PLHv55DFCS84b06AytZPdkFZLj7FHOkcFGJXit" +
                    "HkX5cgww7vuf6F3p0yM/W73SoXTx6GX4G6Hg2rYx3O/9VU2Uq8lvURB4qIbD9XQpzmyiFMaytMnqxcZJc" +
                    "oXCtfkTJ6pI7a92JpRUvdSitg967VUDUAQnCXCM/m0snRkR9LtoXAO1FUGpwlp1EfIdCZFPKNnXMeqev0j9" +
                    "W9ZrkEs9ZWcUEexSj5z+dKYQBhIICviYUQHVqBTZSNy22PlUIeDeIs11j7q4t8rD8LPvzAKWVqXE+5lS1JPZkjg" +
                    "4y5hfX1Dod3t96clFfwsvDP6xBSe1NBcoKbkyGxYK0UvPGtKQEE0Se2zAymYDv41klYE9s+rxp8e94/H8XhrL" +
                    "9oGm8KWb2RmYnAE7ry9gd6e8ZuBRIsISlJAE/e8y8xFmP031S6Lnaet6YXPsFpuFsdQs535IjcFd75hh6DNMBY" +
                    "hSfjv456cvhsb99+fRw/KVZLC3yzNSCbLSyo9d9BI45Plma6V8akURQA/qsaAzU0VyTIqZJkPDTzhuCl92vD2A" +
                    "D/QOhx6iwRSVPAxcRFZcWjgc2wCKh+uCYkTVbNQpB9B90YlNmI3fWTuUOUjwOzQRxJZj11NsimjOJ50qQwT" +
                    "TFj6qQvQ1a/I+MkTx5UO+yNHl718JWcR3AXGmv/aa9rD1eNP8ioTGlOZwPgmr2sor2iBpKTOrB83QgZXP+xRYkb4zVC+LoAX" +
                    "EoIa1+zArywlgREer7DLePukkU6wHTkuSaF+ge5Of1bXuU4i938WJHj0t3D8uQxkJvoFi/EYN/7u2P1zGRLV4dHVUsZMGCCtnO6BBigFMAA=");
            header.put("referer", "https://wx.qq.com/?&lang=zh_CN&target=t");
            originalUrl = originalUrl + "&fun=new&version=v2";
            try {
                HttpEntity entity = myHttpClient.doGet(originalUrl, null, false, header);
                text = EntityUtils.toString(entity);
            } catch (Exception e) {
                LOG.info(e.getMessage());
                return;
            }

            //add by 默非默 2017-08-01 22:28:09
            //如果登录被禁止时，则登录返回的message内容不为空，下面代码则判断登录内容是否为空，不为空则退出程序
            String msg = getLoginMessage(text);
            if (!"".equals(msg)) {
                LOG.info(msg);
//				System.exit(0);
            }
            Document doc = CommonTools.xmlParser(text);

            if (doc != null) {
                core.getLoginInfo().put(StorageLoginInfoEnum.skey.getKey(),
                        doc.getElementsByTagName(StorageLoginInfoEnum.skey.getKey()).item(0).getFirstChild()
                                .getNodeValue());
                core.getLoginInfo().put(StorageLoginInfoEnum.wxsid.getKey(),
                        doc.getElementsByTagName(StorageLoginInfoEnum.wxsid.getKey()).item(0).getFirstChild()
                                .getNodeValue());
                core.getLoginInfo().put(StorageLoginInfoEnum.wxuin.getKey(),
                        doc.getElementsByTagName(StorageLoginInfoEnum.wxuin.getKey()).item(0).getFirstChild()
                                .getNodeValue());
                core.getLoginInfo().put(StorageLoginInfoEnum.pass_ticket.getKey(),
                        doc.getElementsByTagName(StorageLoginInfoEnum.pass_ticket.getKey()).item(0).getFirstChild()
                                .getNodeValue());
            }

        }
    }

    private Map<String, List<String>> getPossibleUrlMap() {
        Map<String, List<String>> possibleUrlMap = new HashMap<String, List<String>>();
        possibleUrlMap.put("wx.qq.com", new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                add("file.wx.qq.com");
                add("webpush.wx.qq.com");
            }
        });

        possibleUrlMap.put("wx2.qq.com", new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                add("file.wx2.qq.com");
                add("webpush.wx2.qq.com");
            }
        });
        possibleUrlMap.put("wx8.qq.com", new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                add("file.wx8.qq.com");
                add("webpush.wx8.qq.com");
            }
        });

        possibleUrlMap.put("web2.wechat.com", new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                add("file.web2.wechat.com");
                add("webpush.web2.wechat.com");
            }
        });
        possibleUrlMap.put("wechat.com", new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                add("file.web.wechat.com");
                add("webpush.web.wechat.com");
            }
        });
        return possibleUrlMap;
    }

    /**
     * 同步消息 sync the messages
     *
     * @return
     * @author https://github.com/yaphone
     * @date 2017年5月12日 上午12:24:55
     */
    public JSONObject webWxSync() {
        JSONObject result = null;
        String url = String.format(URLEnum.WEB_WX_SYNC_URL.getUrl(),
                core.getLoginInfo().get(StorageLoginInfoEnum.url.getKey()),
                core.getLoginInfo().get(StorageLoginInfoEnum.wxsid.getKey()),
                core.getLoginInfo().get(StorageLoginInfoEnum.skey.getKey()),
                core.getLoginInfo().get(StorageLoginInfoEnum.pass_ticket.getKey()));
        Map<String, Object> paramMap = core.getParamMap();
        paramMap.put(StorageLoginInfoEnum.SyncKey.getKey(),
                core.getLoginInfo().get(StorageLoginInfoEnum.SyncKey.getKey()));
        paramMap.put("rr", -new Date().getTime() / 1000);
        String paramStr = JSON.toJSONString(paramMap);
        try {
            HttpEntity entity = myHttpClient.doPost(url, paramStr);
            String text = EntityUtils.toString(entity, Consts.UTF_8.toString());
            JSONObject obj = JSON.parseObject(text);

            if (obj.getJSONObject("BaseResponse").getInteger("Ret") != 0) {
                result = null;
            } else {
                result = obj;
                core.getLoginInfo().put(StorageLoginInfoEnum.SyncKey.getKey(), obj.getJSONObject("SyncCheckKey"));
                JSONArray syncArray = obj.getJSONObject(StorageLoginInfoEnum.SyncKey.getKey()).getJSONArray("List");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < syncArray.size(); i++) {
                    sb.append(syncArray.getJSONObject(i).getString("Key") + "_"
                            + syncArray.getJSONObject(i).getString("Val") + "|");
                }
                String synckey = sb.toString();
                core.getLoginInfo().put(StorageLoginInfoEnum.synckey.getKey(),
                        synckey.substring(0, synckey.length() - 1));// 1_656161336|2_656161626|3_656161313|11_656159955|13_656120033|201_1492273724|1000_1492265953|1001_1492250432|1004_1491805192
                //保存
                storeCoreInfo();
                storeCoreCookie();
            }
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }
        return result;

    }

    /**
     * 检查是否有新消息 check whether there's a message
     *
     * @return
     * @author https://github.com/yaphone
     * @date 2017年4月16日 上午11:11:34
     */
    private Map<String, String> syncCheck() {
        Map<String, String> resultMap = new HashMap<String, String>();
        // 组装请求URL和参数
        String url = core.getLoginInfo().get(StorageLoginInfoEnum.syncUrl.getKey()) + URLEnum.SYNC_CHECK_URL.getUrl();
        List<BasicNameValuePair> params = new ArrayList<>();
        for (BaseParaEnum baseRequest : BaseParaEnum.values()) {
            params.add(new BasicNameValuePair(baseRequest.para().toLowerCase(),
                    core.getLoginInfo().get(baseRequest.value()).toString()));
        }
        params.add(new BasicNameValuePair("r", String.valueOf(new Date().getTime())));
        params.add(new BasicNameValuePair("synckey", (String) core.getLoginInfo().get("synckey")));
        params.add(new BasicNameValuePair("_", String.valueOf(new Date().getTime())));
        //可传递 心跳数据
        if (context != null) {
            Intent intent = new Intent(NOTICE_RECEIVER_FILTER);
            intent.putExtra("CODE", 4);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        try {
            HttpEntity entity = myHttpClient.doGet(url, params, true, null);
            if (entity == null) {
                resultMap.put("retcode", "9999");
                resultMap.put("selector", "9999");
                return resultMap;
            }
            String text = EntityUtils.toString(entity);
            String regEx = "window.synccheck=\\{retcode:\"(\\d+)\",selector:\"(\\d+)\"\\}";
            Matcher matcher = CommonTools.getMatcher(regEx, text);
            if (!matcher.find() || matcher.group(1).equals("2")) {
                LOG.info(String.format("Unexpected sync check result: %s", text));
            } else {
                resultMap.put("retcode", matcher.group(1));
                resultMap.put("selector", matcher.group(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * 解析登录返回的消息，如果成功登录，则message为空
     *
     * @param result
     * @return
     */
    public String getLoginMessage(String result) {
        String[] strArr = result.split("<message>");
        String[] rs = strArr[1].split("</message>");
        if (rs != null && rs.length > 1) {
            return rs[0];
        }
        return "";
    }

    public void storeCoreInfo() {
        try {
            Gson gson = new Gson().newBuilder().excludeFieldsWithoutExposeAnnotation().create();
            FileOutputStream fileOutputStream = new FileOutputStream(DATA_STORAGE_PATH + File.separator + "info.txt");
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(gson.toJson(core).getBytes());
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void storeCoreCookie() {
        File file = new File(DATA_STORAGE_PATH + File.separator + "cookie.txt");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(new Gson().toJson(cookieManager.getCookieStore().getCookies()).getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void storeMember() {
        File file = new File(DATA_STORAGE_PATH + File.separator + "member.txt");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(new Gson().toJson(core.getMemberMaps()).getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
