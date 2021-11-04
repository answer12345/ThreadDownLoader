package com.sangjie.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/*
http相关工具类
 */
public class HttpUtils {

    public static long getHttpContentLength(String url) throws IOException {
        HttpURLConnection httpURLConnection = null;
        int contentLength;
        try {
            httpURLConnection = getHttpConnection(url);
            contentLength = httpURLConnection.getContentLength();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return contentLength;

    }

    /**
     * 分块下载
     * @param url
     * @param startPos 下载起始位置
     * @param endPos 下载结束位置
     * @return
     */
    public static HttpURLConnection getHttpConnection(String url, long startPos, long endPos) throws IOException {
        HttpURLConnection httpURLConnection = getHttpConnection(url);
        LogUtils.info("下载的区间是：{} - {}", startPos, endPos);
        if (endPos != 0) {
            httpURLConnection.setRequestProperty("RANGE", "bytes=" + startPos + "-" + endPos);
        } else {
            httpURLConnection.setRequestProperty("RANGE", "bytes=" + startPos + "-");
        }

        return httpURLConnection;
    }

    /**
     * 获得HttpConnection连接对象
     * @param url 文件地址
     * @return
     */
    public static HttpURLConnection getHttpConnection(String url) throws IOException {
        URL httpUrl = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
        //像文件所在的服务器发送标识信息
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.163 Safari/535.1");

        return httpURLConnection;
    }

    /**
     * 获取下载文件名
     * @param url
     * @return
     */
    public static String getHttpFileName(String url) {
        int index = url.lastIndexOf("/");
        return url.substring(index + 1);
    }
}
