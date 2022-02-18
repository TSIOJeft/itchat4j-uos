package com.farplace.farpush.utils;


import androidx.annotation.BinderThread;
import androidx.annotation.WorkerThread;
import androidx.work.impl.utils.SerialExecutor;

import com.farplace.farpush.itchat.beans.BaseMsg;
import com.farplace.farpush.itchat.utils.Config;
import com.farplace.farpush.itchat.utils.enums.MsgTypeEnum;
import com.farplace.farpush.itchat.utils.tools.DownloadTools;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MsgDownloader {
    public static final SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    /**
     * 下载
     *
     * @param msg      消息
     * @param listener 监听器，请勿更新UI
     */
    public static void download(BaseMsg msg, @WorkerThread MsgDownloadListener listener) {
        MsgTypeEnum type;
        try {
            type = MsgTypeEnum.fromType(msg.getType());
        } catch (Exception exception) {
            // 消息类型错误
            exception.printStackTrace();

            if (listener != null) {
                listener.download(null, msg, null, false);
            }
            return;
        }

        EXECUTOR.execute(() -> {
            String suffix = "farpush";
            switch (type) {
                case PIC:
                    suffix = "jpg";
                    break;
                case VOICE:
                    suffix = "mp3";
                    break;
            }
            String path = getDateStoragePath(suffix);
            if (DownloadTools.getDownloadFn(msg, msg.getType(), path)) {
                if (listener != null) {
                    listener.download(type, msg, path, true);
                }
            } else {
                if (listener != null) {
                    listener.download(type, msg, path, false);
                }
            }
        });
    }


    public static String getStoragePath(String name, String suffix) {
        return Config.DATA_STORAGE_PATH + File.separator + name + "." + suffix;
    }

    public static String getDateStoragePath(String suffix) {
        return getStoragePath(DATA_FORMAT.format(new Date()), suffix);
    }

    /**
     * 下载结果，请勿更新UI
     */
    @WorkerThread
    @FunctionalInterface
    public interface MsgDownloadListener {
        void download(MsgTypeEnum type, BaseMsg msg, String path, boolean result);
    }
}
