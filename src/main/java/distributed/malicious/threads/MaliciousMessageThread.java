package distributed.malicious.threads;

import distributed.server.pojos.Server;
import distributed.server.threads.MessageThread;

public class MaliciousMessageThread extends MessageThread {

    public MaliciousMessageThread(Server sender) {
        super(sender);
    }

}