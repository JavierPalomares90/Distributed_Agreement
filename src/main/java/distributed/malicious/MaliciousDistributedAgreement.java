package distributed.malicious;

import distributed.DistributedAgreement;
import distributed.server.pojos.Server;
import distributed.server.threads.ServerThread;
import distributed.malicious.threads.MaliciousServerThread;
import distributed.utils.Utils;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * Malicious implementation of the Distributed Agreement
 */

public class MaliciousDistributedAgreement extends DistributedAgreement
{
    private static Logger logger = Logger.getLogger(MaliciousDistributedAgreement.class);


    protected static void processClientMessages(Server host,List<Server> peers)
    {
        logger.debug("Spawning off a malicious server thread");
        ServerThread serverThread = new MaliciousServerThread(host.getWeight(), host.getServerId());
        processClientMessages(host, peers, serverThread);
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
            logger.error("Unable to get self host");
            System.exit(-1);
        }

        // Spawn off a thread to handle messages from client
        processClientMessages(host, peers);
    }

}