package distributed.malicious.threads;

import distributed.server.threads.ServerThread;

public class MaliciousServerThread extends ServerThread
{

    public MaliciousServerThread(Float weight, int id) 
    {
        super(weight, id);
    }

}