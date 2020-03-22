package distributed.malicious.threads;

import org.apache.log4j.Logger;

import distributed.malicious.server.byzantine.MaliciousByzPaxos;
import distributed.server.pojos.Server;
import distributed.server.threads.MessageThread;
import distributed.server.byzantine.ByzPaxos;

public class MaliciousMessageThread extends MessageThread 
{

    private static Logger logger = Logger.getLogger(MaliciousMessageThread.class);
    
    public MaliciousMessageThread(Server sender) {
        super(sender);
    }

    @Override
   // Maliciously propose a value
    protected String proposeValue(String value)
    {
        logger.debug("The client wants us to propose value: " + value);
        logger.debug("We're going to malicious propose this!");
        ByzPaxos maliciousPaxos = new MaliciousByzPaxos();

        return startPaxos(maliciousPaxos,value);
    }

    @Override
    // Maliciously receive prepare request
    public String receivePrepareRequest(String[] tokens)
    {
        // TODO: Complete impl
        return null;
    }

    @Override
    // Maliciously receive promise request
    public synchronized String receivePromiseRequest(String[] tokens, Server sender)
    {
        // TODO: Complete impl
        return null;
    }

    @Override
    // Maliciously receive accept request
    public String receiveAcceptRequest(String[] tokens)
    {
        // TODO: Complete impl
        return null;
    }

    @Override
    // Maliciously receive safe request
    protected String receiveSafeRequest(String[] tokens)
    {
        // TODO: Complete impl
        return null;
    }

    @Override
    // Maliciously receive safe broadcast 
    protected String receiveSafeBroadcast(String[] tokens)
    {
        // TODO: Complete impl
        return null;
    }

    @Override
    // Maliciously receive prepare broadcast 
    protected String receivePrepareBroadcast(String[] tokens)
    {
        // TODO: Complete impl
        return null;
    }
}