package com.hk.aiagent.agent;

import com.hk.aiagent.common.ErrorCode;
import com.hk.aiagent.enums.AgentState;
import com.hk.aiagent.exception.BusinessException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础代理类
 *
 * @author huangkun
 * @date 2025/6/1 19:18
 */
@Data
@Slf4j
public abstract class BaseAgent {
    /**
     * 名称
     */
    private String name;
    /**
     * 系统提示词
     */
    private String systemPrompt;
    /**
     * 下一步提示词
     */
    private String nextStepPrompt;
    /**
     * 状态
     */
    private AgentState state = AgentState.IDLE;
    /**
     * 最大步数
     */
    private Integer maxStep = 10;
    /**
     * 当前步数
     */
    private Integer currentStep = 0;
    /**
     * LLM 模型
     */
    private ChatClient chatClient;

    /**
     * 会话列表
     */
    private List<Message> messageList = new ArrayList<>();

    public String run(String userPrompt) {
        // 如果当前状态为运行中，则抛出异常
        if (this.state == AgentState.RUNNING) {
            throw new BusinessException(ErrorCode.ERROR_SYSTEM, "current state is running, cannot be started again");
        }
        // 如果用户提示词为空，则抛出异常
        if (StringUtils.isBlank(userPrompt)) {
            throw new BusinessException(ErrorCode.ERROR_SYSTEM, "user prompt cannot be empty");
        }
        // 将状态设置为运行中
        this.state = AgentState.RUNNING;
        // 将用户提示词添加到会话列表中
        messageList.add(new UserMessage(userPrompt));

        List<String> stepResultList = new ArrayList<>();
        try {
            // 循环执行步骤，直到达到最大步数或状态不再为运行中
            while (currentStep < maxStep && this.state == AgentState.RUNNING) {
                currentStep++;
                // 执行步骤，并将结果添加到结果列表中
                String stepResult = step();
                stepResult = "Step " + currentStep + ":" + stepResult;
                stepResultList.add(stepResult);
            }
            // 如果达到最大步数，则将状态设置为已完成，并将结果添加到结果列表中
            if (currentStep >= maxStep) {
                this.state = AgentState.FINISHED;
                stepResultList.add("Terminated: max step reached, agent finished");
            }
            // 将结果列表中的结果拼接成一个字符串并返回
            return stepResultList.stream().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("agent run error:{}", e.getMessage());
            // 如果发生异常，则将状态设置为错误，并返回异常信息
            this.state = AgentState.ERROR;
            return "agent run error:" + e.getMessage();
        }

    }

    public abstract String step();

}
