package com.small.mq.client.consumer;

public interface MessageListener {
    /**
     * consume message
     *
     * @param data
     * @return
     * @throws Exception
     */
    public Result consume(String data) throws Exception;
}
