package distributed.server.byzantine.accept;

import distributed.server.paxos.requests.Request;
import distributed.server.pojos.Server;

import java.util.List;

/**
 * Byzantine acceptor
 */
public class ByzAcceptor
{

    public void receiveSafeRequest(String[] tokens)
    {
        /**
         * TODO: Complete impl
         */
    }

    public void broadcastRequest(Request request, List<Server> acceptors)
    {
        // Broadcast the request we received from a proposer to all peers
        /**
         * TODO: Complete impl
         */

    }
}
