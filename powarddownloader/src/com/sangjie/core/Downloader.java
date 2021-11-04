package com.sangjie.core;

import com.sangjie.constant.Constant;
import com.sangjie.util.FileUtils;
import com.sangjie.util.HttpUtils;
import com.sangjie.util.LogUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * 下载器
 */
public class Downloader {

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    public ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(Constant.THREAD_NUM, Constant.THREAD_NUM, 0,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(Constant.THREAD_NUM));
    public CountDownLatch countDownLatch = new CountDownLatch(Constant.THREAD_NUM);

    public void downloader(String url) {
        //获取文件名
        String httpFileName = HttpUtils.getHttpFileName(url);

        //存储到指定位置
        httpFileName = Constant.PATH + httpFileName;

        //获取本地文件大小
        long localFileSize = FileUtils.getFileSize(httpFileName);

        //获取链接对象
        HttpURLConnection httpURLConnection = null;
        DownloaderInfoThread downloaderInfoThread = null;
        try {
            httpURLConnection = HttpUtils.getHttpConnection(url);

            //下载文件总大小
            int contentLength = httpURLConnection.getContentLength();

            if (localFileSize >= contentLength ) {
                LogUtils.info("{}已下载完毕", httpFileName);
                return;
            }

            //创建任务对象
            downloaderInfoThread = new DownloaderInfoThread(contentLength);

            //将任务交给线程池执行
            scheduledExecutorService.scheduleAtFixedRate(downloaderInfoThread, 1, 1, TimeUnit.SECONDS);

            //切分任务
            ArrayList<Future> arrayList = new ArrayList<Future>();
            split(url, arrayList);

            /*for (Future f : arrayList) {
                try {
                    f.get();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }*/
            countDownLatch.await();

            //合并文件
            if(merge(httpFileName)) {
                //清理文件
                clearTemp(httpFileName);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        /*try (
                //获取输入流
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                FileOutputStream fos = new FileOutputStream(httpFileName);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
        ){
            int len = -1;
            byte[] buffer = new byte[Constant.BYTE_SIZE];
            while ((len = bis.read(buffer)) != -1) {
                downloaderInfoThread.downSize += len;
                bos.write(buffer, 0, len);
            }
        } catch (FileNotFoundException e) {
           LogUtils.error("下载文件不存在{}", url);
        } catch (Exception e) {
            LogUtils.error("下载失败");
        }*/
          catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("\r");
            System.out.println("下载完成");
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }

            scheduledExecutorService.shutdownNow();

            poolExecutor.shutdown();
        }
    }

    /**
     * 文件切分
     * @param url
     * @param futures
     */
    public void split(String url, ArrayList<Future> futures) {
        //获取下载文件大小
        try {
            long cotentLength = HttpUtils.getHttpContentLength(url);

            //计算切分后的文件大小
            long size = cotentLength / Constant.THREAD_NUM;

            //计算分块个数
            for (int i = 0; i < Constant.THREAD_NUM; i++) {
                //计算起始位置
                long startPos = i * size;
                //结束位置
                long endPos;
                if (i == Constant.THREAD_NUM - 1) {
                    //最后一块
                    endPos = 0;
                } else {
                    endPos = startPos + size;
                }

                //如果不是第一块，起始位置要加一
                if (startPos != 0) {
                    startPos++;
                }

                DownLoaderTask downLoaderTask = new DownLoaderTask(url, startPos, endPos, i, countDownLatch);

                //将任务提交到线程池中
                Future<Boolean> future = poolExecutor.submit(downLoaderTask);
                futures.add(future);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 文件合并
     * @param fileName
     * @return
     */
    public boolean merge(String fileName) {
        LogUtils.info("开始合并文件{}", fileName);
        byte[] buffer = new byte[Constant.BYTE_SIZE];
        int len = -1;
        try (RandomAccessFile accessFile = new RandomAccessFile(fileName, "rw")){
            for (int i = 0; i < Constant.THREAD_NUM; i++) {
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName + ".temp" + i))){
                    while ((len = bis.read(buffer)) != -1) {
                        accessFile.write(buffer, 0, len);
                    }
                }
            }
            LogUtils.info("文件合并完毕{}" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return  false;
        }
        return true;
    }

    /**
     * 清空临时文件
     * @param filName
     * @return
     */
    public boolean clearTemp(String filName) {
        for (int i = 0; i <Constant.THREAD_NUM; i++) {
            File file = new File(filName + ".temp" + i);
            file.delete();
        }

        return true;
    }
}
