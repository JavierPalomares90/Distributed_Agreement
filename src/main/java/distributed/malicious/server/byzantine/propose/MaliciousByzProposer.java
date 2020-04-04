package distributed.malicious.server.byzantine.propose;

import java.util.List;

import org.apache.log4j.Logger;

import distributed.malicious.requests.RandomAcceptRequest;
import distributed.malicious.requests.RandomPrepareRequest;
import distributed.malicious.requests.RandomSafeRequest;
import distributed.server.byzantine.propose.ByzProposer;
import distributed.server.byzantine.requests.SafeRequest;
import distributed.server.paxos.requests.AcceptRequest;
import distributed.server.paxos.requests.PrepareRequest;

import distributed.server.pojos.Server;

/**
 * Malicious impl of a Byz Proposer
 */
public class MaliciousByzProposer extends ByzProposer
{
    private static Logger logger = Logger.getLogger(MaliciousByzProposer.class);

    @Override
    public boolean propose(List<Server> acceptors)
    {
        PrepareRequest prepareRequest = new RandomPrepareRequest();
        return propose(acceptors,prepareRequest);
    }

    // Maliciously mark safe request
    @Override
    public boolean safe(List<Server> acceptors)
    {
        SafeRequest safeRequest = new RandomSafeRequest();
        return safe(acceptors,safeRequest);
    }

    // Maliciously accept values
    @Override
    public boolean accept(List<Server> acceptors)
    {
        AcceptRequest acceptRequest = new RandomAcceptRequest();
        return accept(acceptors,acceptRequest);
    }
}