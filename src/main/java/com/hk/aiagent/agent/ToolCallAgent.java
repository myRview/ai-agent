package com.hk.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.hk.aiagent.enums.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huangkun
 * @date 2025/6/2 10:33
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    private final ToolCallback[] toolCallbacks;

    private ChatResponse chatResponse;

    private final ToolCallingManager toolCallingManager;

    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] toolCallbacks) {
        super();
        this.toolCallbacks = toolCallbacks;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder().withProxyToolCalls(false).build();
    }

    @Override
    public boolean think() {
        List<Message> messageList = getMessageList();
        if (StringUtils.isNoneBlank(getNextStepPrompt())) {
            messageList.add(new UserMessage(getNextStepPrompt()));
        }
        try {
            Prompt prompt = new Prompt(messageList, chatOptions);
            chatResponse = getChatClient().prompt(prompt).tools(toolCallbacks).call().chatResponse();
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String text = assistantMessage.getText();
            log.info("response result text:{}", text);
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
            if (toolCalls.isEmpty()) {
                return false;
            }
            String toolName = toolCalls.stream().map(tool -> tool.name()).collect(Collectors.joining(";"));
            log.info("tools call name:{}", toolName);
            return true;
        } catch (Exception e) {
            log.error(getName() + "思考过程出现问题：" + e.getMessage());
            messageList.add(new AssistantMessage("思考过程出现问题" + e.getMessage()));
            return false;
        }
    }

    @Override
    public String act() {
        if (!chatResponse.hasToolCalls()) {
            return "not tool call";
        }
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult executionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
        List<Message> messageList = executionResult.conversationHistory();
        setMessageList(messageList);
        ToolResponseMessage responseMessage = (ToolResponseMessage) CollUtil.getLast(messageList);
        if (responseMessage == null) {
            return "no tool call response";
        }
        String result = responseMessage.getResponses().stream()
                .map(response -> "工具" + response.name() + "返回结果：" + response.responseData())
                .collect(Collectors.joining("\n"));
        boolean anyMatch = responseMessage.getResponses().stream().anyMatch(response -> "doTerminate".equals(response.name()));
        if (anyMatch) {
            setState(AgentState.FINISHED);
        }
        log.info("tools call result:{}", result);
        return result;
    }
}
