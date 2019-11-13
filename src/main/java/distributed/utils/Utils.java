package distributed.utils;

import distributed.server.pojos.Server;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils
{
    private static AtomicInteger id = new AtomicInteger(0);
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

    public static int getId()
    {
        int value = id.get();
        id.getAndIncrement();
        return value;
    }

    public static synchronized void setId(int value)
    {
        // id value can only be increased
        int currValue = id.get();
        if(currValue > value)
        {
            id.getAndSet(value);
        }

    }
}
