package biz.schr.cdcdemo;

import com.hazelcast.client.config.ClientConfig;

import static com.hazelcast.client.properties.ClientProperty.HAZELCAST_CLOUD_DISCOVERY_TOKEN;
import static com.hazelcast.client.properties.ClientProperty.STATISTICS_ENABLED;

public class Config {

    // DB Connection config
    public static final String dbUsername = "dbz";
    public static final String dbPassword = "iddqd";
    public static final String dbName = "ahlcz5";
    public static final String dbIP = "3.69.177.18";
    public static final int dbPort = 3306;
    public static final String dbURL = "jdbc:mysql://" + dbIP + ":" + dbPort + "/" + dbName + "?serverTimezone=UTC";

    public static final boolean USE_CLOUD = true;

    public static ClientConfig newClientConfig() {
        if (USE_CLOUD) {
            ClientConfig config = new ClientConfig();
            config.setProperty(STATISTICS_ENABLED.getName(), "true");
            config.setProperty("hazelcast.client.cloud.url", "https://bumblebee.test.hazelcast.cloud");

            // aws-hockey cluster
//            config.setProperty(HAZELCAST_CLOUD_DISCOVERY_TOKEN.getName(), "ytvNd1p4WpvTVFsmxGvAgEtOXfU6g7PxwDhKLZ9g7bv6FzP76Q");
//            config.setClusterName("bu-1084");

            // aws-hockey-california
//            config.setProperty(HAZELCAST_CLOUD_DISCOVERY_TOKEN.getName(), "Py7KkFGqgqCZ28b2mbmWUKJG9m3OouWnT0Xndn46MFpezUJBgf");
//            config.setClusterName("bu-1102");

            // aws-hockey-london
            config.setProperty(HAZELCAST_CLOUD_DISCOVERY_TOKEN.getName(), "JYvLyGndxG1Gr3bCZareqYXDn01V2S1PMZNJx0bVWm2ykPwEWi");
            config.setClusterName("bu-1103");

            return config;
        } else {
            return new ClientConfig();
        }
    }
}
