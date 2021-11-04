package com.sangjie.core;

import com.sangjie.constant.Constant;
import com.sangjie.util.HttpUtils;
import com.sangjie.util.LogUtils;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class DownLoaderTask implements Callable<Boolean> {

    private String url;
    private long startPos;
    private long endPos;
    private int part; //下载标识
    private CountDownLatch countDownLatch;

    public DownLoaderTask(String url, long startPos, long endPos, int part, CountDownLatch countDownLatch) {
        this.url = url;
        this.startPos = startPos;
        this.endPos = endPos;
        this.part = part;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public Boolean call() throws Exception {
        //获取文件名
        String name = HttpUtils.getHttpFileName(url);
        //分块文件名
        name = name + ".temp" + part;
        //下载路径
        name = Constant.PATH + name;
        //获取分块下载链接
        HttpURLConnection httpURLConnection = HttpUtils.getHttpConnection(url, startPos, endPos);


        try (
                InputStream in = httpURLConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(in);
                RandomAccessFile accessFile = new RandomAccessFile(name, "rw")
                ) {
            byte[] buffer = new byte[Constant.BYTE_SIZE];
            int len = -1;
            while ((len = bis.read(buffer)) != -1) {
                //1秒内下载数据之和,通过原子类进行操作
                DownloaderInfoThread.downSize.add(len);
                accessFile.write(buffer, 0, len);
            }

        } catch (FileNotFoundException e) {
            LogUtils.error("下载文件不存在{}", url);
            return false;
        } catch (Exception e) {
            LogUtils.error("下载异常");
            return false;
        } finally {
            httpURLConnection.disconnect();

            countDownLatch.countDown();
        }
        return true;
    }
}
