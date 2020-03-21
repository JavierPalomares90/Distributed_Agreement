package distributed.malicious.threads;

import distributed.server.pojos.Server;
import distributed.server.threads.BroadcastThread;

public class MaliciousBroadcastThread extends BroadcastThread
{

    public MaliciousBroadcastThread(Server acceptor,String command, boolean waitForResponse)
    {
        //TODO: Complete impl
        super(acceptor,command,waitForResponse);
    }

}