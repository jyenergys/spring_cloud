package com.lvbility.cloud.stream.binder.rocketmq.core;

import org.apache.rocketmq.client.producer.DefaultMQProducer;

import com.lvbility.cloud.stream.binder.rocketmq.outbound.MessageTopicFactory;

/**
 * 消息生成者
 * @author xiaoyulin
 *
 */
public interface ProducerFactory {
	
	DefaultMQProducer createProducer();
	
	MessageTopicFactory getMessageTopicFactory();
	
	default boolean transactionCapable() {
		return false;
	}
}
