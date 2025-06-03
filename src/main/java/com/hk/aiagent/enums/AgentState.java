package com.hk.aiagent.enums;

import lombok.Getter;

/**
 * @author huangkun
 * @date 2025/6/1 19:31
 */
@Getter
/**
 * 代理执行状态的枚举类
 */
public enum AgentState {

    /**
     * 空闲状态
     */
    IDLE,

    /**
     * 运行中状态
     */
    RUNNING,

    /**
     * 已完成状态
     */
    FINISHED,

    /**
     * 错误状态
     */
    ERROR
}

