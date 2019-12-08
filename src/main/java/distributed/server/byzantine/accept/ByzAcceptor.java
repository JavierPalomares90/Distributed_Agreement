package distributed.server.byzantine.accept;

import distributed.server.paxos.accept.Acceptor;
import distributed.server.paxos.requests.Request;
import distributed.server.pojos.ProposedValue;
import distributed.server.pojos.SafeValue;
import distributed.server.pojos.Server;
import distributed.utils.Command;
import distributed.utils.Utils;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Byzantine acceptor
 */
public class ByzAcceptor extends Acceptor
{
    @Setter(AccessLevel.PUBLIC)
    protected List<Server> acceptors;

    private static Logger logger = Logger.getLogger(ByzAcceptor.class);

    public String receiveSafeRequest(String[] tokens)
    {
        if (tokens.length < 2)
        {
            return null;
        }
        int id = Integer.parseInt(tokens[1]);
        String value = tokens[2];
        logger.debug("Received safe request with id: " + id + " value: " + value);
        SafeValue safeValue = new SafeValue();
        safeValue.setId(id);
        safeValue.setValue(value);
        // Add the value, mark it as not safe for not
        this.serverThread.addSafeValue(safeValue,false);


        // Broadcast the safe to the rest of the acceptors
        Runnable broadcastSafeRunnable = () ->
        {
            logger.debug("Broadcasting safe request to other acceptors");
            boolean isValueSafe = broadcastSafeRequest(id,value,this.acceptors);

            // Mark the value as safe
            this.serverThread.addSafeValue(safeValue,isValueSafe);
        };
        new Thread(broadcastSafeRunnable).start();
        return null;
    }

    /**
     * TODO: Match the signatures so this method is overriden
     * @param tokens
     * @param sender
     * @return
     */
    public String receivePromiseRequest(String[] tokens, Server sender)
    {
        logger.debug("Received promise request");
        int id = Integer.parseInt(tokens[1]);
        if(id > this.serverThread.getPaxosId().get())
        {
            this.serverThread.getPaxosId().set(id);
        }
        this.serverThread.updatePromisedWeight(sender.getWeight());
        return "Received promise request";
    }

    @Override
    public String receiveAcceptRequest(String[] tokens)
    {
        int id = Integer.parseInt(tokens[1]);
        String value = tokens[2];
        SafeValue safeValue = new SafeValue();
        safeValue.setId(id);
        safeValue.setValue(value);
        // check that the value is safe
        AtomicBoolean isValueSafe = this.serverThread.isValueSafe(safeValue);
        if (isValueSafe == null || isValueSafe.get() == false)
        {
            // Value is not safe. reject the request
            return Command.REJECT_ACCEPT.getCommand() + " " + id + " " + value;
        }
        // Value is safe, follow the paxos algo for receiving accept request
        return super.receiveAcceptRequest(tokens);
    }

    /**
     * TODO: Check if this is the correct input to this method
     * @param sender
     * @return
     */
    public synchronized String receiveAcceptResponse(Server sender)
    {
        this.serverThread.getWeightedAccepts().set(this.serverThread.getWeightedAccepts().get() + sender.getWeight());
        if(this.serverThread.getWeightedAccepts().get() > 5.0/6)
        {
            lock.lock();
            // We've received enough accepts. can agree on a value
            this.phase2Condition.signalAll();
            lock.unlock();
        }
        return "Agreed to value";
    }


    @Override
    public String receivePrepareRequest(String[] tokens)
    {
        if (tokens.length < 3)
        {
            return Command.REJECT_PREPARE.getCommand() + " " + this.serverThread.getPaxosId().get() + " " + this.serverThread.getPaxosValue() + "\n";
        }
        int id = Integer.parseInt(tokens[1]);
        String value = tokens[2];
        logger.debug("Receiving prepare request with id " + id + " value " + value);
        // Compare the id to our current id
        if(id > this.serverThread.getPaxosId().get())
        {
            String valuePreviouslyPromised = updateValues(id,value);

            logger.debug("Promising to request with id " + id + " value " + value);

            // Broadcast the prepare request to the other acceptors
            Runnable broadcastPrepareRunnable = () ->
            {
                boolean broadcastSuccessful = broadcastPrepareRequest(id,value,this.acceptors);
                /**
                 * TODO: reject if the broadcast did not reach a quorum
                 */

            };
            new Thread(broadcastPrepareRunnable).start();


            return Command.PROMISE.getCommand() + " " + this.serverThread.getPaxosId().get() + " " + valuePreviouslyPromised + "\n";
        }
        logger.debug("Rejecting prepare request with id " + id + " value " + value);
        // REJECT the request and send the paxos id and value
        return Command.REJECT_PREPARE.getCommand() + " " + this.serverThread.getPaxosId().get() + " " + this.serverThread.getPaxosValue() + "\n";

    }

    private boolean broadcastPrepareRequest(int id, String value, List<Server> acceptors)
    {
        // Broadcast the request we received from a proposer to all peers
        String cmd = Command.PREPARE_BROADCAST.getCommand() + " " + id + " " + value;
        return broadcastCommand(cmd,acceptors);
    }

    private boolean broadcastSafeRequest(int id, String value, List<Server> acceptors)
    {
        // Broadcast the request we received from a proposer to all peers
        String cmd = Command.SAFE_BROADCAST.getCommand() + " " + id + " " + value;
        return broadcastCommand(cmd,acceptors);
    }

    private static synchronized boolean broadcastCommand(String cmd, List<Server> acceptors)
    {
        return broadcastCommand(cmd,acceptors,0);

    }


    private static synchronized boolean broadcastCommand(String cmd, List<Server> acceptors, int numFaulty)
    {
        int numAccepts = 0;
        int numRejects = 0;
        int numServers = acceptors.size();
        int quorumSize = Utils.getQuorumSize(numServers,numFaulty);
        for(Server acceptor: acceptors)
        {
            String response = Utils.sendTcpMessage(acceptor,cmd, true);
            if(Command.SAFE_BROADCAST_ACCEPT.getCommand().equals(response) || Command.PREPARE_BROADCAST_ACCEPT.getCommand().equals(response))
            {
                numAccepts++;
            }
            else
            {
                numRejects++;
            }
            if(numAccepts >= quorumSize)
            {
                return true;
            }
            if(numRejects > (numServers - quorumSize))
            {
                return false;
            }
        }
        return false;
    }

    public String receiveSafeBroadcast(String[] tokens)
    {
        int id = Integer.parseInt(tokens[1]);
        String value = tokens[2];

        SafeValue safeValue = new SafeValue();
        safeValue.setId(id);
        safeValue.setValue(value);

        // Check that we've receive this request previously
        if(this.serverThread.isValueSafe(safeValue) == null)
        {
            // The safe request that was broadcast doesn't match what we've received
            return Command.SAFE_BROADCAST_REJECT.getCommand();
        }

        return Command.SAFE_BROADCAST_ACCEPT.getCommand();
    }

    public String receivePrepareBroadcast(String[] tokens)
    {
        int id = Integer.parseInt(tokens[1]);
        String value = tokens[2];
        ProposedValue proposedValue = new ProposedValue();
        proposedValue.setId(id);
        proposedValue.setValue(value);
        // Check the id of the broadcast matches what we received
        if(this.serverThread.isValueProposed(proposedValue) == null)
        {
            // The safe request that was broadcast doesn't match what we've received
            return Command.PREPARE_BROADCAST_REJECT.getCommand();
        }

        return Command.PREPARE_BROADCAST_ACCEPT.getCommand();
    }
}
