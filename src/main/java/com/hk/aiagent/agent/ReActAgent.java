package com.hk.aiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct(Reasoning and Acting)模式的推理代理抽象类
 * @author huangkun
 * @date 2025/6/2 9:19
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    /**
     * 推理方法
     * @return 是否需要调用工具
     */
    public abstract boolean think();

    /**
     * 行动方法
     * @return 行动结果
     */
    public abstract String act();

    /**
     * 步骤方法
     * @return 步骤结果
     */
    @Override
    public String step() {
        try {
            if (think()) {
                 return  act();
            }
            return "No need to call tools";
        } catch (Exception e) {
            log.error("agent think error", e);
            return "agent think error:" + e.getMessage();
        }
    }
}
