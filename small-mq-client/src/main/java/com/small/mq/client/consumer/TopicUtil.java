package com.small.mq.client.consumer;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/5/19 6:03 PM
 */
public class TopicUtil {
    private static final String SpaceMark = "_consumer_";

    public static String makeRegistryKey(String topic) {
        String registryKey = SpaceMark.concat(topic);        // _consumer_{topic01}
        return registryKey;
    }

    public static String makeRegistryValPrefix(String group) {
        String registryValPrefix = group.concat(SpaceMark);   // {group01}_consumer_***
        return registryValPrefix;
    }

    public static String makeRegistryVal(String group, String consumerUuid) {
        String registryValPrefix = makeRegistryValPrefix(group);
        String registryVal = registryValPrefix.concat(consumerUuid);  // {group01}_consumer_{uuid}
        return registryVal;
    }

    public static String parseGroupFromRegistryVal(String registryVal) {
        String[] onlineConsumerItemArr = registryVal.split(SpaceMark);
        if (onlineConsumerItemArr != null && onlineConsumerItemArr.length > 1) {
            String group = onlineConsumerItemArr[0];
            return group;
        }
        return null;
    }
}
