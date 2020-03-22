package distributed.malicious.threads;

import distributed.server.threads.WeightedRequestThread;
import distributed.server.pojos.WeightedResponse;
import distributed.server.paxos.requests.Request;
import distributed.server.pojos.Server;

import java.security.InvalidParameterException;

import org.apache.log4j.Logger;

public class MaliciousWeightedRequestThread extends WeightedRequestThread
{
    private static Logger logger = Logger.getLogger(MaliciousWeightedRequestThread.class);

    public MaliciousWeightedRequestThread(Request request, Server accept, boolean waitForResponse, Float weight) {
        super(request, accept, waitForResponse,weight);
    }

    @Override
    public WeightedResponse call() throws InvalidParameterException
    {
        //TODO: Complete impl
        return null;
    }


    
}