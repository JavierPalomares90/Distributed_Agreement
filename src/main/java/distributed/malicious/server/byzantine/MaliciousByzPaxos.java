package distributed.malicious.server.byzantine;

import org.apache.log4j.Logger;

import distributed.malicious.server.byzantine.propose.MaliciousByzProposer;
import distributed.server.byzantine.ByzPaxos;
import distributed.server.byzantine.propose.ByzProposer;
import distributed.server.pojos.Server;
import distributed.utils.Command;

import java.util.List;

public class MaliciousByzPaxos extends ByzPaxos
{
    private static Logger logger = Logger.getLogger(MaliciousByzPaxos.class);

    // Maliciously propose a value
    @Override
    public String proposeValue(String value, List<Server> servers)
    {
        // Update paxosValue
        this.serverThread.setPaxosValue(value);
    
        // Increment the paxos id
        int id =  this.serverThread.getPaxosId().incrementAndGet();

        // Increment the paxos Id
        logger.debug("Proposing value " + value + " with id " + id);

        // Phase 1 of Paxos: Propose the value maliciously
        // To confuse each of the acceptors, we're going to send a request with the same id,
        // but a random value
        logger.debug("Sending malicious proposal requests");
        ByzProposer proposer = new MaliciousByzProposer();
        proposer.setServerThread(this.serverThread);

        proposer.propose(servers);

        // Don't wait till we get enough promise accepted messages. go ahead and tell the acceptors the prepare request is safe
        logger.debug("Sending malicious safe requests");
        proposer.safe(servers);

        logger.debug("Starting phase 2 maliciously");
        // Phase 2 of Paxos: Accept the value maliciously
        proposer.accept(servers);
        logger.debug("We will accept the value");

        return Command.AGREE.getCommand() + " " + value;

    }


}