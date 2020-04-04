package distributed.malicious.server.byzantine.accept;

import java.util.List;

import org.apache.log4j.Logger;

import distributed.server.byzantine.accept.ByzAcceptor;
import distributed.server.pojos.Server;
import distributed.utils.Command;
import distributed.utils.Value;

/**
 * Malicious impl of a Byzantine Acceptor
 * In order to be a malicious acceptor, we're going to flip the value received in the requests, and broadcast that.
 * We'll also reject any broadcasts from the other acceptors
 */
public class MaliciousByzAcceptor extends ByzAcceptor
{
    private static Logger logger = Logger.getLogger(MaliciousByzAcceptor.class);

    // Maliciously broadcast prepare request
    @Override
    protected boolean broadcastPrepareRequest(int id, String value, List<Server> acceptors, int senderID)
    {
        // We're going to lie about the value we received in the prepare request to confuse the other acceptors
        if(Value.ZERO.getValue().equals(value))
        {
            value = Value.ONE.getValue();
        }else
        {
            value = Value.ZERO.getValue();
        }
        return super.broadcastPrepareRequest(id,value,acceptors,senderID);
    }

    // Maliciously broadcast safe request
    @Override
    protected boolean broadcastSafeRequest(int id, String value, List<Server> acceptors, int senderID)
    {
        // We're going to lie about the value we received in the safe request to confuse the other acceptors
        if(Value.ZERO.getValue().equals(value))
        {
            value = Value.ONE.getValue();
        }else
        {
            value = Value.ZERO.getValue();
        }
        return super.broadcastPrepareRequest(id,value,acceptors,senderID);
    }

    // Maliciously receive safe request. Reject the broadcast
    @Override
    public String receiveSafeBroadcast(String[] tokens)
    {
        return Command.SAFE_BROADCAST_REJECT.getCommand();
    }

    // Maliciously receive prepare request. Reject the broadcast
    @Override
    public String receivePrepareBroadcast(String[] tokens)
    {
        return Command.PREPARE_BROADCAST_REJECT.getCommand();
    }



}