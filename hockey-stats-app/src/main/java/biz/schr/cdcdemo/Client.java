package biz.schr.cdcdemo;

import biz.schr.cdcdemo.dto.Player;
import biz.schr.cdcdemo.util.Constants;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Observable;

import java.util.List;

public class Client {

    public static void main(String[] args) {
        JetService jet = HazelcastClient.newHazelcastClient(Config.newClientConfig()).getJet();

        Observable<HazelcastJsonValue> observable = jet.getObservable(Constants.TOP_SCORERS_OBSERVABLE);
        observable.addObserver(System.out::println);
    }

}
