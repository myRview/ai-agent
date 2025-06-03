package com.hk.aiagent.tools;

import com.hk.aiagent.cos.TenXunCosManager;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具注册类
 * @author huangkun
 * @date 2025/5/26 10:44
 */
@Configuration
public class ToolRegistrationConfig {

    @Value("${search.apiKey}")
    private String apiKey;
    @Autowired
    private TenXunCosManager tenXunCosManager;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool operationTool = new FileOperationTool();
        OnlineSearchTool onlineSearchTool = new OnlineSearchTool(apiKey);
        ResourceDownloadTool downloadTool = new ResourceDownloadTool();
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool(tenXunCosManager);
        TerminateTool terminateTool = new TerminateTool();
        return ToolCallbacks.from(operationTool, onlineSearchTool, downloadTool, webScrapingTool,pdfGenerationTool,terminateTool);
    }
}
