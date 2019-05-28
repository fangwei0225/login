package com.taoding.mp.util;

import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.*;
import com.taoding.mp.base.model.BosVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: BosUtils</p>
 * <p>Description: 百度上传文件工具类</p>
 *
 * @author kinglo
 * @version 1.0.0
 * @date 2018/6/13 10:55
 */
public class BosUtils {

    private static Logger logger = LoggerFactory.getLogger(BosUtils.class);

    private static BosClient client;

    /**
     * 百度BOS文件库名称
     */
    private static String bucketName;

    /**
     * 百度BOS文件链过期时间
     */
    private static Integer expireTime;

    /**
     * 缩略图配置
     */
    private static String imgLayout = "@s_0,w_300,q_60,f_png";

    /**
     * 缩略图配置
     */
    private static String imgLayoutRaw = "@s_0,w_720,q_60,f_png";

    public static void run(String accessKey, String secretKey, String bucketName, String expireTime) {
        BosUtils.bucketName = bucketName;
        BosUtils.expireTime = Integer.parseInt(expireTime);
        BosClientConfiguration config = new BosClientConfiguration();
        // 设置HTTP最大连接数为10
        config.setMaxConnections(100);
        // 设置TCP连接超时为5000毫秒
        config.setConnectionTimeoutInMillis(50000);
        // 设置Socket传输数据超时的时间为2000毫秒
        config.setSocketTimeoutInMillis(30000);
        config.setCredentials(new DefaultBceCredentials(accessKey, secretKey));
        client = new BosClient(config);
        logger.info("BOS 启动！");
    }

    /**
     * 根据百度bosKey获取文件的字节数组
     *
     * @param objectKey
     * @return
     * @throws IOException
     */
    public static byte[] getObjectByte(String objectKey) throws IOException {
        //获取Object，返回结果为BosObject对象
        BosObject object = client.getObject(bucketName, objectKey);
        // 获取ObjectMeta
        ObjectMetadata meta = object.getObjectMetadata();
        //获取Object的输入流
        InputStream objectContent = object.getObjectContent();
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = objectContent.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        objectContent.close();
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

    /**
     * 获取Object的URL
     *
     * @param objectKey
     * @return
     */
    public static String generatePresignedUrl(String objectKey, String imgLayout) {
        if (imgLayout != null) {
            objectKey = objectKey + imgLayout;
        }
        URL url = client.generatePresignedUrl(bucketName, objectKey, expireTime);
        //指定用户需要获取的Object所在的Bucket名称、该Object名称、时间戳、URL的有效时长
        return url.toString();
    }

    /**
     * 上传远程图片到bos
     *
     * @param fileUrl
     * @return key
     */
    public static String PutObjectFromURL(String fileUrl) {
        String key = getKey(null);
        try {
            URL url = new URL(fileUrl);
            java.net.URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            client.putObject(bucketName, key, is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return key;
    }

    private static String getKey(String suffix) {
        return CommonUtils.getUUID() + (suffix != null ? "." + suffix : "");
    }

    /**
     * 上传单文件
     *
     * @param ufile
     * @return
     */
    public static String PutObjectFromFile(File ufile) {
        String key = getKey(null);
        PutObjectResponse putObjectFromFileResponse = client.putObject(bucketName, key, ufile);
        // 打印ETag
        System.out.println(putObjectFromFileResponse.getETag());
        return key;
    }

    /**
     * 通过文件流上传单文件
     *
     * @param is     文件流
     * @param suffix 后缀名
     * @return
     */
    public static String PutObjectFromInputStream(InputStream is, String suffix, String fileName) {
        String key = getKey(suffix);
        // 设置额外属性
        PutObjectResponse putObjectFromFileResponse = null;
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentDisposition("attachment;filename=" + new String(fileName.getBytes(), Charset.forName("ISO-8859-1")));
        putObjectFromFileResponse = client.putObject(bucketName, key, is, meta);
        // 打印ETag
        System.out.println(putObjectFromFileResponse.getETag());
        return key;
    }

    /**
     * 通过二进制串上传单文件
     *
     * @param bytes  二进制串
     * @param suffix 后缀名
     * @return
     */
    public static String putObjectFromByte(byte[] bytes, String suffix) {
        String key = getKey(suffix);
        PutObjectResponse putObjectFromFileResponse = client.putObject(bucketName, key, bytes);
        return key;
    }

    /**
     * 通过bosKeys取文件url列表
     *
     * @param bosKeys(英文逗号分割)
     * @return
     */
    public static List<BosVO> getUrlsByBosKeys(String bosKeys, boolean isImage) {
        if (bosKeys == null) {
            return new ArrayList<>();
        }
        List<BosVO> urls = new ArrayList<>();
        String[] keys = bosKeys.split(",");
        for (int i = 0; i < keys.length; i++) {
            String url = null;
            BosVO bosUrlVo = new BosVO();
            if (isImage) {
                url = generatePresignedUrl(keys[i], imgLayoutRaw);
                String thumbnailUrl = generatePresignedUrl(keys[i], imgLayout);
                bosUrlVo.setThumbnailUrl(thumbnailUrl);
            } else {
                url = generatePresignedUrl(keys[i], null);
            }
            bosUrlVo.setKey(keys[i]);
            bosUrlVo.setUrl(url);
            urls.add(bosUrlVo);
        }
        return urls;
    }

    /**
     * 通过bosKey取文件url
     *
     * @param bosKey
     * @return
     */
    public static BosVO getUrlByBosKey(String bosKey, boolean isImage) {
        if (bosKey == null) {
            return null;
        }
        String url = null;
        BosVO bosUrlVo = new BosVO();
        if (isImage) {
            url = generatePresignedUrl(bosKey, imgLayoutRaw);
            String thumbnailUrl = generatePresignedUrl(bosKey, imgLayout);
            bosUrlVo.setThumbnailUrl(thumbnailUrl);
        } else {
            url = generatePresignedUrl(bosKey, null);
        }
        bosUrlVo.setUrl(url);
        bosUrlVo.setKey(bosKey);
        return bosUrlVo;
    }

    /**
     * 根据bosKey获取url
     *
     * @param bosKey
     * @return
     */
    public static String getUrlByBosKey(String bosKey) {
        Assert.notNull(bosKey, "bosKey must be not null!");
        return client.generatePresignedUrl(bucketName, bosKey, -1).toString();
    }

    /**
     * 获取指定的bucket中所有的文件key及url输出到bucket-name.txt，配合百度BOS仓库迁移方案
     */
    private static void getBosKeyList() {
        //网算账号配置
        String wsAcc = "25a51bf25b09491ca1b0e6da296c9ba5";
        String wsSec = "839ab294ccc843b9b01850dc8ad8a752";

        String bucket = "pt-files-test";

        // 初始化一个BosClient
        BosClientConfiguration config = new BosClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(wsAcc, wsSec));
        BosClient client = new BosClient(config);

        // 用户可设置每页最多500条记录
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucket);
        listObjectsRequest.withMaxKeys(500);
        boolean isTruncated = true;

        StringBuilder content = new StringBuilder();
        while (isTruncated) {
            ListObjectsResponse listObjectsResponse = client.listObjects(listObjectsRequest);
            isTruncated = listObjectsResponse.isTruncated();
            if (listObjectsResponse.getNextMarker() != null) {
                listObjectsRequest.withMarker(listObjectsResponse.getNextMarker());
            }
            listObjectsResponse.getContents().forEach(obj -> {
                content.append(obj.getKey() + "\t");
                content.append("http://" + bucket + ".cdn.bcebos.com/" + obj.getKey() + "\tSTANDARD\r\n");
            });
        }
        try {
            FileWriter fileWriter = new FileWriter("/data/" + bucket + ".txt");
            fileWriter.write(content.toString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}
