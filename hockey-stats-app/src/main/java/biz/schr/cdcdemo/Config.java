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
    public static final String dbURL = "jdbc:mysql://" + dbIP + ":" + dbPort + "/" + dbName;

    public static ClientConfig newClientConfig() {
        ClientConfig config = new ClientConfig();
        config.setProperty(STATISTICS_ENABLED.getName(),"true");
        config.setProperty(HAZELCAST_CLOUD_DISCOVERY_TOKEN.getName(),"gA6VBZaa7u2w5967oWK0uPFIXpf6t3s2kgmUiANib8xrQglpv9");
        config.setProperty("hazelcast.client.cloud.url","https://dev.test.hazelcast.cloud");
        config.setClusterName("de-1818");
        return config;
    }
}
