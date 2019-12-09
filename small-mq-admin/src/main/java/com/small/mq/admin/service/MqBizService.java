package com.small.mq.admin.service;

import com.small.mq.admin.Result;
import com.small.mq.admin.model.MqBiz;

import java.util.List;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 11:40 AM
 */
public interface MqBizService {

    List<MqBiz> findAll();

    MqBiz load(int id);

    Result<String> add(MqBiz MqBiz);

    Result<String> update(MqBiz MqBiz);

    Result<String> delete(int id);
}
