package distributed.malicious.server.byzantine.propose;

import java.util.List;

import org.apache.log4j.Logger;

import distributed.server.byzantine.propose.ByzProposer;
import distributed.server.paxos.requests.AcceptRequest;
import distributed.server.paxos.requests.PrepareRequest;
import distributed.server.paxos.requests.Request;

import distributed.server.pojos.Server;

/**
 * Malicious impl of a Byz Proposer
 */
public class MaliciousByzProposer extends ByzProposer
{
    private static Logger logger = Logger.getLogger(MaliciousByzProposer.class);

    // Malicious send requests
    @Override
    protected void sendRequest(Request request, List<Server> acceptors, boolean waitForResponse) throws InterruptedException
    {
        // TODO: Complete impl
    }


    // Malicious send accept request
    @Override
    protected void sendAcceptRequest(AcceptRequest acceptRequest, List<Server> acceptors)
    {
        // TODO: Complete impl
    }

    // Maliciously send prepare request
    @Override
    protected void sendPrepareRequest(PrepareRequest prepareRequest, List<Server> acceptors)
    {
        // TODO: Complete impl
    }

    /**
     * Maliciously parse the responses from the acceptors
     * @param response
     */
    @Override
    protected void parseResponseFromAcceptor(String response, Float weight)
    {
        // TODO: Complete impl
    }

    // Maliciously mark safe request
    @Override
    public boolean safe(List<Server> acceptors)
    {
        return false;
    }



}