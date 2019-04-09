package com.lvbility.cloud.stream.binder.rocketmq.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.lvbility.cloud.stream.binder.rocketmq.RocketmqMessageChannelBinder;
import com.lvbility.cloud.stream.binder.rocketmq.properties.RocketmqBinderConfigurationProperties;
import com.lvbility.cloud.stream.binder.rocketmq.properties.RocketmqExtendedBindingProperties;
import com.lvbility.cloud.stream.binder.rocketmq.provisioning.RocketmqTopicProvisioner;

@Configuration
@ConditionalOnMissingBean(Binder.class)
@EnableConfigurationProperties({ RocketmqExtendedBindingProperties.class })
public class RocketmqBinderConfiguration {
	
	@Autowired
	private RocketmqExtendedBindingProperties rocketmqExtendedBindingProperties;

	@Bean
	RocketmqBinderConfigurationProperties configurationProperties() {
		return new RocketmqBinderConfigurationProperties();
	}
	
	@Bean
	RocketmqTopicProvisioner provisioningProvider(RocketmqBinderConfigurationProperties configurationProperties) {
		return new RocketmqTopicProvisioner(configurationProperties);
	}
	
	@Bean
	RocketmqMessageChannelBinder RocketmqMessageChannelBinder(RocketmqBinderConfigurationProperties configurationProperties,
			RocketmqTopicProvisioner provisioningProvider) {

		RocketmqMessageChannelBinder rocketmqMessageChannelBinder = new RocketmqMessageChannelBinder(
				configurationProperties, provisioningProvider);
		rocketmqMessageChannelBinder.setExtendedBindingProperties(this.rocketmqExtendedBindingProperties);
		
		return rocketmqMessageChannelBinder;
	}
	

}
