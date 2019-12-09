package com.small.mq.admin.service.impl;

import com.small.mq.admin.Result;
import com.small.mq.admin.dao.MqBizDao;
import com.small.mq.admin.dao.MqTopicDao;
import com.small.mq.admin.model.MqBiz;
import com.small.mq.admin.service.MqBizService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/9/19 11:38 AM
 */
@Service
public class MqBizServiceImpl implements MqBizService {
    @Resource
    private MqBizDao mqBizDao;
    @Resource
    private MqTopicDao mqTopicDao;

    @Override
    public List<MqBiz> findAll() {
        return mqBizDao.findAll();
    }

    @Override
    public MqBiz load(int id) {
        return mqBizDao.load(id);
    }

    @Override
    public Result<String> add(MqBiz MqBiz) {

        // valid
        if (MqBiz.getBizName() == null || MqBiz.getBizName().trim().length() == 0) {
            return new Result<>(Result.FAIL_CODE, "业务线名称不可为空");
        }
        if (!(MqBiz.getBizName().trim().length() >= 4 && MqBiz.getBizName().trim().length() <= 64)) {
            return new Result<>(Result.FAIL_CODE, "业务线名称长度非法[2-64]");
        }

        // exist
        List<MqBiz> list = findAll();
        if (list != null) {
            for (MqBiz item : list) {
                if (item.getBizName().equals(MqBiz.getBizName())) {
                    return new Result<>(Result.FAIL_CODE, "业务线名称不可重复");
                }
            }
        }

        int ret = mqBizDao.add(MqBiz);
        return ret > 0 ? Result.SUCCESS : Result.FAIL;
    }

    @Override
    public Result<String> update(MqBiz MqBiz) {

        // valid
        if (MqBiz.getBizName() == null || MqBiz.getBizName().trim().length() == 0) {
            return new Result<>(Result.FAIL_CODE, "业务线名称不可为空");
        }
        if (!(MqBiz.getBizName().trim().length() >= 4 && MqBiz.getBizName().trim().length() <= 64)) {
            return new Result<>(Result.FAIL_CODE, "业务线名称长度非法[2-64]");
        }

        // exist
        List<MqBiz> list = findAll();
        if (list != null) {
            for (MqBiz item : list) {
                if (item.getId() != MqBiz.getId() && item.getBizName().equals(MqBiz.getBizName())) {
                    return new Result<>(Result.FAIL_CODE, "业务线名称不可重复");
                }
            }
        }

        int ret = mqBizDao.update(MqBiz);
        return ret > 0 ? Result.SUCCESS : Result.FAIL;
    }

    @Override
    public Result<String> delete(int id) {

        // valid limit not use
        int count = mqTopicDao.pageListCount(0, 1, id, null);
        if (count > 0) {
            return new Result<>(Result.FAIL_CODE, "禁止删除，该业务线下存在Topic");
        }

        int ret = mqBizDao.delete(id);
        return ret > 0 ? Result.SUCCESS : Result.FAIL;
    }

}
