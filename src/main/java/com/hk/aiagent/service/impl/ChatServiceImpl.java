package com.hk.aiagent.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import com.hk.aiagent.ai.ChatAIClient;
import com.hk.aiagent.chatmemory.FileChatMemory;
import com.hk.aiagent.model.dto.UserChatMessage;
import com.hk.aiagent.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huangkun
 * @date 2025/5/15 16:03
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ChatAIClient chatAIClient;
    @Autowired
    private FileChatMemory fileChatMemory;
    @Override
    public String sendMessage(UserChatMessage userMessage) {
        String answer = chatAIClient.doChatPromptTemplate(userMessage.getMessage(), userMessage.getConversantId());
        return answer;
    }

    @Override
    public Flux<String> sendMessageStream(UserChatMessage userMessage) {
        Flux<String> stringFlux = chatAIClient.doChatPromptTemplateStream(userMessage.getMessage(), userMessage.getConversantId());
        return stringFlux;
    }

    @Override
    public String startNewConversation() {
        // 生成唯一的对话ID
        String conversationId = UUID.randomUUID().toString();
        // 初始化对话参数
        return conversationId;
    }

    @Override
    public List<UserChatMessage> getChatHistory(String conversationId, Integer lastN) {
        List<UserChatMessage> chatMessages =new ArrayList<>();
        List<Message> messages = fileChatMemory.get(conversationId, lastN);
        if (CollectionUtil.isNotEmpty(messages)){
            chatMessages = messages.stream().map(this::convertToUserChatMessage).collect(Collectors.toList());
        }
        return chatMessages;
    }

    //将message实体转换为UserChatMessage
    private UserChatMessage convertToUserChatMessage(Message message){
        UserChatMessage userChatMessage = new UserChatMessage();
        userChatMessage.setMessage(message.getText());
        return userChatMessage;
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
