package com.small.mq.client.broker;

import com.small.mq.client.message.Message;

import java.util.List;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/5/19 3:15 PM
 */
public interface Broker {

    /**
     * 新增消息，批量
     *
     * @param messages
     * @return
     */
    public int addMessages(List<Message> messages);

    /**
     * 分片数据，批量： MOD( "分片ID", #{consumerTotal}) = #{consumerRank}, 值 consumerTotal>1 时生效
     */
    public List<Message> pullNewMessage(String topic, String group, int consumerRank, int consumerTotal, int pagesize);

    /**
     *  锁定消息，单个；SmallMqMessageStatus：NEW >>> RUNNING
     *
     *  @param id
     *  @param appendLog
     *  @return
     */
    public int lockMessage(long id, String appendLog);

    /**
     *  回调消息，批量；SmallMqMessageStatus：RUNNING >>> SUCCESS/FAIL
     *
     * @param messages
     * @return
     */
    public int callbackMessages(List<Message> messages);

}
