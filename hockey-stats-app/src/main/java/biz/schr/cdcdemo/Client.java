package biz.schr.cdcdemo;

import biz.schr.cdcdemo.util.Constants;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryMergedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

public class Client {

    public static void main(String[] args) throws Exception {
        HazelcastInstance client = HazelcastClient.newHazelcastClient(Config.newClientConfig());

        IMap<Object, Object> map = client.getMap(Constants.TOP_SCORERS_MAP);
        map.addEntryListener((EntryUpdatedListener<Object, Object>) entryEvent -> System.out.println("Entry updated: " + entryEvent), true);
        map.addEntryListener((EntryMergedListener<Object, Object>) entryEvent -> System.out.println("Entry merged: " + entryEvent), true);
        System.out.println(map.values());
        Thread.sleep(Long.MAX_VALUE);
    }

}
