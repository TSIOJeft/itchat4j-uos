package com.farplace.farpush.itchat.utils;

import static com.farplace.farpush.itchat.utils.Config.DATA_STORAGE_PATH;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

/**
 * HTTP访问类，对Apache HttpClient进行简单封装，适配器模式
 *
 * @author https://github.com/yaphone
 * @version 1.0
 * @date 创建时间：2017年4月9日 下午7:05:04
 */
public class MyHttpClient {
    private static CloseableHttpClient httpClient;

    private static MyHttpClient instance = null;

    public static CookieStore cookieStore;
    public static CookieManager cookieManager;

    static {
        cookieManager = new CookieManager();
        cookieStore = cookieManager.getCookieStore();
        //加载cookie
        CookieHandler.setDefault(cookieManager);
    }

    public static String getCookie(String name) {
        List<HttpCookie> cookies = cookieStore.getCookies();
        for (HttpCookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase(name)) {
                return cookie.getValue();
            }
        }
        return null;

    }

    private MyHttpClient() {

    }

    /**
     * 获取cookies
     *
     * @return
     * @author https://github.com/yaphone
     * @date 2017年5月7日 下午8:37:17
     */
    public static MyHttpClient getInstance() {
        if (instance == null) {
            synchronized (MyHttpClient.class) {
                if (instance == null) {
                    instance = new MyHttpClient();
                }
            }
        }
        return instance;
    }

    /**
     * 处理GET请求
     *
     * @param url
     * @param params
     * @return
     * @author https://github.com/yaphone
     * @date 2017年4月9日 下午7:06:19
     */
    public HttpEntity doGet(String url, List<BasicNameValuePair> params, boolean redirect,
                            Map<String, String> headerMap) {
        HttpEntity entity = null;
//        HttpGet httpGet = new HttpGet();
        HttpURLConnection urlConnection = null;
        try {
            if (params != null) {
                String paramStr = EntityUtils.toString(new UrlEncodedFormEntity(params));
                URL url1 = new URL(url + "?" + paramStr);
                urlConnection = (HttpURLConnection) url1.openConnection();
                //                httpGet = new HttpGet(url + "?" + paramStr);
            } else {
                urlConnection = (HttpURLConnection) new URL(url).openConnection();

            }
            urlConnection.setRequestMethod("GET");

            if (!redirect) {
                urlConnection.setInstanceFollowRedirects(false);
            }
            urlConnection.setRequestProperty("User-Agent", Config.USER_AGENT);
//            httpGet.setHeader("User-Agent", Config.USER_AGENT);
            if (headerMap != null) {
                Set<Entry<String, String>> entries = headerMap.entrySet();
                for (Entry<String, String> entry : entries) {
                    urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
//                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }

            String redirect_url = urlConnection.getHeaderField("Location");
            if (redirect_url != null) {
                urlConnection = (HttpURLConnection) new URL(redirect_url).openConnection();
            }
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            StringBuffer stringBuffer = new StringBuffer();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
            }
            entity = new StringEntity(stringBuffer.toString(), "UTF-8");
//
//            CloseableHttpResponse response = httpClient.execute(httpGet);
//            entity = response.getEntity();
        } catch (IOException e) {
//get failed
        }

        return entity;
    }

    public InputStream doGetInputStream(String url, List<BasicNameValuePair> params, boolean redirect,
                                           Map<String, String> headerMap) {
        HttpEntity entity = null;
//        HttpGet httpGet = new HttpGet();
        HttpURLConnection urlConnection = null;
        try {
            if (params != null) {
                String paramStr = EntityUtils.toString(new UrlEncodedFormEntity(params));
                URL url1 = new URL(url + "?" + paramStr);
                urlConnection = (HttpURLConnection) url1.openConnection();
                //                httpGet = new HttpGet(url + "?" + paramStr);
            } else {
                urlConnection = (HttpURLConnection) new URL(url).openConnection();

            }
            urlConnection.setRequestMethod("GET");

            if (!redirect) {
                urlConnection.setInstanceFollowRedirects(false);
            }
            urlConnection.setRequestProperty("User-Agent", Config.USER_AGENT);
//            httpGet.setHeader("User-Agent", Config.USER_AGENT);
            if (headerMap != null) {
                Set<Entry<String, String>> entries = headerMap.entrySet();
                for (Entry<String, String> entry : entries) {
                    urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
//                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }

            String redirect_url = urlConnection.getHeaderField("Location");
            if (redirect_url != null) {
                urlConnection = (HttpURLConnection) new URL(redirect_url).openConnection();
            }

            InputStream inputStream = urlConnection.getInputStream();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return inputStream;
            }
//
//            CloseableHttpResponse response = httpClient.execute(httpGet);
//            entity = response.getEntity();
        } catch (IOException e) {
//get failed
        }

        return null;
    }

    /**
     * 处理POST请求
     *
     * @param url
     * @param paramsStr
     * @return
     * @author https://github.com/yaphone
     * @date 2017年4月9日 下午7:06:35
     */
    public HttpEntity doPost(String url, String paramsStr) {
        HttpEntity entity = null;
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setRequestMethod("POST");
//            httpPost = new HttpPost(url);
//            httpPost.setEntity(params);
            httpURLConnection.setRequestProperty("Content-type", "application/json; charset=utf-8");
            httpURLConnection.setRequestProperty("User-Agent", Config.USER_AGENT);
//            httpPost.setHeader("Content-type", "application/json; charset=utf-8");
//            httpPost.setHeader("User-Agent", Config.USER_AGENT);
            StringEntity params = new StringEntity(paramsStr, Consts.UTF_8.toString());
            OutputStream outputStream = httpURLConnection.getOutputStream();
            params.writeTo(outputStream);
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            StringBuffer stringBuffer = new StringBuffer();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
            }
            entity = new StringEntity(stringBuffer.toString(), "UTF-8");
//            entity = response.getEntity();
        } catch (IOException ignored) {

        }

        return entity;
    }

    /**
     * 上传文件到服务器
     *
     * @param url
     * @param reqEntity
     * @return
     * @author https://github.com/yaphone
     * @date 2017年5月7日 下午9:19:23
     */
    public HttpEntity doPostFile(String url, HttpEntity reqEntity) {
        HttpEntity entity = null;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", Config.USER_AGENT);
        httpPost.setEntity(reqEntity);
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            entity = response.getEntity();

        } catch (Exception e) {

        }
        return entity;
    }

    public static CloseableHttpClient getHttpClient() {
        return httpClient;
    }

}