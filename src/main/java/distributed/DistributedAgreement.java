package distributed;

import distributed.server.pojos.Server;
import distributed.server.threads.ServerThread;
import distributed.utils.Utils;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DistributedAgreement
{
    // The weight of this process
    private static double weight;
    private static Logger logger = Logger.getLogger(DistributedAgreement.class);



    private static Server getSelf(int serverId, List<Server> servers)
    {
        for (Server server:servers)
        {
            if(serverId == server.getServerId())
            {
                return server;
            }
        }
        return null;
    }

    public static void main(String[] args)
    {
        int serverId = Integer.parseInt(args[1]);
        logger.debug("Starting serverId: " + serverId);
        List<Server> peers = Utils.getHosts();
        Server host = getSelf(serverId,peers);

        // Spawn off a thread to handle messages from client
        ServerThread serverThread = new ServerThread();
        serverThread.setIpAddress(host.getIpAddress());
        serverThread.setPeers(peers);
        serverThread.setPort(host.getPort());
        new Thread(serverThread).start();
    }



}
