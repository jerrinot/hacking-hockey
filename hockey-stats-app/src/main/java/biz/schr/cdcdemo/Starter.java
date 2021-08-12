package biz.schr.cdcdemo;

import biz.schr.cdcdemo.dto.Player;
import biz.schr.cdcdemo.loader.PlayerMapLoader;
import biz.schr.cdcdemo.loader.RosterMapLoader;
import biz.schr.cdcdemo.util.Constants;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import static com.hazelcast.config.MapStoreConfig.InitialLoadMode.EAGER;

public class Starter {

    public static void main(String[] args) {

        // Start Jet cluster member
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        submitMapConfigs(hz);

        // Trigger loaders to eagerly populate lookup tables from the database
        preloadLookupCaches(hz);

        // Index the cached collections for ad-hoc querying
        createIndexes(hz);

        // Start the streaming job
        hz.getJet().newJobIfAbsent(TopScorers.buildPipeline(), TopScorers.getJobConfig());

    }

    private static void submitMapConfigs(HazelcastInstance instance) {
        // Set read-through backend for Roster-Player mapping cache
        MapStoreConfig rosterPlayerCacheConfig = new MapStoreConfig();
        rosterPlayerCacheConfig.setClassName(RosterMapLoader.class.getName())
                .setInitialLoadMode(EAGER)
                .setEnabled(true);
        MapConfig rosterCacheConfig = new MapConfig(Constants.ROSTER_CACHE);
        rosterCacheConfig.setMapStoreConfig(rosterPlayerCacheConfig);
        instance.getConfig().addMapConfig(rosterCacheConfig);

        // Set read-through backend for Roster-Player mapping cache
        MapStoreConfig playerDetailsCacheConfig = new MapStoreConfig();
        playerDetailsCacheConfig.setClassName(PlayerMapLoader.class.getName())
                .setInitialLoadMode(EAGER)
                .setEnabled(true);
        MapConfig playerCacheConfig = new MapConfig(Constants.PLAYER_CACHE);
        playerCacheConfig.setMapStoreConfig(playerDetailsCacheConfig);
        instance.getConfig().addMapConfig(playerCacheConfig);
    }

    private static void preloadLookupCaches(HazelcastInstance hz) {
        System.out.println("Loading lookup data to cluster cache..");

        // Trigger map loaders to eagerly populate maps from the database
        IMap<Long,Long> roster = hz.getMap(Constants.ROSTER_CACHE);
        IMap<Long, Player> player = hz.getMap(Constants.PLAYER_CACHE);

        System.out.println("Roster->Player mapping lookup table size: " + roster.size());
        System.out.println("Player Details cache size: " + player.size());
    }

    private static void createIndexes(HazelcastInstance hz) {
        // ordered, since we have ranged queries for this field
        hz.getMap(Constants.PLAYER_CACHE).addIndex(new IndexConfig(IndexType.SORTED, "goals"));
    }
}