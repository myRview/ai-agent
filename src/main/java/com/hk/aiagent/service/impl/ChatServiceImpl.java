package com.hk.aiagent.service.impl;

import cn.hutool.core.lang.UUID;
import com.hk.aiagent.ai.ChatAIClient;
import com.hk.aiagent.model.dto.UserChatMessage;
import com.hk.aiagent.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author huangkun
 * @date 2025/5/15 16:03
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ChatAIClient chatAIClient;
    @Override
    public String sendMessage(UserChatMessage userMessage) {
        String answer = chatAIClient.doChatPromptTemplate(userMessage.getMessage(), userMessage.getConversantId(),"小智","智者");
        return answer;
    }

    @Override
    public String startNewConversation() {
        // 生成唯一的对话ID
        String conversationId = UUID.randomUUID().toString();
        // 初始化对话参数
        return conversationId;
    }

    @Override
    public void getChatHistory(String conversationId) {

    }

    @Override
    public void resetConversation(String conversationId) {

    }

    @Override
    public String generateImage(String subject, String environment, Integer height, Integer width, String style) {
        String prompt = String.format(
                "一个%s，置身于%s的环境中，使用%s的艺术风格，高清4K画质，细节精致",
                subject, environment, style
        );
        return chatAIClient.generateImage(prompt, height, width);
    }

    @Override
    public String parseImage(MultipartFile file, String userMessage) {
        List<Media> mediaList = List.of(new Media(MimeTypeUtils.IMAGE_PNG,file.getResource()));
        UserMessage message = new UserMessage(userMessage, mediaList);

        String result = chatAIClient.parseImage(message);
        return result;
    }
}
