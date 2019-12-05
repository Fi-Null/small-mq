package com.small.mq.client.util;

import com.small.rpc.util.RpcException;

import java.util.concurrent.*;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/5/19 8:40 PM
 */
public class ThreadPoolUtil {

    /**
     * make server thread pool
     *
     * @param serverType
     * @return
     */
    public static ThreadPoolExecutor makeServerThreadPool(final String serverType, int corePoolSize, int maxPoolSize) {
        ThreadPoolExecutor serverHandlerPool = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                r -> new Thread(r, "small-mq, " + serverType + "-serverHandlerPool-" + r.hashCode()),
                (r, executor) -> {
                    throw new RpcException("small-mq " + serverType + " Thread pool is EXHAUSTED!");
                });        // default maxThreads 300, minThreads 60

        return serverHandlerPool;
    }

}
