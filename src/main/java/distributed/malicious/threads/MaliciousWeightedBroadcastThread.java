package distributed.malicious.threads;

import distributed.server.pojos.Server;
import distributed.server.pojos.WeightedResponse;
import distributed.server.threads.WeightedBroadcastThread;

import java.security.InvalidParameterException;
import org.apache.log4j.Logger;

// Malicious impl of the weighted broadcast Thread
public class MaliciousWeightedBroadcastThread extends WeightedBroadcastThread
{
    private static Logger logger = Logger.getLogger(MaliciousWeightedBroadcastThread.class);

    public MaliciousWeightedBroadcastThread(Server acceptor, String command, boolean waitForResponse, Float weight) {
        super(acceptor,command,waitForResponse,weight);
    }

    @Override
    public WeightedResponse call() throws InvalidParameterException
    {
        //TODO: Complete impl
        return null;
    }
    
}