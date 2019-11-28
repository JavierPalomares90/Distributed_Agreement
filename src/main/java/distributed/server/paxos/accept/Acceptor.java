package distributed.server.paxos.accept;

import distributed.server.threads.ServerThread;
import distributed.utils.Command;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

@Data
public class Acceptor
{
    @Setter(AccessLevel.PUBLIC)
    private ServerThread serverThread;

    @Setter(AccessLevel.PUBLIC)
    private Condition phase1Condition;

    @Setter(AccessLevel.PUBLIC)
    private Condition phase2Condition;

    @Setter(AccessLevel.PUBLIC)
    private Lock lock;

    private static Logger logger = Logger.getLogger(Acceptor.class);

    public String receivePromiseRequest(String[] tokens)
    {
        logger.debug("Received promise request");
        int id = Integer.parseInt(tokens[1]);
        if(id > this.serverThread.getPaxosId().get())
        {
            this.serverThread.getPaxosId().set(id);
        }
        this.serverThread.incrementNumPromises();
        return "Received promise request";
    }

    public String receiveAcceptRequest(String[] tokens)
    {
        logger.debug("Received accept request");
        int id = Integer.parseInt(tokens[1]);
        String value = tokens[2];
        if(id >= this.serverThread.getPaxosId().get())
        {
            logger.debug("Accepting accept request");
            return Command.ACCEPT.getCommand() + " " + id + " " + value;
        }
        return Command.REJECT_ACCEPT.getCommand() + " " + id + " " + value;
    }

    public synchronized String receiveAcceptResponse(String[] tokens,int numServers)
    {
        this.serverThread.getNumAccepts().getAndIncrement();
        if(this.serverThread.getNumAccepts().get() > (numServers/2) + 1)
        {
            lock.lock();
            // We've received enough accepts. can agree on a value
            this.phase2Condition.signalAll();
            lock.unlock();
        }
        return "Agreed to value";
    }

    public String receivePrepareRequest(String[] tokens)
    {
        if (tokens.length < 3)
        {
            return Command.REJECT_PREPARE.getCommand() + " " + this.serverThread.getPaxosId().get() + " " + this.serverThread.getPaxosValue() + "\n";
        }
        int id = Integer.parseInt(tokens[1]);
        String value = tokens[2];
        logger.debug("Receiving prepare request with id " + id + " value " + value);
        // Compare the id to our current id
        if(id > this.serverThread.getPaxosId().get())
        {
            // Update the new paxosId and accept the request
            this.serverThread.getPaxosId().set(id);
            this.serverThread.setPaxosValue(value);

            logger.debug("Promising to request with id " + id + " value " + value);
            return Command.PROMISE.getCommand() + " " + this.serverThread.getPaxosId().get() + " " + this.serverThread.getPaxosValue() + "\n";
        }
        logger.debug("Rejecting prepare request with id " + id + " value " + value);
        // REJECT the request and send the paxos id and value
        return Command.REJECT_PREPARE.getCommand() + " " + this.serverThread.getPaxosId().get() + " " + this.serverThread.getPaxosValue() + "\n";

    }

}