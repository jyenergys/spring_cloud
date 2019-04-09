package com.lvbility.cloud.stream.binder.rocketmq;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.context.Lifecycle;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.lvbility.boot.starter.rocketmq.core.RocketmqTemplate;
import com.lvbility.boot.starter.rocketmq.listener.DefaultRocketMQListenerContainer;
import com.lvbility.boot.starter.rocketmq.listener.config.ContainerProperties;
import com.lvbility.cloud.stream.binder.rocketmq.core.DefaultRocketmqProducerFactory;
import com.lvbility.cloud.stream.binder.rocketmq.core.ProducerFactory;
import com.lvbility.cloud.stream.binder.rocketmq.outbound.RocketmqMsgDrivenChannelAdapter;
import com.lvbility.cloud.stream.binder.rocketmq.outbound.RocketmqProducerMessageHandler;
import com.lvbility.cloud.stream.binder.rocketmq.properties.RocketmqBinderConfigurationProperties;
import com.lvbility.cloud.stream.binder.rocketmq.properties.RocketmqConsumerProperties;
import com.lvbility.cloud.stream.binder.rocketmq.properties.RocketmqExtendedBindingProperties;
import com.lvbility.cloud.stream.binder.rocketmq.properties.RocketmqProducerProperties;
import com.lvbility.cloud.stream.binder.rocketmq.provisioning.RocketmqTopicProvisioner;

/**
 * Rocketmq Message Channel Binder
 * @author xiaoyulin
 *
 */
public class RocketmqMessageChannelBinder extends AbstractMessageChannelBinder<ExtendedConsumerProperties<RocketmqConsumerProperties>,
	ExtendedProducerProperties<RocketmqProducerProperties>,RocketmqTopicProvisioner> 
	implements ExtendedPropertiesBinder<MessageChannel, RocketmqConsumerProperties, RocketmqProducerProperties> {
	
	private final RocketmqBinderConfigurationProperties configurationProperties;
	
	private RocketmqExtendedBindingProperties extendedBindingProperties = new RocketmqExtendedBindingProperties();
	
	private DefaultRocketmqProducerFactory producerFactory;

	public RocketmqMessageChannelBinder(RocketmqBinderConfigurationProperties configurationProperties, RocketmqTopicProvisioner provisioningProvider) {
		super(headersToMap(configurationProperties), provisioningProvider);
		this.configurationProperties = configurationProperties;
		//TODO:事务？
		
	}
	
	public RocketmqTopicProvisioner getProvisioningProvider(){
		return this.provisioningProvider;
	}

	private static String[] headersToMap(RocketmqBinderConfigurationProperties configurationProperties) {
		String[] headersToMap;
		if (ObjectUtils.isEmpty(configurationProperties.getHeaders())) {
			headersToMap = BinderHeaders.STANDARD_HEADERS;
		}
		else {
			String[] combinedHeadersToMap = Arrays.copyOfRange(BinderHeaders.STANDARD_HEADERS, 0,
					BinderHeaders.STANDARD_HEADERS.length + configurationProperties.getHeaders().length);
			System.arraycopy(configurationProperties.getHeaders(), 0, combinedHeadersToMap,
					BinderHeaders.STANDARD_HEADERS.length,
					configurationProperties.getHeaders().length);
			headersToMap = combinedHeadersToMap;
		}
		return headersToMap;
	}

	@Override
	protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
			ExtendedProducerProperties<RocketmqProducerProperties> producerProperties, MessageChannel errorChannel)
			throws Exception {
		ProducerFactory producerFB = this.getProducerFactory(null, producerProperties);
		RocketmqTemplate rocketmqTemplate = new RocketmqTemplate(producerFB.createProducer());
		ProducerConfigurationMessageHandler handler = new ProducerConfigurationMessageHandler(rocketmqTemplate,
				destination.getName(), producerProperties, producerFB);
		return handler;
	}
	
	protected DefaultRocketmqProducerFactory getProducerFactory(String transactionIdPrefix,
			ExtendedProducerProperties<RocketmqProducerProperties> producerProperties) {
		if(producerFactory != null){
			return producerFactory;
		}
		Map<String, Object> props = new HashMap<>();
		if (!ObjectUtils.isEmpty(configurationProperties.getProducerConfiguration())) {
			props.putAll(configurationProperties.getProducerConfiguration());
		}
		producerFactory = new DefaultRocketmqProducerFactory(props);
		producerFactory.setMessageTopicFactory(provisioningProvider.getMessageTopicFactory());
		
		return producerFactory;
	}

	@Override
	protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
			ExtendedConsumerProperties<RocketmqConsumerProperties> extendedConsumerProperties) throws Exception {
		boolean anonymous = !StringUtils.hasText(group);
		String consumerGroup = anonymous ? "anonymous." + UUID.randomUUID().toString() : group;
		
		ContainerProperties containerProperties = this.createContainerProperties(destination, consumerGroup, extendedConsumerProperties);
		DefaultRocketMQListenerContainer messageListenerContainer = new DefaultRocketMQListenerContainer(containerProperties);
		final RocketmqMsgDrivenChannelAdapter rocketmqMsgDrivenChannelAdapter = new RocketmqMsgDrivenChannelAdapter(messageListenerContainer);
		rocketmqMsgDrivenChannelAdapter.setBeanFactory(this.getBeanFactory());
		ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, consumerGroup,
				extendedConsumerProperties);
		if (extendedConsumerProperties.getMaxAttempts() > 1) {
//			rocketmqMsgDrivenChannelAdapter.setRetryTemplate(buildRetryTemplate(extendedConsumerProperties));
			rocketmqMsgDrivenChannelAdapter.setRecoveryCallback(errorInfrastructure.getRecoverer());
		}
		else {
			rocketmqMsgDrivenChannelAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());
		}
		return rocketmqMsgDrivenChannelAdapter;
	}
	
	private ContainerProperties createContainerProperties(ConsumerDestination destination, String group,
			ExtendedConsumerProperties<RocketmqConsumerProperties> extendedConsumerProperties){
		boolean anonymous = !StringUtils.hasText(group);
		String consumerGroup = anonymous ? "anonymous." + UUID.randomUUID().toString() : group;
		ContainerProperties containerProperties = new ContainerProperties();
		containerProperties.setConsumerGroup(consumerGroup);
		containerProperties.setNamesrvAddr(configurationProperties.getNamesrvAddr());
		containerProperties.setDestination(destination.getName());
		containerProperties.setConsumeThreadMin(extendedConsumerProperties.getExtension().getConsumeThreadMin());
		containerProperties.setConsumeThreadMax(extendedConsumerProperties.getExtension().getConsumeThreadMax());
		//TODO:待优化
		containerProperties.setTopic(extendedConsumerProperties.getExtension().getTopic());
		if(!StringUtils.isEmpty(extendedConsumerProperties.getExtension().getSelectorExpress())){
			containerProperties.setSelectorExpress(extendedConsumerProperties.getExtension().getSelectorExpress());
		}
		return containerProperties;
	}
	
	public RocketmqExtendedBindingProperties getExtendedBindingProperties() {
		return extendedBindingProperties;
	}

	public void setExtendedBindingProperties(RocketmqExtendedBindingProperties extendedBindingProperties) {
		this.extendedBindingProperties = extendedBindingProperties;
	}

	private final class ProducerConfigurationMessageHandler extends RocketmqProducerMessageHandler implements Lifecycle {
		
		private boolean running = true;
		
		private final ProducerFactory producerFactory;
		
		ProducerConfigurationMessageHandler(RocketmqTemplate rocketmqTemplate, String destination,
				ExtendedProducerProperties<RocketmqProducerProperties> producerProperties, ProducerFactory producerFactory) {
			super(rocketmqTemplate, producerFactory.getMessageTopicFactory());
			setDestinationExpression(new LiteralExpression(destination));
			setBeanFactory(RocketmqMessageChannelBinder.this.getBeanFactory());
			if (producerProperties.getExtension().isSync()) {
				setSync(true);
			}
			this.producerFactory = producerFactory;
		}
		
		@Override
		public void start() {
			try {
				super.onInit();
			}
			catch (Exception e) {
				this.logger.error("Initialization errors: ", e);
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public void stop() {
			if (this.producerFactory instanceof Lifecycle) {
				((Lifecycle) producerFactory).stop();
			}
			this.running = false;
		}
		
		@Override
		public boolean isRunning() {
			return this.running;
		}
		
		}

	@Override
	public RocketmqConsumerProperties getExtendedConsumerProperties(String channelName) {
		return this.extendedBindingProperties.getExtendedConsumerProperties(channelName);
	}

	@Override
	public RocketmqProducerProperties getExtendedProducerProperties(String channelName) {
		return this.extendedBindingProperties.getExtendedProducerProperties(channelName);
	}
		
	}
