package distributed.server.accept;

import distributed.server.threads.ServerThread;
import distributed.utils.Command;
import org.apache.log4j.Logger;

public class Acceptor
{
    private static Logger logger = Logger.getLogger(Acceptor.class);

    public static String receivePrepareRequest(String[] tokens)
    {
        if (tokens.length < 3)
        {
            return Command.REJECT_PREPARE.getCommand() + " " + ServerThread.getPaxosId() + " " + ServerThread.getPaxosValue();
        }
        int id = Integer.parseInt(tokens[1]);
        String value = tokens[2];
        logger.debug("Receiving prepare request with id " + id + " value " + value);
        // Compare the id to our current id
        if(id > ServerThread.getPaxosId())
        {
            // Update the new paxosId and accept the request
            ServerThread.setPaxosId(id);
            ServerThread.setPaxosValue(value);

            logger.debug("Promising to request with id " + id + " value " + value);
            return Command.PROMISE.getCommand() + " " + ServerThread.getPaxosId() + " " + ServerThread.getPaxosValue();
        }
        logger.debug("Rejecting prepare request with id " + id + " value " + value);
        // REJECT the request and send the paxos id and value
        return Command.REJECT_PREPARE.getCommand() + " " + ServerThread.getPaxosId() + " " + ServerThread.getPaxosValue();

    }


}
