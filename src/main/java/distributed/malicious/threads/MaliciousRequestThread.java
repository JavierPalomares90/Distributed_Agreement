package distributed.malicious.threads;

import distributed.server.paxos.requests.Request;
import distributed.server.pojos.Server;
import distributed.server.threads.RequestThread;

public class MaliciousRequestThread extends RequestThread
{

    public MaliciousRequestThread(Request request, Server acceptor, boolean waitForResponse) 
    {
        //TODO: Complete impl
        super(request,acceptor,waitForResponse);
    }

}