package com.lvbility.boot.starter.rocketmq.listener;

/**
 * RocketMq消息处理监听器
 */
public interface RocketMQListener<T> {
    void onMessage(T message);
}
