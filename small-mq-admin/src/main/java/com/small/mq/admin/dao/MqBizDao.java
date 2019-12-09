package com.small.mq.admin.dao;

import com.small.mq.admin.model.MqBiz;
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
public interface MqBizDao {

    public List<MqBiz> findAll();

    public MqBiz load(@Param("id") int id);

    public int add(MqBiz MqBiz);

    public int update(@Param("MqBiz") MqBiz MqBiz);

    public int delete(@Param("id") int id);

}
