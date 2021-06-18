package com.atguigu.gmall.product.utils;

import com.atguigu.gmall.product.test.FdfsTest;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/17 10:56
 * @Description:
 */
public class FileUploadUtil {


    public static String imageUpload(MultipartFile multipartFile){

        String imageUrl = "http://192.168.200.128:8080";

        try {
            ClassLoader classLoader = FdfsTest.class.getClassLoader();
            String path = classLoader.getResource("tracker.conf").getPath();
            ClientGlobal.init(path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        //获取tracker service
        TrackerClient trackerClient = new TrackerClient();

        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //获取storage客户端连接
        StorageClient storageClient = new StorageClient(trackerServer,null);

        //上传或下载文件
        try {
            String filenameExtension = StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
            String[] urls = storageClient.upload_appender_file(multipartFile.getBytes(), filenameExtension, null);
            for (String url : urls) {
                imageUrl = imageUrl + "/" + url;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        return imageUrl;
    }
}
