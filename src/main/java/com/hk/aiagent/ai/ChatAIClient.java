package com.hk.aiagent.ai;

import com.hk.aiagent.advisor.MyLoggerAdvisor;
import com.hk.aiagent.chatmemory.FileChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

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
    public ChatAIClient(ChatModel dashScopeChatModel) {
        ChatMemory chatMemory = new FileChatMemory();
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory), new MyLoggerAdvisor())
                .build();
    }

    @Value("classpath:/prompts/${chat.fileName}.st")
    private Resource systemResource;

    public String doChatPromptTemplate(String message, String conversantId, String... params) {

//        String userText = "告诉我三个来自海盗黄金时代的著名海盗，以及他们为什么这样做。为每个海盗至少写一个句子。";
//        String systemText = "你是一个有用的人工智能助手，帮助人们找到信息。你的名字是 {name}你应该用你的名字和{voice}的方式回复用户的请求。";

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        HashMap<String, Object> map = new HashMap<>();

        List<String> strings = extractVariables(systemPromptTemplate.getTemplate());
        int length = params.length;
        if (length > 0 && strings.size() >= length) {
            for (int i = 0; i < length; i++) {
                String param = params[i];
                String key = strings.get(i);
                map.put(key, param);
            }
        }
        Message systemMessage = systemPromptTemplate.createMessage(map);
        ChatResponse response = chatClient
                .prompt()
                .system(systemMessage.getText())
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversantId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
                )
                .call().chatResponse();

        return response.getResult().getOutput().getText();
    }

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
