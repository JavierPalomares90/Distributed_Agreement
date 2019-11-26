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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

@Data
public class Paxos implements Runnable
{
    private static Logger logger = Logger.getLogger(Paxos.class);

    private String value;
    private List<Server> servers;

    @Setter(AccessLevel.PUBLIC)
    Condition phase1Condition;

    @Setter(AccessLevel.PUBLIC)
    Condition phase2Condition;

    @Setter(AccessLevel.PUBLIC)
    ServerThread serverThread;

    @Setter(AccessLevel.PUBLIC)
    Lock lock;

    // Start the paxos algorithm to reserve the value
    public String reserveValue(String value, List<Server> servers)
    {
        // Incremeent the paxos id
        int id =  this.serverThread.getPaxosId().incrementAndGet();

        // Increment the paxos Id
        logger.debug("Reserving value " + value + " with id " + id);

        // Servers list doesn't include "this" server
        int numServers = servers.size() + 1;

        // Phase 1 of Paxos: Propose the value
        Proposer proposer = new Proposer();
        proposer.setId(id);
        proposer.setValue(value);
        proposer.setServerThread(this.serverThread);
        proposer.propose(servers);
        // Wait till we get enough promises to move onto phase 2
        try
        {
            lock.lock();
            while(serverThread.getNumPromises().get() < (numServers / 2 ) + 1)
            {

                phase1Condition.await();
            }

        } catch (InterruptedException e)
        {
            logger.error("Unale to await for promises",e);
        }finally
        {
            lock.unlock();

        }

        logger.debug("Starting phase 2");
        // Phase 2 of Paxos: Accept the value
        proposer.accept(servers);
        // Wait till we get enough accepts to agree on a value
        logger.debug("Waiting for value agreement");
        try
        {
            lock.lock();
            while(serverThread.getNumAccepts().get() < (numServers / 2 ) + 1)
            {

                phase2Condition.await();
            }

        } catch (InterruptedException e)
        {
            logger.error("Unale to await for accepts",e);
        }finally
        {
            lock.unlock();

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
