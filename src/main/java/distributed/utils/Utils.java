package distributed.utils;

import distributed.server.pojos.Server;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils
{
    private static Logger logger = Logger.getLogger(Utils.class);
    private static String HOSTS_FILE = "hosts.yaml";

    // Load the hosts from the yaml file
    public static List<Server> getHosts()
    {
        logger.debug("Getting hosts from hosts.yaml");
        Yaml yaml = new Yaml(new Constructor(Server.class));
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(HOSTS_FILE);
        if(inputStream == null)
        {
            logger.error("Unable to load from hosts.yaml");
            return null;
        }
        List<Server> hosts = new ArrayList<Server>();
        for (Object o : yaml.loadAll(inputStream))
        {
            hosts.add( (Server) o);
        }
        try
        {
            inputStream.close();
        }catch (IOException e)
        {

            logger.error("Unable to close input stream",e);
        }
        return hosts;
    }
}
