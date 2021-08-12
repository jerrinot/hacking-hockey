import com.hazelcast.config.Config;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.jet.Observable;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryUpdatedListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;

public class Starter {

    public static final String PLAYER_CACHE = "players";
    public static final String TOP_SCORERS_OBSERVABLE = "top_5_observable";
    public static final String TOP_SCORERS_MAP = "top_5_map";

    public static void main(String[] args) {

        try {

            Config cnf = Config.loadDefault();
            cnf.getJetConfig().setEnabled(true);

            cnf.getNetworkConfig().getJoin().getAutoDetectionConfig().setEnabled(false);

            HazelcastInstance hz = Hazelcast.newHazelcastInstance(cnf);

            IMap<Long,Player> players = hz.getMap(PLAYER_CACHE);
            players.putAll(loadSymbols());

            hz.getMap(TOP_SCORERS_MAP).addEntryListener( new MyEntryListener() ,  true);

            // Observable<HazelcastJsonValue> observable = hz.getJet().getObservable(TOP_SCORERS_OBSERVABLE);
            // observable.addObserver(System.out::println);

            hz.getJet().newJob(TopScorers.buildPipeline(), TopScorers.getJobConfig()).join();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Hazelcast.shutdownAll();
        }

    }

    public static Map<Long, Player> loadSymbols() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Starter.class.getResourceAsStream("/players.csv"), UTF_8))
        ) {
            return reader
                    .lines()
                    .map(l -> l.split("\\,"))
                    .collect(toMap( s -> Long.valueOf(s[0]),
                        s -> new Player(Long.valueOf(s[0]),s[1],s[2])));
        }
    }

    static class MyEntryListener implements EntryUpdatedListener<Long, HazelcastJsonValue> {

        @Override
        public void entryUpdated(EntryEvent<Long, HazelcastJsonValue> event) {

            System.out.println("Entry Updated:" + event.getValue() + ", type: " + event.getValue().getClass().getName());
        }
    }
}
