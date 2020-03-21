package distributed.malicious.threads;

import distributed.server.pojos.Server;
import distributed.server.threads.MessageThread;

public class MaliciousMessageThread extends MessageThread {

    public MaliciousMessageThread(Server sender) {
        super(sender);
    }

    @Override
   // Maliciously propose a value
    protected String proposeValue(String value)
    {
        // TODO: Complete impl
        return null;
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