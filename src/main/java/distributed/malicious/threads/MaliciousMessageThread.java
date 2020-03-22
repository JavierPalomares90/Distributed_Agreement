package distributed.malicious.threads;

import org.apache.log4j.Logger;

import distributed.malicious.server.byzantine.MaliciousByzPaxos;
import distributed.malicious.server.byzantine.accept.MaliciousByzAcceptor;
import distributed.server.pojos.Server;
import distributed.server.threads.MessageThread;
import distributed.server.byzantine.ByzPaxos;
import distributed.server.byzantine.accept.ByzAcceptor;

public class MaliciousMessageThread extends MessageThread 
{

    private static Logger logger = Logger.getLogger(MaliciousMessageThread.class);
    
    public MaliciousMessageThread(Server sender) {
        super(sender);
    }

    // Maliciously propose a value
    @Override
    protected String proposeValue(String value)
    {
        logger.debug("The client wants us to propose value: " + value);
        logger.debug("We're going to malicious propose this!");
        ByzPaxos maliciousPaxos = new MaliciousByzPaxos();

        return startPaxos(maliciousPaxos,value);
    }

    // Maliciously receive prepare request
    @Override
    public String receivePrepareRequest(String[] tokens)
    {
        ByzAcceptor acceptor = new MaliciousByzAcceptor();
        return receivePrepareRequest(acceptor,tokens);
    }

    // Maliciously receive promise request
    @Override
    public synchronized String receivePromiseRequest(String[] tokens, Server sender)
    {
        ByzAcceptor acceptor = new MaliciousByzAcceptor();
        return receivePromiseRequest(acceptor,tokens,sender);
    }

    // Maliciously receive accept request
    @Override
    public String receiveAcceptRequest(String[] tokens)
    {
        ByzAcceptor acceptor = new MaliciousByzAcceptor();
        return receiveAcceptRequest(acceptor, tokens);
    }

    // Maliciously receive AcceptResponse
    @Override
    public synchronized String receiveAcceptResponse(Server sender)
    {
        ByzAcceptor acceptor = new MaliciousByzAcceptor();
        return receiveAcceptResponse(acceptor, sender);
    }

    @Override
    // Maliciously receive safe request
    protected String receiveSafeRequest(String[] tokens)
    {
        ByzAcceptor acceptor = new MaliciousByzAcceptor();
        return receiveSafeRequest(acceptor, tokens);
    }

    @Override
    // Maliciously receive safe broadcast 
    protected String receiveSafeBroadcast(String[] tokens)
    {
        ByzAcceptor acceptor = new MaliciousByzAcceptor();
        return receiveSafeBroadcast(acceptor, tokens);
    }

    @Override
    // Maliciously receive prepare broadcast 
    protected String receivePrepareBroadcast(String[] tokens)
    {
        ByzAcceptor acceptor = new MaliciousByzAcceptor();
        return receivePrepareBroadcast(acceptor, tokens);
    }
}