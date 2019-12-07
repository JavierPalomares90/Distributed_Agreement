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
     
    @Override
    private void sendAcceptRequest(AcceptRequest acceptRequest, List<Server> acceptors)
    {
        sendRequest(acceptRequest, acceptors);
        // Increment the number of accepts for ourself
        this.serverThread.getWeightedAccepts().set(this.serverThread.getWeightedAccepts().get() + this.serverThread.getOwnWeight().get());
    }
    
    @Override
    private void sendPrepareRequest(PrepareRequest prepareRequest, List<Server> acceptors)
    {
        sendRequest(prepareRequest, acceptors);
        // Increment the number of promises for ourself
        this.serverThread.getWeightedPromises().set(this.serverThread.getWeightedPromises().get() + this.serverThread.getOwnWeight().get());
    }

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
