package distributed.malicious.server.byzantine.accept;

import java.util.List;

import org.apache.log4j.Logger;

import distributed.server.byzantine.accept.ByzAcceptor;
import distributed.server.pojos.Server;

/**
 * Malicious impl of a Byzantine Acceptor
 */
public class MaliciousByzAcceptor extends ByzAcceptor
{
    private static Logger logger = Logger.getLogger(MaliciousByzAcceptor.class);

    // Maliciously receive safe request
    @Override
    public String receiveSafeRequest(String[] tokens)
    {
        // TODO: Complete impl
        return null;
    }

    // Maliciously receive promise request
    @Override
    public String receivePromiseRequest(String[] tokens, Server sender)
    {
        // TODO: Complete impl
        return null;
    }

    // Maliciously receive accept request
    @Override
    public String receiveAcceptRequest(String[] tokens)
    {
        // TODO: Complete impl
        return null;
    }


    // Maliciously receive accept request
    @Override
    public synchronized String receiveAcceptResponse(Server sender)
    {
        // TODO: Complete impl
        return null;
    }

    // Maliciously receive prepare request
    @Override
    public String receivePrepareRequest(String[] tokens)
    {
        // TODO: Complete impl
        return null;
    }

    // Maliciously broadcast prepare request
    @Override
    protected boolean broadcastPrepareRequest(int id, String value, List<Server> acceptors, int senderID)
    {
        // TODO: Complete impl
        return false;
    }

    // Maliciously broadcast safe request
    @Override
    protected boolean broadcastSafeRequest(int id, String value, List<Server> acceptors, int senderID)
    {
        // TODO: Complete impl
        return false;
    }

    // Maliciously broadcast commands
    @Override
    protected synchronized boolean broadcastCommand(String cmd, List<Server> acceptors, int senderID, int numFaulty) throws InterruptedException
    {
        // TODO: Complete impl
        return false;
    }

    // Maliciously receive safe request
    @Override
    public String receiveSafeBroadcast(String[] tokens)
    {
        // TODO: Complete impl
        return null;
    }

    // Maliciously receive prepare request
    @Override
    public String receivePrepareBroadcast(String[] tokens)
    {
        // TODO: Complete impl
        return null;
    }



}