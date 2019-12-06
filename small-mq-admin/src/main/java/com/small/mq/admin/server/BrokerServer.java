package com.small.mq.admin.server;

import com.small.mq.client.broker.Broker;
import com.small.mq.client.message.Message;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/6/19 4:22 PM
 */
@Component
public class BrokerServer implements Broker, InitializingBean, DisposableBean {


    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void destroy() throws Exception {

    }


    // ---------------------- broker api ----------------------
    @Override
    public int addMessages(List<Message> messages) {
        return 0;
    }

    @Override
    public List<Message> pullNewMessage(String topic, String group, int consumerRank, int consumerTotal, int pagesize) {
        return null;
    }

    @Override
    public int lockMessage(long id, String appendLog) {
        return 0;
    }

    @Override
    public int callbackMessages(List<Message> messages) {
        return 0;
    }
}
