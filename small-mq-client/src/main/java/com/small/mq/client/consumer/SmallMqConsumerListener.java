package com.small.mq.client.consumer;

public interface SmallMqConsumerListener {
    /**
     * consume message
     *
     * @param data
     * @return
     * @throws Exception
     */
    public SmallMqResult consume(String data) throws Exception;
}
