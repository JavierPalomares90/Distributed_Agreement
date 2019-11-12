package distributed.utils;

import distributed.server.pojos.Server;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class Utils
{
    private static String HOSTS_FILE = "hosts.yaml";

    // Load the hosts from the yaml file
    public static List<Server> getHosts()
    {
        Yaml yaml = new Yaml(new Constructor(Server.class));
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(HOSTS_FILE);
        List<Server> hosts = new ArrayList<Server>();
        for (Object o : yaml.loadAll(inputStream))
        {
            hosts.add( (Server) o);
        }
        return hosts;
    }
}
