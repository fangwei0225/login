package com.taoding.mp.base.controller;

import com.taoding.mp.base.model.ResponseVO;
import com.taoding.mp.util.BosUtils;
import com.taoding.mp.util.CommonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传、处理接口类
 *
 * @author wuwentan
 * @date 2018/8/20
 */
@RestController
@RequestMapping
public class FileController {

    @Value("${file.upload.acceptTypes}")
    String acceptTypes;

    @RequestMapping("/server/file/upload")
    public ResponseVO fileUpload(MultipartFile file) throws IOException {
        Map<String, Object> resultMap = new HashMap<>();
        String key = "";
        String url = "";
        String thumbnailUrl = "";
        String name = "";
        String suffix = "";
        //上传文件为空抛出异常处理
        if (file == null || file.equals("") || file.getSize() <= 0) {
            return new ResponseVO(400, "上传文件内容为空");
        } else {
            //验证文件格式是否为可接受上传文件
            String originalFilename = file.getOriginalFilename();
            suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            name = file.getOriginalFilename();
            if (acceptTypes.indexOf(suffix) < 0) {
                return new ResponseVO(400, "文件格式不支持");
            } else {
                //调用百度云文件上传工具类返回文件key
                key = BosUtils.PutObjectFromInputStream(file.getInputStream(), suffix, originalFilename);
                //获取百度云文件的url
                url = BosUtils.getUrlByBosKey(key);
                thumbnailUrl = BosUtils.getUrlByBosKey(key, true).getThumbnailUrl();
                file.getInputStream().close();
            }
        }
        resultMap.put("key", key);
        resultMap.put("url", url);
        resultMap.put("thumbnailUrl", thumbnailUrl);
        resultMap.put("name", name);
        resultMap.put("type", suffix);
        resultMap.put("size", file.getSize());
        return new ResponseVO(resultMap);
    }

    /**
     * 图片上传（保存本地服务器）
     *
     * @param image
     * @return
     */
    @RequestMapping("/file/imageUpload")
    public ResponseVO imageUpload(@RequestParam String image) {
        String facePicPath = "";
        String corpId = "taoding";
        String faceId = CommonUtils.getUUID();
        //截取图片类型:jpg,png,gif
        String imageType = image.substring(image.indexOf("/") + 1, image.indexOf(";"));
        image = image.replace("data:image/" + imageType + ";base64,", "");
        byte[] bytes = CommonUtils.getFromBASE64(image);
        OutputStream out = null;
        String outputPath = facePicPath + File.separator + corpId + File.separator;
        File destFile = new File(outputPath);
        if (!destFile.exists()) {
            destFile.mkdirs();
        }
        try {
            out = new FileOutputStream(outputPath + File.separator + faceId + "." + imageType);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseVO(outputPath);
    }

    /**
     * 富文本编辑器文件上传
     *
     * @param upload
     * @return
     * @throws IOException
     */
    @RequestMapping("/file/ckEditorUpload")
    public Map<String, Object> ueditorUpload(MultipartFile upload) throws IOException {
        Map<String, Object> resultMap = new HashMap<>();
        String key = "";
        String url = "";
        String name = "";

        //上传文件为空抛出异常处理
        if (upload == null || upload.equals("") || upload.getSize() <= 0) {
            throw new RuntimeException("上传文件为空");
        } else {
            //验证文件格式是否为可接受上传文件
            String suffix = upload.getOriginalFilename().substring(upload.getOriginalFilename().lastIndexOf(".") + 1);
            name = upload.getOriginalFilename();
            if (acceptTypes.indexOf(suffix) < 0) {
                throw new RuntimeException("上传文件格式不支持");
            } else {
                //调用百度云文件上传工具类返回文件key
                key = BosUtils.PutObjectFromInputStream(upload.getInputStream(), suffix, name);
                //获取百度云文件的url
                url = BosUtils.getUrlByBosKey(key);
                upload.getInputStream().close();
            }
        }
        resultMap.put("uploaded", "1");
        resultMap.put("url", url);
        return resultMap;
    }
}
