package com.lvbility.cloud.stream.binder.rocketmq.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.ExtendedBindingProperties;

@ConfigurationProperties("lvstream.rocketmq")
public class RocketmqExtendedBindingProperties 
	implements ExtendedBindingProperties<RocketmqConsumerProperties, RocketmqProducerProperties> {
	
	private Map<String, RocketmqBindingProperties> bindings = new HashMap<>();

	@Override
	public RocketmqConsumerProperties getExtendedConsumerProperties(String channelName) {
		if (bindings.containsKey(channelName)) {
			if (bindings.get(channelName).getConsumer() != null) {
				return bindings.get(channelName).getConsumer();
			}
			else {
				RocketmqConsumerProperties properties = new RocketmqConsumerProperties();
				this.bindings.get(channelName).setConsumer(properties);
				return properties;
			}
		}
		else {
			RocketmqConsumerProperties properties = new RocketmqConsumerProperties();
			RocketmqBindingProperties rbp = new RocketmqBindingProperties();
			rbp.setConsumer(properties);
			bindings.put(channelName, rbp);
			return properties;
		}
	}

	@Override
	public RocketmqProducerProperties getExtendedProducerProperties(String channelName) {
		if (bindings.containsKey(channelName)) {
			if (bindings.get(channelName).getProducer() != null) {
				return bindings.get(channelName).getProducer();
			}
			else {
				RocketmqProducerProperties properties = new RocketmqProducerProperties();
				this.bindings.get(channelName).setProducer(properties);
				return properties;
			}
		}
		else {
			RocketmqProducerProperties properties = new RocketmqProducerProperties();
			RocketmqBindingProperties rbp = new RocketmqBindingProperties();
			rbp.setProducer(properties);
			bindings.put(channelName, rbp);
			return properties;
		}
	}

	public Map<String, RocketmqBindingProperties> getBindings() {
		return bindings;
	}

	public void setBindings(Map<String, RocketmqBindingProperties> bindings) {
		this.bindings = bindings;
	}

}
