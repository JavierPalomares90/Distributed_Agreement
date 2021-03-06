package distributed;

import distributed.server.pojos.Server;
import distributed.server.threads.ServerThread;
import distributed.utils.Utils;
import org.apache.log4j.Logger;

import java.util.List;

public class DistributedAgreement
{
    // The weight of this process
    private static double weight;
    private static Logger logger = Logger.getLogger(DistributedAgreement.class);



    protected static Server getSelf(int serverId, List<Server> servers)
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

    protected static void processClientMessages(Server host,List<Server> peers)
    {
        ServerThread serverThread = new ServerThread(host.getWeight(), host.getServerId());
        processClientMessages(host, peers,serverThread);
    }

    protected static void processClientMessages(Server host,List<Server> peers,ServerThread serverThread)
    {
        serverThread.setIpAddress(host.getIpAddress());
        // Remove self from the list of hosts
        peers.remove(host);
        serverThread.setPeers(peers);
        serverThread.setPort(host.getPort());
        serverThread.setServerId(host.getServerId());
        new Thread(serverThread).start();
    }

    public static void main(String[] args)
    {
        if(args.length < 2)
        {
            logger.error("Usage: <hostsFilePath> <serverId>");
            System.exit(-1);
        }
        String hostsFilePath = args[0];
        int serverId = Integer.parseInt(args[1]);
        logger.debug("Starting serverId: " + serverId);
        List<Server> peers = Utils.getHosts(hostsFilePath);
        if(peers == null)
        {
            logger.error("Unable to get hosts");
            System.exit(-1);
        }
        Server host = getSelf(serverId,peers);
        if(host == null)
        {
            logger.error("Uanble to get self host");
            System.exit(-1);
        }

        // Spawn off a thread to handle messages from client
        processClientMessages(host, peers);
    }



}
