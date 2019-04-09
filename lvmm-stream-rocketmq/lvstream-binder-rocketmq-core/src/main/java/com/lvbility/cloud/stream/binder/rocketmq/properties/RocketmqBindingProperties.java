package com.lvbility.cloud.stream.binder.rocketmq.properties;

public class RocketmqBindingProperties {
	
	private RocketmqConsumerProperties consumer = new RocketmqConsumerProperties();

	private RocketmqProducerProperties producer = new RocketmqProducerProperties();

	public RocketmqConsumerProperties getConsumer() {
		return consumer;
	}

	public void setConsumer(RocketmqConsumerProperties consumer) {
		this.consumer = consumer;
	}

	public RocketmqProducerProperties getProducer() {
		return producer;
	}

	public void setProducer(RocketmqProducerProperties producer) {
		this.producer = producer;
	}
	
}
