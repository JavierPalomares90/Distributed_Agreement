package distributed.utils;

import distributed.server.pojos.Server;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils
{
    private static Logger logger = Logger.getLogger(Utils.class);

    // Load the hosts from the yaml file
    public static List<Server> getHosts(String path)
    {
        logger.debug("Getting hosts from hosts.yaml");
        InputStream inputStream;

        try
        {
            inputStream = new FileInputStream(path);
        }catch (FileNotFoundException e)
        {
            logger.error("Unable to find hosts file",e);
            return null;
        }

        if(inputStream == null)
        {
            logger.error("Unable to load from hosts.yaml");
            return null;
        }
        Yaml yaml = new Yaml(new Constructor(Server.class));
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
