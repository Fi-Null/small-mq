package com.small.mq.admin.dao;

import com.small.mq.admin.model.MqTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 10:51 AM
 */
@Mapper
public interface MqTopicDao {

    public List<MqTopic> pageList(@Param("offset") int offset,
                                  @Param("pagesize") int pagesize,
                                  @Param("bizId") int bizId,
                                  @Param("topic") String topic);

    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("bizId") int bizId,
                             @Param("topic") String topic);

    public MqTopic load(@Param("topic") String topic);

    public int add(@Param("MqTopic") MqTopic MqTopic);

    public int update(@Param("MqTopic") MqTopic MqTopic);

    public int delete(@Param("topic") String topic);

    public List<MqTopic> findAlarmByTopic(@Param("topics") List<String> topics);

}
