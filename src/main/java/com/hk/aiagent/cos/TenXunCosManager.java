package com.hk.aiagent.cos;

import com.hk.aiagent.common.ErrorCode;
import com.hk.aiagent.cos.config.CosConfig;
import com.hk.aiagent.exception.BusinessException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.DeleteObjectsRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.job.DocHtmlRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 腾讯云对象存储
 *
 * @author huangkun
 * @date 2025/5/27 11:47
 */
@Component
@Slf4j
public class TenXunCosManager {

    @Autowired
    private CosConfig cosConfig;

    @Autowired
    private COSClient cosClient;


    /**
     * @param fileKey 指定文件上传到 COS 上的路径，即对象键。例如对象键为 folder/picture.jpg，则表示将文件 picture.jpg 上传到 folder 路径下
     * @param file    上传的文件
     * @return
     */
    public String upload(String fileKey, File file) {
        try {
            String bucket = cosConfig.getBucket();
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileKey, file);
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            URL url = cosClient.getObjectUrl(bucket, fileKey);
            return url.toString();
        } catch (CosClientException e) {
            log.error("上传文件失败", e);
            throw new BusinessException(ErrorCode.ERROR_SYSTEM, "上传文件失败:" + e);
        }
    }

    /**
     * 流式上传文件
     *
     * @param fileKey
     * @param inputStream
     * @param metadata
     * @return
     */
    public String upload(String fileKey, InputStream inputStream, ObjectMetadata metadata) {
        try {
            if (metadata == null) {
                metadata = new ObjectMetadata();
            }
            String bucket = cosConfig.getBucket();
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileKey, inputStream, metadata);
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            URL url = cosClient.getObjectUrl(bucket, fileKey);
            return url.toString();
        } catch (CosClientException e) {
            log.error("上传文件失败", e);
            throw new BusinessException(ErrorCode.ERROR_SYSTEM, "上传文件失败:" + e);
        }
    }

    public String uploadDocGeneratorPreviewUrl(String fileKey, File file, String srcType) {
        try {
            String bucket = cosConfig.getBucket();
            upload(fileKey, file);
            String previewUrl = getPreviewUrl(fileKey, srcType, bucket);
            return previewUrl;
        } catch (CosClientException e) {
            log.error("上传文件失败", e);
            throw new BusinessException(ErrorCode.ERROR_SYSTEM, "上传文件失败:" + e);
        } catch (URISyntaxException e) {
            log.error("生产预览地址失败", e);
            throw new BusinessException(ErrorCode.ERROR_SYSTEM, "生产预览地址失败：" + e);
        }
    }

    /**
     * * 生成预览地址
     * * 目前支持的输入文件类型包含如下格式：
     * * 演示文件：pptx、ppt、pot、potx、pps、ppsx、dps、dpt、pptm、potm、ppsm。
     * * 文字文件：doc、dot、wps、wpt、docx、dotx、docm、dotm。
     * * 表格文件：xls、xlt、et、ett、xlsx、xltx、csv、xlsb、xlsm、xltm、ets。
     * * 表格文件，一张表可能分割为多页转换，生成多张图片。
     * * 其他格式文件： pdf、 lrc、 c、 cpp、 h、 asm、 s、 java、 asp、 bat、 bas、 prg、 cmd、 rtf、 txt、 log、 xml、 htm、 html。
     *
     * @param fileKey
     * @param srcType
     * @param bucket
     * @return
     * @throws URISyntaxException
     */
    private String getPreviewUrl(String fileKey, String srcType, String bucket) throws URISyntaxException {
        //1.创建请求对象
        DocHtmlRequest request = new DocHtmlRequest();
        //2.添加请求参数，参数详情请见 API 接口文档
        request.setBucketName(bucket);
        //如果需要转为图片 dstType 为 DocHtmlRequest.DocType.jpg
        request.setDstType(DocHtmlRequest.DocType.jpg);
        request.setObjectKey(fileKey);
        request.setSrcType(srcType);
        request.setPage("1");
        //3.调用接口，获取任务响应对象
        String previewUrl = cosClient.generateDocPreviewUrl(request);
        return previewUrl;
    }


    /**
     * 删除存储对象
     *
     * @param fileKey
     */
    public void deleteObject(String fileKey) {
        try {
            cosClient.deleteObject(cosConfig.getBucket(), fileKey);
        } catch (CosClientException e) {
            throw new BusinessException(ErrorCode.ERROR_SYSTEM, "删除文件失败:" + e);
        }
    }

    /**
     * 删除存储对象
     *
     * @param fileKey
     */
    public void deleteObjects(String... fileKey) {
        List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>(fileKey.length);
        for (int i = 0; i < fileKey.length; i++) {
            String key = fileKey[i];
            DeleteObjectsRequest.KeyVersion keyVersion = new DeleteObjectsRequest.KeyVersion(key);
            keys.add(keyVersion);
        }
        try {
            String bucket = cosConfig.getBucket();
            DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucket);
            deleteRequest.setKeys(keys);
            cosClient.deleteObjects(deleteRequest);
        } catch (CosClientException e) {
            throw new BusinessException(ErrorCode.ERROR_SYSTEM, "删除文件失败:" + e);
        }
    }
}
