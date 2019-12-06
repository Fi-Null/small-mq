package com.small.mq.client.factory;

import com.small.mq.client.consumer.MessageListener;
import com.small.mq.client.consumer.annotation.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/5/19 3:23 PM
 */
public class SmallMqSpringClientFactory implements ApplicationContextAware, DisposableBean {

    private final static Logger logger = LoggerFactory.getLogger(SmallMqClientFactory.class);

    // ---------------------- param  ----------------------

    private String adminAddress;

    private String accessToken;

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    // smallMqClientFactory
    private SmallMqClientFactory smallMqClientFactory;


    @Override
    public void destroy() throws Exception {
        smallMqClientFactory.destroy();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // load consumer from spring
        List<MessageListener> consumerList = new ArrayList<>();

        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(Consumer.class);
        if (serviceMap != null && serviceMap.size() > 0) {
            for (Object serviceBean : serviceMap.values()) {
                if (serviceBean instanceof MessageListener) {
                    consumerList.add((MessageListener) serviceBean);
                }
            }
        }

        // init
        smallMqClientFactory = new SmallMqClientFactory();

        smallMqClientFactory.setAdminAddress(adminAddress);
        smallMqClientFactory.setAccessToken(accessToken);
        smallMqClientFactory.setConsumerList(consumerList);

        try {
            smallMqClientFactory.init();
        } catch (Exception e) {
            logger.error(">>>>>>>>>>> small-mq init failed.cause by: ", e);
        }
    }
}
