package com.sangjie.core;

import com.sangjie.constant.Constant;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 展示下载信息
 */
public class DownloaderInfoThread implements Runnable{

    private long fileSize; // 文件大小
    private static LongAdder finishedFileSize = new LongAdder(); //已经下载的文件大小

    public static volatile LongAdder downSize = new LongAdder(); // 本次下载文件大小

    private double preSize; //前一次下载大小

    public DownloaderInfoThread(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public void run() {
        //计算文件总大小
        String total = String.format("%.2f", fileSize / Constant.MB);

        //计算每秒下载速度 kb
        int speed = (int)((downSize.doubleValue() - preSize) / 1024d);
        preSize = downSize.doubleValue();

        //剩余文件大小
        double remainSize = fileSize - finishedFileSize.doubleValue() - downSize.doubleValue();

        //估算剩余时间
        String remainTime = String.format("%.1f", remainSize / 1024d / speed);

        if ("Infinity".equalsIgnoreCase(remainTime)) {
            remainTime = "-";
        }

        //已下载大小
        String currentFileSize = String.format("%.2f",(downSize.doubleValue() - finishedFileSize.doubleValue()) / Constant.MB);

        String downInfo = String.format("已下载 %smb/%smb, 速度是%skb/s, 剩余时间%ss", currentFileSize,
                total, speed, remainTime);

        System.out.print("\r");
        System.out.print(downInfo);
    }
}
