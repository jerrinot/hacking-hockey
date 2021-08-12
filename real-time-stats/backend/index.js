'use strict';
const { Client } = require('hazelcast-client');

(async () => {
    let client;
    try {
        client = await Client.newHazelcastClient();
        const map = await client.getMap('top_5_map');

        await map.addEntryListener({
            added: (entryEvent) => {
                console.log(entryEvent);
            },
            updated: (entryEvent) => {
                console.log(entryEvent);
            }
        }, undefined, true);

    } catch (err) {
        await client.shutdown();
        console.error('Error occurred:', err);
        process.exit(1);
    }
})();
