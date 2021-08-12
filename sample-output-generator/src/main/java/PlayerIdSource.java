import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.pipeline.SourceBuilder;
import com.hazelcast.jet.pipeline.StreamSource;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * Generates random player Ids
 */
public class PlayerIdSource {

    private Set playerIds;
    private long lastEmit;
    private static final long emitInterval = 1_000;
    private static final int itemsPerEmission = 10;

    private Random random;

    private PlayerIdSource(HazelcastInstance hz) {
        playerIds = hz.getMap("players").keySet();
        random = new Random();
    }

    private void fillBuffer(SourceBuilder.TimestampedSourceBuffer<Long> buffer) {

        long now = System.currentTimeMillis();
        if (now < (lastEmit + emitInterval)) {
            return;
        }
        lastEmit = now;


        for (int i = 0; i < itemsPerEmission; i++) {
            int randomNumber = random.nextInt(playerIds.size());

            Iterator iterator = playerIds.iterator();
            Long randomElement;

            int currentIndex = 0;
            while (iterator.hasNext()) {

                randomElement = (Long) iterator.next();

                // if current index is equal to random number
                if (currentIndex == randomNumber) {
                    buffer.add(randomElement, now);
                    break;
                }

                // increase the current index
                currentIndex++;
            }
        }

    }


    public static StreamSource<Long> playerIdDataSource() {
        return SourceBuilder.timestampedStream("Goal Source",
                ctx -> new PlayerIdSource(ctx.hazelcastInstance()))
                            .fillBufferFn(PlayerIdSource::fillBuffer)
                            .build();

    }
}
