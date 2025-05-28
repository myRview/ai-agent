package com.hk.aiagent.ai;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.hk.aiagent.advisor.MyLoggerAdvisor;
import com.hk.aiagent.chatmemory.FileChatMemory;
import com.hk.aiagent.common.ErrorCode;
import com.hk.aiagent.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.image.*;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author huangkun
 * @date 2025/5/13 15:32
 */
@Component
@Slf4j
public class ChatAIClient {
    private final ChatClient chatClient;

    private final ImageModel imageModel;
    private SystemPromptTemplate systemPromptTemplate;


    @Value("${chat.system-message}")
    private Resource systemResource;

    @Autowired
    private ToolCallback[] allTools;

    private static final String DEFAULT_MODEL = "qwen-vl-max-latest";
    @Autowired
    private Advisor augmentationAdvisor;

    public ChatAIClient(ChatModel dashScopeChatModel, ImageModel imageModel) {
        ChatMemory chatMemory = new FileChatMemory();
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory), new MyLoggerAdvisor())
                .build();
        this.imageModel = imageModel;
    }

    @PostConstruct
    public void init() {
        log.info("加载系统配置信息");
        systemPromptTemplate = new SystemPromptTemplate(systemResource);
    }


    /**
     * 获取系统消息
     * @param message
     * @param conversantId
     * @param params
     * @return
     */
    public String doChatPromptTemplate(String message, String conversantId, String... params) {

        String systemMessage = getSystemMessage(params);

        try {
            ChatResponse response = chatClient
                    .prompt()
                    .system(systemMessage)
                    .user(message)
                    .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversantId)
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                    )
                    .advisors(augmentationAdvisor)
                    .tools(allTools)
                    .call().chatResponse();
            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("调用失败", e);
            throw new BusinessException(ErrorCode.ERROR_SYSTEM, "调用失败");
        }
    }

    public Flux<String> doChatPromptTemplateStream(String message, String conversantId, String... params) {

        String systemMessage = getSystemMessage(params);

        try {
            Flux<String> flux = chatClient
                    .prompt()
                    .system(systemMessage)
                    .user(message)
                    .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversantId)
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                    )
                    .advisors(augmentationAdvisor)
                    .stream()
                    .content();
            return flux;
        } catch (Exception e) {
            log.error("调用失败", e);
            throw new BusinessException(ErrorCode.ERROR_SYSTEM, "调用失败");
        }
    }

    /**
     * 生成图片
     * @param prompt  描述
     * @param height  高度
     * @param width   宽度
     * @return
     */
    public String generateImage(String prompt, Integer height, Integer width) {
        /**
         * 构建图片数据
         */
        ImageOptions imageOptions = ImageOptionsBuilder.builder()
                .height(height)
                .width(width)
                .build();
        ImagePrompt imagePrompt = new ImagePrompt(prompt, imageOptions);
        String imageUrl = null;
        try {
            ImageResponse imageResponse = imageModel.call(imagePrompt);
            imageUrl = imageResponse.getResult().getOutput().getUrl();
            return imageUrl;
        } catch (Exception e) {
            log.error("生成图片失败", e);
            throw new BusinessException(ErrorCode.ERROR_SYSTEM, "生成图片失败");
        }
    }

    /**
     * 解析图片
     * @param message
     * @return
     */
    public String parseImage(UserMessage message) {
        try {
            ChatResponse response = chatClient
                    .prompt(new Prompt(message,
                            DashScopeChatOptions.builder().withModel(DEFAULT_MODEL).withMultiModel(true).build()))
                    .call()
                    .chatResponse();
            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("解析失败", e);
            throw new BusinessException(ErrorCode.ERROR_SYSTEM, "解析失败");
        }
    }

    private String getSystemMessage(String[] params) {
        if (params == null || params.length == 0) {
            return this.systemPromptTemplate.createMessage().getText();
        }
        int length = params.length;
        HashMap<String, Object> map = new HashMap<>();
        List<String> strings = extractVariables(this.systemPromptTemplate.getTemplate());
        if (length > 0 && strings.size() >= length) {
            for (int i = 0; i < length; i++) {
                String param = params[i];
                String key = strings.get(i);
                map.put(key, param);
            }
        }
        Message systemMessage = this.systemPromptTemplate.createMessage(map);
        return systemMessage.getText();
    }

    /**
     * 获取变量
     * @param templateContent
     * @return
     */
    private List<String> extractVariables(String templateContent) {
        List<String> variables = new ArrayList<>();
        // 定义正则表达式模式，匹配 {...} 格式的变量
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(templateContent);

        // 查找所有匹配的变量
        while (matcher.find()) {
            // group(1) 获取括号内的内容（即变量名）
            variables.add(matcher.group(1));
        }

        return variables;
    }

}
