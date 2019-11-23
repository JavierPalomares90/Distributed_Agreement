package distributed.server.paxos;

import distributed.server.paxos.propose.Proposer;
import distributed.server.pojos.Server;
import distributed.utils.Command;
import lombok.Data;
import org.apache.log4j.Logger;

import java.util.List;

@Data
public class Paxos implements Runnable
{
    private static Logger logger = Logger.getLogger(Paxos.class);

    private String value;
    private List<Server> servers;

    // Start the paxos algorithm to reserve the value
    public synchronized String reserveValue(String value, List<Server> servers)
    {
        logger.debug("Reserving value " + value);

        // Phase 1 of Paxos: Propose the value
        Proposer proposer = new Proposer(value);
        boolean proposalAccepted = proposer.propose(servers);
        if(proposalAccepted == false)
        {
            return Command.REJECT_PREPARE.getCommand();
        }
        // Wait till we get enough promises to move onto phase 2
        logger.debug("Waiting for phase 2");
        try
        {
            this.wait();
        } catch (InterruptedException e)
        {
            logger.error("Unable to wait for phase 2",e);
        }
        logger.debug("Starting phase 2");
        // Phase 2 of Paxos: Accept the value
        boolean accepted = proposer.accept(servers);
        if(accepted == false)
        {
            return Command.REJECT_ACCEPT.getCommand();
        }
        // Wait till we get enough accepts to agree on a value
        logger.debug("Waiting for value agreement");
        try
        {
            this.wait();
        } catch (InterruptedException e)
        {
            logger.error("Unable to wait for value agreement",e);
        }
        logger.debug("Agreed to value");
        // The value is agreed to
        return Command.AGREE.getCommand() + " " + value;
    }

    public void run()
    {
        reserveValue(this.value,this.servers);

    }
}
