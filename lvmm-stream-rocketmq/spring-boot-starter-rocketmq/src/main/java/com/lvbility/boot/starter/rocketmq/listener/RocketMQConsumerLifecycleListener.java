package com.lvbility.boot.starter.rocketmq.listener;

/**
 * RocketMQ Consumer Lifecycle Listener.
 */
public interface RocketMQConsumerLifecycleListener<T> {
    void prepareStart(final T consumer);
}
