package com.hk.aiagent.service;

import com.hk.aiagent.model.dto.UserChatMessage;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author huangkun
 * @date 2025/5/15 16:02
 */
public interface ChatService {
    String sendMessage(UserChatMessage userMessage);

    String startNewConversation();

    List<UserChatMessage> getChatHistory(String conversationId, Integer lastN);

    void resetConversation(String conversationId);

    String generateImage(String subject, String environment, Integer height, Integer width, String style);

    String parseImage(MultipartFile file, String userMessage);

    Flux<String> sendMessageStream(UserChatMessage userMessage);
}
