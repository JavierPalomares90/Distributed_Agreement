package distributed.server.paxos;

import distributed.server.paxos.propose.Proposer;
import distributed.server.pojos.Server;
import distributed.server.threads.ServerThread;
import distributed.utils.Command;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.locks.Lock;

@Data
public class Paxos implements Runnable
{
    private static Logger logger = Logger.getLogger(Paxos.class);

    private String value;
    private List<Server> servers;

    @Setter(AccessLevel.PUBLIC)
    Lock phase1Lock;

    @Setter(AccessLevel.PUBLIC)
    Lock phase2Lock;

    // Start the paxos algorithm to reserve the value
    public String reserveValue(String value, List<Server> servers)
    {
        // Increment the paxos Id
        int id = ServerThread.incrementPaxosId();
        logger.debug("Reserving value " + value + " with id " + id);

        // Phase 1 of Paxos: Propose the value
        Proposer proposer = new Proposer(value);
        proposer.propose(servers);
        // Wait till we get enough promises to move onto phase 2
        synchronized(phase1Lock)
        {
            logger.debug("Waiting for phase 2");
            try
            {
                phase1Lock.wait();
            } catch (InterruptedException e)
            {
                logger.error("Unable to wait for phase 2",e);
            }
        }
        logger.debug("Starting phase 2");
        // Phase 2 of Paxos: Accept the value
        proposer.accept(servers);
        // Wait till we get enough accepts to agree on a value
        logger.debug("Waiting for value agreement");
        synchronized (phase2Lock)
        {
            try
            {
                phase2Lock.wait();
            } catch (InterruptedException e)
            {
                logger.error("Unable to wait for value agreement",e);
            }

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
