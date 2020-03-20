package distributed.malicious.threads;

import java.net.Socket;

import distributed.server.pojos.Server;
import distributed.server.threads.MessageThread;
import distributed.server.threads.ServerThread;

public class MaliciousServerThread extends ServerThread
{

    public MaliciousServerThread(Float weight, int id) 
    {
        super(weight, id);
    }

    @Override
    protected void startMessageThread(Server sender,Socket socket)
    {
        // Spawn off a new thread to process messages from this client
        MessageThread clientThread = new MaliciousMessageThread(sender);
        startMessageThread(clientThread,socket);
        
    }

}