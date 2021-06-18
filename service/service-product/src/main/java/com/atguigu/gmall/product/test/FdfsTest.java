package com.atguigu.gmall.product.test;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/17 9:17
 * @Description:
 */
public class FdfsTest {
    public static void main(String[] args) throws Exception {

        ClassLoader classLoader = FdfsTest.class.getClassLoader();

        String path = classLoader.getResource("tracker.conf").getPath();

        ClientGlobal.init(path);

        //获取tracker service
        TrackerClient trackerClient = new TrackerClient();

        TrackerServer trackerServer = trackerClient.getConnection();

        //获取storage客户端连接
        StorageClient storageClient = new StorageClient(trackerServer,null);

        //上传或下载文件
        String[] urls = storageClient.upload_appender_file("G:\\images\\2.jpg", "jpg", null);

        for (String url : urls) {
            System.out.println("url = " + url);
        }
    }
}
