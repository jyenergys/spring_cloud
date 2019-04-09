package com.lvbility.cloud.stream.binder.rocketmq.properties;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: LuWei
 * @Date: 2018/12/28 15:31
 */
public class RocketmqBinderConfigurationProperties {
    public boolean isAutoCreateTopics(){

        return false;
    }

    public Map<String, TopicBindingProperties>  getTopics(){
        return null;
    }

    public String getNamesrvAddr() {
        String ip= null;
        try {
            InetAddress address = InetAddress.getLocalHost();
            ip=address .getHostAddress().toString();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }

    public Map<String, Object>  getProducerConfiguration() {
        Map<String, Object> props = new HashMap<>();
        props.put("username","zhangsan");
        props.put("age","30");
        return props;
    }

    public String[] getHeaders(){
        String[] headers ={"Content-Type","application/json"};

        return headers;
    }
}
