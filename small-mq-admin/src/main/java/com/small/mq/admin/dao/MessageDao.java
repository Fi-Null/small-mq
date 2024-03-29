package com.small.mq.admin.dao;

import com.small.mq.client.message.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface MessageDao {
    // ---------------------- admin api ----------------------

    // 精确查询 topic
    public List<Message> pageList(@Param("offset") int offset,
                                  @Param("pagesize") int pagesize,
                                  @Param("topic") String topic,
                                  @Param("status") String status,
                                  @Param("addTimeStart") Date addTimeStart,
                                  @Param("addTimeEnd") Date addTimeEnd);

    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("topic") String topic,
                             @Param("status") String status,
                             @Param("addTimeStart") Date addTimeStart,
                             @Param("addTimeEnd") Date addTimeEnd);

    public int delete(@Param("id") int id);

    public int update(@Param("message") Message message);


    // ---------------------- broker api ----------------------

    public int save(@Param("messageList") List<Message> messageList);

    public List<Message> pullNewMessage(@Param("newStatus") String newStatus,
                                        @Param("topic") String topic,
                                        @Param("group") String group,
                                        @Param("consumerRank") int consumerRank,
                                        @Param("consumerTotal") int consumerTotal,
                                        @Param("pagesize") int pagesize);

    public int lockMessage(@Param("id") long id,
                           @Param("appendLog") String appendLog,
                           @Param("newStatus") String newStatus,
                           @Param("ingStatus") String ingStatus);

    public int updateStatus(@Param("messageList") List<Message> messageList);


    // ---------------------- broker service ----------------------

    /**
     * retry message, retryCount -1 and status from fail to new
     */
    public int updateRetryCount(@Param("failStatus") String failStatus,
                                @Param("newStatus") String newStatus,
                                @Param("appendLog") String appendLog);

    /**
     * clean success message before the days
     */
    public int cleanSuccessMessage(@Param("successStatus") String successStatus, @Param("logretentiondays") int logretentiondays);


    /**
     * find new topic not in topic-table
     */
    public List<String> findNewTopicList();

    /**
     * message info by day
     */
    public List<Map<String, Object>> messageCountByDay(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * clean message
     */
    public int clearMessage(@Param("topic") String topic, @Param("status") String status, @Param("type") int type);

    /**
     * reset block timeout message, reset status from RUNNING to FAIL
     */
    public int resetBlockTimeoutMessage(@Param("ingStatus") String ingStatus, @Param("failStatus") String failStatus, @Param("appendLog") String appendLog);

}
