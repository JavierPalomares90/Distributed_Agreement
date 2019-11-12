package distributed.client;

import distributed.server.pojos.Server;
import distributed.utils.Utils;
import org.apache.log4j.Logger;

import java.util.List;

public class Client
{
    private static Logger logger = Logger.getLogger(Client.class);

    private static String RESERVE = "reserve";

    public static void main(String[] args)
    {
        List<Server> servers = Utils.getHosts();


    }
}
