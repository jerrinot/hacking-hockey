'use strict';
const { Client } = require('hazelcast-client');
const WebSocketServer = require('websocket').server;
const http = require('http');

function originIsAllowed(origin) {
    console.log(origin);
    return origin === process.env.NODE_APP_ORIGIN || origin ===  'http://localhost:8080';
}

async function sendToConnection(connection, data){
    connection.send(data);
}

(async () => {
    let client;
    let connections = new Set();
    let top5Cache;

    try {
        const server = http.createServer(function (request, response) {
            console.log((new Date()) + ' Received request for ' + request.url);
            response.writeHead(404);
            response.end();
        });

        server.listen(3000, function () {
            console.log((new Date()) + ' Server is listening on port 3000');
        });

        const wsServer = new WebSocketServer({
            httpServer: server,
            // You should not use autoAcceptConnections for production
            // applications, as it defeats all standard cross-origin protection
            // facilities built into the protocol and the browser.  You should
            // *always* verify the connection's origin and decide whether or not
            // to accept it.
            autoAcceptConnections: false
        });


        wsServer.on('request', function (request) {
            if (!originIsAllowed(request.origin)) {
                // Make sure we only accept requests from an allowed origin
                request.reject();
                console.log((new Date()) + ' Connection from origin ' + request.origin + ' rejected.');
                return;
            }

            try{
                const connection = request.accept('stats-protocol', request.origin);
                console.log((new Date()) + ' Connection accepted.');

                connections.add(connection);
                console.log(connections.size);
                // send cached data if there is
                if(top5Cache) connection.send(JSON.stringify(top5Cache));

                connection.on('close', function (reasonCode, description) {
                    console.log((new Date()) + ' Peer ' + connection.remoteAddress + ' disconnected.');
                    console.log((new Date()) + ' Reason code: ' + reasonCode + ' description: ' + description);
                    connections.delete(connection);
                });
            } catch (e) {
                console.log((new Date()) + ' Cannot accept connection ' + e);
            }
        });

        client = await Client.newHazelcastClient({
            clusterName: process.env.HOCKEY_NODE_HZ_CLOUD_CLUSTER_NAME,
            network: {
                hazelcastCloud: {
                    discoveryToken: process.env.HOCKEY_NODE_HZ_CLOUD_TOKEN
                }
            },
            properties: {
                'hazelcast.client.cloud.url': 'https://bumblebee.test.hazelcast.cloud/'
            }
        });
        const map = await client.getMap('top_5_map');

        await map.addEntryListener({
            added: async (entryEvent) => {
                top5Cache = {statName: "top5", value: entryEvent.value};

                const jobs = [];
                for (const connection of connections) {
                    jobs.push(sendToConnection(connection, JSON.stringify(top5Cache)));
                }
                await Promise.all(jobs);
                // console.log(entryEvent);
            },
            updated: async (entryEvent) => {
                top5Cache = {statName: "top5", value: entryEvent.value};

                const jobs = [];
                for (const connection of connections) {
                    jobs.push(sendToConnection(connection, JSON.stringify(top5Cache)));
                }
                await Promise.all(jobs);
                // console.log(entryEvent);
            }
        }, undefined, true);

    } catch (err) {
        await client.shutdown();
        console.error('Error occurred:', err);
        process.exit(1);
    }
})();
