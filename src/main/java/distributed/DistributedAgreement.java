package distributed;

import distributed.server.pojos.Server;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DistributedAgreement
{
    // The weight of this process
    private double weight;
    private static Logger logger = Logger.getLogger(DistributedAgreement.class);
    private static String ipAddress;
    private static int port;
    private static List<Server> hosts;

    private static String HOSTS_FILE = "hosts.yaml";

    // Load the hosts from the yaml file
    private static List<Server> getHosts()
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

    public static void main(String[] args)
    {
        hosts = getHosts();
        // TODO: Parse messages from client


    }



}
