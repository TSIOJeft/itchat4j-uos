package com.farplace.farpush.itchat.utils.tools;

import android.util.Log;

import com.alibaba.fastjson.util.IOUtils;
import com.farplace.farpush.itchat.beans.BaseMsg;
import com.farplace.farpush.itchat.core.Core;
import com.farplace.farpush.itchat.utils.MyHttpClient;
import com.farplace.farpush.itchat.utils.enums.MsgTypeEnum;
import com.farplace.farpush.itchat.utils.enums.URLEnum;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * 下载工具类
 *
 * @author https://github.com/yaphone
 * @version 1.0
 * @date 创建时间：2017年4月21日 下午11:18:46
 */
public class DownloadTools {
    private static Logger logger = Logger.getLogger("DownloadTools");
    private static Core core = Core.getInstance();
    private static MyHttpClient myHttpClient = core.getMyHttpClient();

    /**
     * 处理下载任务
     *
     * @param path
     * @return
     * @author https://github.com/yaphone
     * @date 2017年4月21日 下午11:00:25
     */
    public static boolean getDownloadFn(BaseMsg msg, String type, String path) {
        Map<String, String> headerMap = new HashMap<String, String>();
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        String url = "";
        if (type.equals(MsgTypeEnum.PIC.getType())) {
            url = String.format(URLEnum.WEB_WX_GET_MSG_IMG.getUrl(), (String) core.getLoginInfo().get("url"));
        } else if (type.equals(MsgTypeEnum.VOICE.getType())) {
            url = String.format(URLEnum.WEB_WX_GET_VOICE.getUrl(), (String) core.getLoginInfo().get("url"));
        } else if (type.equals(MsgTypeEnum.VIEDO.getType())) {
            headerMap.put("Range", "bytes=0-");
            url = String.format(URLEnum.WEB_WX_GET_VIEDO.getUrl(), (String) core.getLoginInfo().get("url"));
        } else if (type.equals(MsgTypeEnum.MEDIA.getType())) {
            headerMap.put("Range", "bytes=0-");
            url = String.format(URLEnum.WEB_WX_GET_MEDIA.getUrl(), (String) core.getLoginInfo().get("fileUrl"));
            params.add(new BasicNameValuePair("sender", msg.getFromUserName()));
            params.add(new BasicNameValuePair("mediaid", msg.getMediaId()));
            params.add(new BasicNameValuePair("filename", msg.getFileName()));
        }
        params.add(new BasicNameValuePair("msgid", msg.getNewMsgId()));
        params.add(new BasicNameValuePair("skey", (String) core.getLoginInfo().get("skey")));
        InputStream inputStream = myHttpClient.doGetInputStream(url, params, true, headerMap);
//        HttpEntity entity = myHttpClient.doGet(url, params, true, headerMap);
        try {
            OutputStream out = new FileOutputStream(path);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int nread;
            byte[] read = new byte[1024];
            while ((nread = inputStream.read(read, 0, read.length)) != -1) {
                byteArrayOutputStream.write(read, 0, nread);
            }
            byte[] bytes = byteArrayOutputStream.toByteArray();
//            byte[] bytes = EntityUtils.toByteArray(entity);
//            byte[] bytes = new byte[1024];
//            int len;
//            while ((len=inputStream.read(bytes))!=-1) {
//                out.write(bytes, 0, len);
//            }
            out.write(bytes);
            out.flush();
            out.close();
            msg.setFilePath(path);
            // Tools.printQr(path);
            return true;
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return false;
    }

    ;

    public static byte[] getDownloadFnBytes(BaseMsg msg, String type) {
        Map<String, String> headerMap = new HashMap<String, String>();
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        String url = "";
        if (type.equals(MsgTypeEnum.PIC.getType())) {
            url = String.format(URLEnum.WEB_WX_GET_MSG_IMG.getUrl(), (String) core.getLoginInfo().get("url"));
        } else if (type.equals(MsgTypeEnum.VOICE.getType())) {
            url = String.format(URLEnum.WEB_WX_GET_VOICE.getUrl(), (String) core.getLoginInfo().get("url"));
        } else if (type.equals(MsgTypeEnum.VIEDO.getType())) {
            headerMap.put("Range", "bytes=0-");
            url = String.format(URLEnum.WEB_WX_GET_VIEDO.getUrl(), (String) core.getLoginInfo().get("url"));
        } else if (type.equals(MsgTypeEnum.MEDIA.getType())) {
            headerMap.put("Range", "bytes=0-");
            url = String.format(URLEnum.WEB_WX_GET_MEDIA.getUrl(), (String) core.getLoginInfo().get("fileUrl"));
            params.add(new BasicNameValuePair("sender", msg.getFromUserName()));
            params.add(new BasicNameValuePair("mediaid", msg.getMediaId()));
            params.add(new BasicNameValuePair("filename", msg.getFileName()));
        }
        params.add(new BasicNameValuePair("msgid", msg.getNewMsgId()));
        params.add(new BasicNameValuePair("skey", (String) core.getLoginInfo().get("skey")));
        HttpEntity entity = myHttpClient.doGet(url, params, true, headerMap);
        try {
            byte[] bytes = EntityUtils.toByteArray(entity);
            return bytes;
//            out.write(bytes);
//            out.flush();
//            out.close();
            // Tools.printQr(path);

        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return null;
    }

}
