import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.function.ComparatorEx;
import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.config.ProcessingGuarantee;
import com.hazelcast.jet.json.JsonUtil;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.ServiceFactories;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hazelcast.function.Functions.wholeItem;

public class TopScorers {

    public static final int RANKING_TABLE_SIZE = 5;

    static JobConfig getJobConfig() {
        JobConfig jc = new JobConfig();
        jc.setProcessingGuarantee(ProcessingGuarantee.EXACTLY_ONCE);
        jc.setName("Top scorers demo data");
        return jc;
    }

    static Pipeline buildPipeline() {
        Pipeline p = Pipeline.create();

        StreamSource<Long> source = PlayerIdSource.playerIdDataSource();

        // Continuously get and parse goal records
        StreamStage<Map.Entry<Long, Long>> updatedGoals = p.readFrom(source)
                   .withoutTimestamps()

                   // Update the stats for the Player
                   .groupingKey(wholeItem())
                   .rollingAggregate(AggregateOperations.counting());

        // Update Player Cache
        updatedGoals.writeTo(updatePlayerCache());

        // Update top scorer ranking and push changes to subscribers
        StreamStage<List<Player>> top5 = updatedGoals
                .rollingAggregate(TopNUnique.topNUnique(RANKING_TABLE_SIZE, ComparatorEx.comparingLong(Map.Entry::getValue)))
                .apply(TopScorers::sendUpdatesOnlyForChanges).apply(TopScorers::lookupPlayers);

        top5
            .map( l -> new HazelcastJsonValue(JsonUtil.toJson(l)))
            .writeTo(Sinks.observable(Starter.TOP_SCORERS_OBSERVABLE));

        top5
              .map( l -> new HazelcastJsonValue(JsonUtil.toJson(l)))
              .writeTo(Sinks.map(Starter.TOP_SCORERS_MAP, l -> 1L, l -> l));

        return p;
    }

    private static Sink<Map.Entry<Long, Long>> updatePlayerCache() {
        return Sinks.mapWithUpdating(
                Starter.PLAYER_CACHE,
                e -> e.getKey(),
                (Player player, Map.Entry<Long, Long> updatedStats) -> {
                    if (player != null && updatedStats != null) {
                        player.setGoals(updatedStats.getValue());
                        return player;
                    }

                    return null;
                }
        );
    }

    private static StreamStage<List<Map.Entry<Long, Long>>> sendUpdatesOnlyForChanges(StreamStage<List<Map.Entry<Long, Long>>> input) {
        return input.filterStateful(
                () -> new Object[1],
                (lastItem, item) -> {
                    if (lastItem[0] == null) {
                        lastItem[0] = item;
                        return true;
                    } else if (lastItem[0].equals(item)) {
                        return false;
                    } else {
                        lastItem[0] = item;
                        return true;
                    }
                }
        );
    }


    private static StreamStage<List<Player>> lookupPlayers(StreamStage<List<Map.Entry<Long, Long>>> input) {
        return input.mapUsingService(ServiceFactories.<Long, Player>iMapService(Starter.PLAYER_CACHE),
                (cache, item) -> item.stream().map(e ->  {
                        Player p = cache.get( e.getKey());
                        p.setGoals(e.getValue());
                        return p;
                    }).collect(Collectors.toList())
        );
    }
}
