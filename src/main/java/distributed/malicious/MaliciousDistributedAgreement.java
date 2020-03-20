package distributed.malicious;

import distributed.DistributedAgreement;
import distributed.server.pojos.Server;
import distributed.server.threads.ServerThread;
import distributed.malicious.threads.MaliciousServerThread;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * Malicious implementation of the Distributed Agreement
 */

public class MaliciousDistributedAgreement extends DistributedAgreement
{
    private static Logger logger = Logger.getLogger(MaliciousDistributedAgreement.class);


    public static void processClientMessages(Server host,List<Server> peers)
    {
        logger.debug("Spawning off a malicious server thread");
        ServerThread serverThread = new MaliciousServerThread(host.getWeight(), host.getServerId());
        processClientMessages(host, peers);
    }

}