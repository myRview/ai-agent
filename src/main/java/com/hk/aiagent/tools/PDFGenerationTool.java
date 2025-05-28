package com.hk.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.hk.aiagent.constant.CommonConstant;
import com.hk.aiagent.cos.TenXunCosManager;
import com.hk.aiagent.enums.FileSuffixEnum;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.qcloud.cos.model.PutObjectResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class PDFGenerationTool {

    private final TenXunCosManager tenXunCosManager;

    public PDFGenerationTool(TenXunCosManager tenXunCosManager) {
        this.tenXunCosManager = tenXunCosManager;
    }


    @Tool(description = "Generate a PDF file with given content")
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {
        String value = FileSuffixEnum.PDF.getValue();
        String suffix = CommonConstant.POINT_SEPARATOR + value;
        if (!fileName.toLowerCase().endsWith(suffix)) {
            fileName += suffix;
        }
        // 使用配置的临时目录构建路径
        String fileDir = System.getProperty("user.dir") + CommonConstant.FILE_TEMP_PATH + value;
        String filePath = value + CommonConstant.SEPARATOR + fileName;

        try {
            // 创建目录（如果不存在）
            FileUtil.mkdir(fileDir);
            File tempFile = FileUtil.createTempFile(new File(fileDir, filePath));
            try (PdfWriter writer = new PdfWriter(tempFile);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                // 自定义字体（需要人工下载字体文件到特定目录）
//                String fontPath = Paths.get("src/main/resources/static/fonts/simsun.ttf")
//                        .toAbsolutePath().toString();
//                PdfFont font = PdfFontFactory.createFont(fontPath,
//                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                // 使用内置中文字体
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);
                // 添加内容并生成PDF
                Paragraph paragraph = new Paragraph(content);
                document.add(paragraph);
            }

            // 上传至COS并获取访问URL
            String uploadUrl = tenXunCosManager.upload(filePath, tempFile);
            // 删除临时文件
            FileUtil.del(tempFile);
            return "PDF生成成功，访问地址：" + uploadUrl;
        } catch (IOException e) {
            e.printStackTrace();
            return "PDF生成失败：" + e.getMessage();
        }
    }
}
