package com.farplace.farpush.itchat.core;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.farplace.farpush.itchat.beans.BaseMsg;
import com.farplace.farpush.itchat.utils.MyHttpClient;
import com.farplace.farpush.itchat.utils.enums.parameters.BaseParaEnum;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

/**
 * 核心存储类，全局只保存一份，单例模式
 *
 * @author https://github.com/yaphone
 * @version 1.0
 * @date 创建时间：2017年4月23日 下午2:33:56
 */
public class Core implements Serializable {

    public static Core instance;

    private Core() {

    }

    public static Core getInstance() {
        if (instance == null) {
            synchronized (Core.class) {
                instance = new Core();
            }
        }
        return instance;
    }

    public void copy(Core core) {
        this.loginInfo = core.loginInfo;
        this.alive = core.alive;
        this.contactList = core.memberList;
        this.groupIdList = core.groupIdList;
        this.groupMemeberMap = core.groupMemeberMap;
        this.groupList = core.groupList;
        this.uuid = core.uuid;
        this.indexUrl = core.indexUrl;
        this.userSelf = core.userSelf;
        this.memberCount = core.memberCount;
        this.userName = core.userName;
        this.lastNormalRetcodeTime = core.lastNormalRetcodeTime;
    }

    boolean alive = false;
    @Expose
    private int memberCount = 0;
    @Expose
    private String indexUrl;
    @Expose
    private String userName;
    @Expose
    private String nickName;

    private List<BaseMsg> msgList = new ArrayList<BaseMsg>();
    @Expose
    private JSONObject userSelf; // 登陆账号自身信息

    private List<JSONObject> memberList = new ArrayList<JSONObject>(); // 好友+群聊+公众号+特殊账号

    private List<JSONObject> contactList = new ArrayList<JSONObject>();// 好友

    private List<JSONObject> groupList = new ArrayList<JSONObject>();

    ; // 群

    private Map<String, JSONArray> groupMemeberMap = new HashMap<String, JSONArray>(); // 群聊成员字典

    private List<JSONObject> publicUsersList = new ArrayList<JSONObject>();

    ;// 公众号／服务号

    private List<JSONObject> specialUsersList = new ArrayList<JSONObject>();
    ;// 特殊账号

    private List<String> groupIdList = new ArrayList<String>(); // 群ID列表

    private List<String> groupNickNameList = new ArrayList<String>(); // 群NickName列表
    @Expose
    private Map<String, JSONObject> userInfoMap = new HashMap<String, JSONObject>();
    @Expose
    public Map<String, Object> loginInfo = new HashMap<String, Object>();
    // CloseableHttpClient httpClient = HttpClients.createDefault();
    MyHttpClient myHttpClient = MyHttpClient.getInstance();
    @Expose
    String uuid = null;

    boolean useHotReload = false;
    //username 和 nickname 映射列表
    private Map<String, String> memberMaps = new HashMap<>();
    String hotReloadDir = "itchat.pkl";
    int receivingRetryCount = 5;

    public Map<String, String> getMemberMaps() {
        return memberMaps;
    }

    public void setMemberMaps(Map<String, String> memberMaps) {
        this.memberMaps = memberMaps;
    }

    private long lastNormalRetcodeTime; // 最后一次收到正常retcode的时间，秒为单位

    public  boolean receive_group;
    public  boolean deliver_msg;

    /**
     * 请求参数
     */
    public Map<String, Object> getParamMap() {
        return new HashMap<String, Object>(1) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                Map<String, String> map = new HashMap<String, String>();
                for (BaseParaEnum baseRequest : BaseParaEnum.values()) {
                    map.put(baseRequest.para(), Objects.toString(getLoginInfo().get(baseRequest.value())));
                }
                put("BaseRequest", map);
            }
        };
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public List<JSONObject> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<JSONObject> memberList) {
        this.memberList = memberList;
    }

    public Map<String, Object> getLoginInfo() {

        return loginInfo;
    }

    public void setLoginInfo(Map<String, Object> loginInfo) {
        this.loginInfo = loginInfo;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public boolean isUseHotReload() {
        return useHotReload;
    }

    public void setUseHotReload(boolean useHotReload) {
        this.useHotReload = useHotReload;
    }

    public String getHotReloadDir() {
        return hotReloadDir;
    }

    public void setHotReloadDir(String hotReloadDir) {
        this.hotReloadDir = hotReloadDir;
    }

    public int getReceivingRetryCount() {
        return receivingRetryCount;
    }

    public void setReceivingRetryCount(int receivingRetryCount) {
        this.receivingRetryCount = receivingRetryCount;
    }

    public MyHttpClient getMyHttpClient() {
        return myHttpClient;
    }

    public List<BaseMsg> getMsgList() {
        return msgList;
    }

    public void setMsgList(List<BaseMsg> msgList) {
        this.msgList = msgList;
    }

    public void setMyHttpClient(MyHttpClient myHttpClient) {
        this.myHttpClient = myHttpClient;
    }

    public List<String> getGroupIdList() {
        return groupIdList;
    }

    public void setGroupIdList(List<String> groupIdList) {
        this.groupIdList = groupIdList;
    }

    public List<JSONObject> getContactList() {
        return contactList;
    }

    public void setContactList(List<JSONObject> contactList) {
        this.contactList = contactList;
    }

    public List<JSONObject> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<JSONObject> groupList) {
        this.groupList = groupList;
    }

    public List<JSONObject> getPublicUsersList() {
        return publicUsersList;
    }

    public void setPublicUsersList(List<JSONObject> publicUsersList) {
        this.publicUsersList = publicUsersList;
    }

    public List<JSONObject> getSpecialUsersList() {
        return specialUsersList;
    }

    public void setSpecialUsersList(List<JSONObject> specialUsersList) {
        this.specialUsersList = specialUsersList;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public JSONObject getUserSelf() {
        return userSelf;
    }

    public void setUserSelf(JSONObject userSelf) {
        this.userSelf = userSelf;
    }

    public Map<String, JSONObject> getUserInfoMap() {
        return userInfoMap;
    }

    public void setUserInfoMap(Map<String, JSONObject> userInfoMap) {
        this.userInfoMap = userInfoMap;
    }

    public synchronized long getLastNormalRetcodeTime() {
        return lastNormalRetcodeTime;
    }

    public synchronized void setLastNormalRetcodeTime(long lastNormalRetcodeTime) {
        this.lastNormalRetcodeTime = lastNormalRetcodeTime;
    }

    public List<String> getGroupNickNameList() {
        return groupNickNameList;
    }

    public void setGroupNickNameList(List<String> groupNickNameList) {
        this.groupNickNameList = groupNickNameList;
    }

    public Map<String, JSONArray> getGroupMemeberMap() {
        return groupMemeberMap;
    }

    public void setGroupMemeberMap(Map<String, JSONArray> groupMemeberMap) {
        this.groupMemeberMap = groupMemeberMap;
    }

    public String getIndexUrl() {
        return indexUrl;
    }

    public void setIndexUrl(String indexUrl) {
        this.indexUrl = indexUrl;
    }

}
