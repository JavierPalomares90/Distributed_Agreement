package distributed.malicious.requests;

import distributed.malicious.utils.Utils;
import distributed.server.paxos.requests.AcceptRequest;
import distributed.utils.Command;

public class RandomAcceptRequest extends AcceptRequest
{
    // Malicious impl: send a random value in the accept request
    @Override
    public String toString()
    {
        String s = Command.ACCEPT_REQUEST + " " + this.getId() + " " + Utils.getRandomValue() + "\n";
        return s;
    }


}