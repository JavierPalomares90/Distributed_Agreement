package distributed.server.byzantine.accept;

import distributed.server.paxos.accept.Acceptor;
import distributed.server.paxos.requests.Request;
import distributed.server.pojos.Server;
import distributed.utils.Command;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Byzantine acceptor
 */
public class ByzAcceptor extends Acceptor
{
    @Setter(AccessLevel.PUBLIC)
    protected List<Server> acceptors;

    private static Logger logger = Logger.getLogger(ByzAcceptor.class);

    public void receiveSafeRequest(String[] tokens)
    {
        if (tokens.length < 2)
        {
            return;
        }
        int id = Integer.parseInt(tokens[1]);
        String value = tokens[2];

        // Broadcast the safe to the rest of the acceptors
        Runnable broadcastSafeRunnable = () ->
        {
            broadcastSafeRequest(id,value,this.acceptors);
        };
        new Thread(broadcastSafeRunnable).start();

        /**
         * TODO: Complete impl
         */
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
                broadcastPrepareRequest(id,value,this.acceptors);
            };
            new Thread(broadcastPrepareRunnable).start();


            return Command.PROMISE.getCommand() + " " + this.serverThread.getPaxosId().get() + " " + valuePreviouslyPromised + "\n";
        }
        logger.debug("Rejecting prepare request with id " + id + " value " + value);
        // REJECT the request and send the paxos id and value
        return Command.REJECT_PREPARE.getCommand() + " " + this.serverThread.getPaxosId().get() + " " + this.serverThread.getPaxosValue() + "\n";

    }

    private void broadcastPrepareRequest(int id, String value, List<Server> acceptors)
    {
        // Broadcast the request we received from a proposer to all peers
        String cmd = Command.PREPARE_BROADCAST.getCommand() + " " + id + " " + value;
        broadcastCommand(cmd,acceptors);
    }

    private void broadcastSafeRequest(int id, String value, List<Server> acceptors)
    {
        // Broadcast the request we received from a proposer to all peers
        String cmd = Command.SAFE_BROADCAST.getCommand() + " " + id + " " + value;
        broadcastCommand(cmd,acceptors);
    }


    private void broadcastCommand(String cmd, List<Server> acceptors)
    {
        for(Server acceptor: acceptors)
        {

        }

    }
}
