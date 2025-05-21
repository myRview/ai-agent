package com.hk.aiagent.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author huangkun
 * @date 2025/5/15 16:08
 */
@Data
public class UserChatMessage {


    /**
     * 对话ID，用于标识用户会话
     */
    @NotBlank(message = "对话ID不能为空")
    private String conversantId;

    /**
     * 用户发送的消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;
}
