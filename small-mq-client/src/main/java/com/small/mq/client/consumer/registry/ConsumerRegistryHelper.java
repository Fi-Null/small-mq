package com.small.mq.client.consumer.registry;

import com.small.mq.client.consumer.ConsumerThread;
import com.small.mq.client.consumer.TopicUtil;
import com.small.mq.client.consumer.annotation.Consumer;
import com.small.registry.client.model.RegistryDataParamVO;
import com.small.rpc.registry.smallregistry.SmallRegistryServiceRegistry;

import java.util.*;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/5/19 5:39 PM
 */
public class ConsumerRegistryHelper {

    private SmallRegistryServiceRegistry serviceRegistry;

    public ConsumerRegistryHelper(SmallRegistryServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * consumer registry
     *
     * @param consumerThreadList
     */
    public void registerConsumer(List<ConsumerThread> consumerThreadList) {

        List<RegistryDataParamVO> registryParamList = new ArrayList<>();
        Set<String> registryParamKeyList = new HashSet<>();

        for (ConsumerThread consumerThread : consumerThreadList) {
            String registryKey = TopicUtil.makeRegistryKey(consumerThread.getMqConsumer().topic());
            String registryVal = TopicUtil.makeRegistryVal(consumerThread.getMqConsumer().group(), consumerThread.getUuid());

            registryParamList.add(new RegistryDataParamVO(registryKey, registryVal));
            registryParamKeyList.add(registryKey);
        }

        // registry mult consumer
        serviceRegistry.getRegistryClient().registry(registryParamList);

        // discovery mult consumer
        serviceRegistry.getRegistryClient().discovery(registryParamKeyList);
    }

    /**
     * consumer registry remove
     */
    public void removeConsumer(List<ConsumerThread> consumerThreadList) {
        List<RegistryDataParamVO> registryParamList = new ArrayList<>();
        for (ConsumerThread consumerThread : consumerThreadList) {
            String registryKey = TopicUtil.makeRegistryKey(consumerThread.getMqConsumer().topic());
            String registryVal = TopicUtil.makeRegistryVal(consumerThread.getMqConsumer().group(), consumerThread.getUuid());
            registryParamList.add(new RegistryDataParamVO(registryKey, registryVal));
        }

        serviceRegistry.getRegistryClient().remove(registryParamList);
    }

    /**
     * get total group list
     */
    public Set<String> getTotalGroupList(String topic) {
        // init data
        String registryKey = TopicUtil.makeRegistryKey(topic);


        // load all consumer, find all groups
        Set<String> groupSet = new HashSet<>();
        TreeSet<String> onlineConsumerRegistryValList = serviceRegistry.discovery(registryKey);

        if (onlineConsumerRegistryValList != null && onlineConsumerRegistryValList.size() > 0) {
            for (String onlineConsumerRegistryValItem : onlineConsumerRegistryValList) {
                String groupItem = TopicUtil.parseGroupFromRegistryVal(onlineConsumerRegistryValItem);
                if (groupItem != null && groupItem.length() > 1) {
                    groupSet.add(groupItem);
                }
            }
        }

        if (!groupSet.contains(Consumer.DEFAULT_GROUP)) {
            groupSet.add(Consumer.DEFAULT_GROUP);
        }
        return groupSet;
    }

    /**
     * isActice
     *
     * @param consumerThread
     * @return
     */
    public ActiveInfo isActice(ConsumerThread consumerThread) {
        // init data
        String registryKey = TopicUtil.makeRegistryKey(consumerThread.getMqConsumer().topic());
        String registryValPrefix = TopicUtil.makeRegistryValPrefix(consumerThread.getMqConsumer().group());
        String registryVal = TopicUtil.makeRegistryVal(consumerThread.getMqConsumer().group(), consumerThread.getUuid());

        // load all consumer
        TreeSet<String> onlineConsumerSet = serviceRegistry.discovery(registryKey);
        if (onlineConsumerSet == null || onlineConsumerSet.size() == 0) {
            return null;
        }

        // filter by group
        TreeSet<String> onlineConsumerSet_group = new TreeSet<>();
        for (String onlineConsumerItem : onlineConsumerSet) {
            if (onlineConsumerItem.startsWith(registryValPrefix)) {
                onlineConsumerSet_group.add(onlineConsumerItem);
            }
        }
        if (onlineConsumerSet_group == null || onlineConsumerSet_group.size() == 0) {
            return null;
        }

        // rank
        int rank = -1;
        for (String onlineConsumerItem : onlineConsumerSet_group) {
            rank++;
            if (onlineConsumerItem.equals(registryVal)) {
                break;
            }
        }
        if (rank == -1) {
            return null;
        }

        return new ActiveInfo(rank, onlineConsumerSet_group.size(), onlineConsumerSet_group.toString());
    }


    public static class ActiveInfo {
        // consumer rank
        public int rank;
        // alive num
        public int total;
        // registry rank info
        public String registryRankInfo;

        public ActiveInfo(int rank, int total, String registryRankInfo) {
            this.rank = rank;
            this.total = total;
            this.registryRankInfo = registryRankInfo;
        }

        @Override
        public String toString() {
            return "ActiveInfo{" +
                    "rank=" + rank +
                    ", total=" + total +
                    ", registryRankInfo='" + registryRankInfo + '\'' +
                    '}';
        }
    }

}
