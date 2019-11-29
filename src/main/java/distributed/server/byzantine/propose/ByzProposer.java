package distributed.server.byzantine.propose;

import distributed.server.byzantine.requests.SafeRequest;
import distributed.server.paxos.propose.Proposer;
import distributed.server.pojos.Server;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

import java.util.List;

/**
 * Byzantine proposer
 */
public class ByzProposer extends Proposer
{
    private static Logger logger = Logger.getLogger(ByzProposer.class);

    private void sendSafeRequest(SafeRequest safeRequest, List<Server> acceptors)
    {
        sendRequest(safeRequest,acceptors,false);
    }

    /**
     * Send the safe request to the acceptors to let them know a value is safe
     * @param acceptors
     * @return
     */

    public boolean safe(List<Server> acceptors)
    {
        int id = this.serverThread.getPaxosId().get();
        String value =  this.serverThread.getPaxosValue();
        logger.debug("Sending safe for value " + value);
        SafeRequest safeRequest = new SafeRequest();
        safeRequest.setId(id);
        safeRequest.setValue(value);
        sendSafeRequest(safeRequest,acceptors);
        return true;
    }

}
