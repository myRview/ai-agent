package com.hk.aiagent.controller;

import com.hk.aiagent.common.ResponseResult;
import com.hk.aiagent.model.dto.UserChatMessage;
import com.hk.aiagent.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author huangkun
 * @date 2025/5/14 18:10
 */
@Tag(name = "ChatController", description = "AI应用接口")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;


    @Operation(summary = "发送消息")
    @PostMapping("/send")
    public ResponseResult<?> sendMessage(@RequestBody UserChatMessage userMessage) {
        String message = chatService.sendMessage(userMessage);
        return ResponseResult.success(message);
    }

    @Operation(summary = "开始新的对话")
    @PostMapping("/start")
    public ResponseResult<?> startNewConversation() {
        String conversationId = chatService.startNewConversation();
        return ResponseResult.success(conversationId);
    }

    @Operation(summary = "获取对话历史")
    @GetMapping("/history")
    public ResponseResult<?> getChatHistory(@RequestParam String conversationId) {
        chatService.getChatHistory(conversationId);
        return ResponseResult.success();
    }

    @Operation(summary = "重置对话")
    @GetMapping("/reset")
    public ResponseResult resetConversation(@RequestParam String conversationId) {
        chatService.resetConversation(conversationId);
        return ResponseResult.success();
    }

    @Operation(summary = "生成图片")
    @GetMapping("/image/generate")
    public ResponseResult<?> multipleGenerate(
            @RequestParam(value = "subject", defaultValue = "一只会编程的猫") String subject,
            @RequestParam(value = "environment", defaultValue = "办公室") String environment,
            @RequestParam(value = "height", defaultValue = "1024") Integer height,
            @RequestParam(value = "width", defaultValue = "1024") Integer width,
            @RequestParam(value = "style", defaultValue = "生动") String style) {
        String imgUrl = chatService.generateImage(subject, environment, height, width, style);
        return ResponseResult.success(imgUrl);
    }

    @Operation(summary = "解析图片")
    @PostMapping("/image/parse")
    public ResponseResult<?> parseImage(@RequestParam MultipartFile file, @RequestParam(required = false, defaultValue = "解析一下图片信息") String userMessage) {
        String parseResult = chatService.parseImage(file, userMessage);
        return ResponseResult.success(parseResult);
    }


}