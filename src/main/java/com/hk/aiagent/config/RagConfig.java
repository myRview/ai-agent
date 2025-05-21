package com.hk.aiagent.config;

import com.hk.aiagent.rag.RagDocumentLoader;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author huangkun
 * @date 2025/5/17 10:49
 */
@Configuration
public class RagConfig {

    @Autowired
    private RagDocumentLoader ragDocumentLoader;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;
    @Autowired
    private ChatModel dashScopeChatModel;
//    @Bean
//    VectorStore vectorStore(EmbeddingModel embeddingModel) {
//        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel)
//                .build();
//        List<Document> documents = ragDocumentLoader.loadMarkDowns();
//        simpleVectorStore.add(documents);
//        return simpleVectorStore;
//    }


    /**
     * 创建阿里文档检索器
     *
     * @return
     */
/*    @Bean
    public Advisor augmentationAdvisor() {
        DashScopeApi dashscopeApi = new DashScopeApi(apiKey);
        final String indexName = "恋爱大师";
        DashScopeDocumentRetriever retriever = new DashScopeDocumentRetriever(dashscopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName(indexName)
                        .build()
        );
        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .build();
        return advisor;
    }*/
    @Bean
    public Advisor augmentationAdvisor(EmbeddingModel embeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        List<Document> documents = ragDocumentLoader.loadMarkDowns();
        KeywordMetadataEnricher metadataEnricher = new KeywordMetadataEnricher(dashScopeChatModel, 5);
        List<Document> documentList = metadataEnricher.apply(documents);
        simpleVectorStore.add(documentList);

//        FilterExpressionBuilder fb = new FilterExpressionBuilder();
//        FilterExpressionBuilder.Op filter = fb.and(fb.eq("category", "恋爱"), fb.eq("type", "单身"));

        //使用spring AI内置的检索增强器
        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                // 配置查询增强器
                .queryAugmenter(
                        //错误情况处理
                        ContextualQueryAugmenter
                                .builder()
                                //允许空上下文查询
                                .allowEmptyContext(true)
//                                .emptyContextPromptTemplate()
                                .build()
                )
                // 配置文档检索器
                .documentRetriever(VectorStoreDocumentRetriever
                                .builder()
                                //相似度阈值
                                .similarityThreshold(0.5)
                                //返回前3个最相关的文档
                                .topK(3)
                                //复杂过滤条件
//                        .filterExpression(new FilterExpressionBuilder().eq("type","love").build())
                                .vectorStore(simpleVectorStore)
                                .build()
                )
                .queryTransformers(queryTransformer())
                .build();

        return advisor;
    }

    @Bean
    public QueryTransformer queryTransformer() {
        //重写转化器
        return RewriteQueryTransformer.builder().chatClientBuilder(ChatClient.builder(dashScopeChatModel))
                .build();
    }

}
