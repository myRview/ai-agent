package com.hk.aiagent.rag;

import com.hk.aiagent.common.ErrorCode;
import com.hk.aiagent.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huangkun
 * @date 2025/5/17 11:05
 */
@Component
@Slf4j
public class RagDocumentLoader {


    private final ResourcePatternResolver resourcePatternResolver;

    public RagDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载文档
     * @return
     */
    public List<Document> loadMarkDowns() {

        List<Document> documents = new ArrayList<>();
        try {
            // 获取classpath:document/*.md路径下的所有资源
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false) //包含代码块
                        .withIncludeBlockquote(false) //包含引用块
//                        .withAdditionalMetadata("type","love")  // 添加自定义元数据
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                List<Document> readDocuments = reader.get();
                documents.addAll(readDocuments);
            }
        } catch (IOException e) {
            log.error("加载文档失败", e);
            throw new BusinessException(ErrorCode.ERROR_SYSTEM,"加载文档失败");
        }
        return documents;

    }
}
