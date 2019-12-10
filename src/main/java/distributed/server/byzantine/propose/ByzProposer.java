package distributed.server.byzantine.propose;

import distributed.server.byzantine.requests.SafeRequest;
import distributed.server.paxos.propose.Proposer;
import distributed.server.paxos.requests.AcceptRequest;
import distributed.server.paxos.requests.PrepareRequest;
import distributed.server.paxos.requests.Request;
import distributed.server.pojos.Server;
import distributed.utils.Command;
import distributed.utils.Utils;
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
    protected void sendAcceptRequest(AcceptRequest acceptRequest, List<Server> acceptors)
    {
        sendRequest(acceptRequest, acceptors);
        // Increment the number of accepts for ourself
        this.serverThread.getWeightedAccepts().set(this.serverThread.getWeightedAccepts().get() + this.serverThread.getOwnWeight().get());
    }
    
    @Override
    protected void sendPrepareRequest(PrepareRequest prepareRequest, List<Server> acceptors)
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
        safeRequest.setSenderID(this.serverThread.getServerId());
        sendSafeRequest(safeRequest,acceptors);
        return true;
    }

    @Override
    protected void sendRequestToAcceptor(Request request, Server acceptor, boolean waitForResponse)
    {
        logger.debug("Sending request " + request.toString() + " to acceptor" + acceptor);
        String command = request.toString();
        // Send the command over TCP
        String response = Utils.sendTcpMessage(acceptor, command, waitForResponse);
        parseResponseFromAcceptor(response, acceptor.getWeight());

    }

    /**
     * Parse the responses from the acceptors
     * @param response
     */
    private void parseResponseFromAcceptor(String response, Float weight)
    {
        /**
         * TODO: If the request timed out, the input to this message is null, so neither accepts or rejects will update.
         * Does this cause issues?
         */
        logger.debug("Response from acceptor " + response);
        if(response != null)
        {
            String[] tokens = response.split("\\s+");
            if(tokens.length > 1)
            {
                if (Command.PROMISE.getCommand().equals(tokens[0]))
                {
                    // Update the id
                    updateIdAndValue(tokens);
                    // the prepare request was accepted
                    this.serverThread.updatePromisedWeight(weight);

                }else if (Command.ACCEPT.getCommand().equals(tokens[0]))
                {
                    // the accept reqeust was accepted
                    updateId(tokens);
                    this.serverThread.updateAcceptedWeight(weight);
                }else if(Command.REJECT_PREPARE.getCommand().equals(tokens[0]))
                {
                    // the prepare request was rejected
                    updateIdAndValue(tokens);
                    this.serverThread.updateWeightPromisesRejected(weight);

                }else if(Command.REJECT_ACCEPT.getCommand().equals(tokens[0]))
                {
                    // The accept request was rejected
                    updateId(tokens);
                    this.serverThread.updateWeightAcceptsRejected(weight);
                }
            }

        }
    }

}
