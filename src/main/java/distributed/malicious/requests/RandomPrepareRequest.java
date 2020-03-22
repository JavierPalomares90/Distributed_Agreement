package distributed.malicious.requests;

import distributed.malicious.utils.Utils;
import distributed.server.paxos.requests.PrepareRequest;
import distributed.utils.Command;

public class RandomPrepareRequest extends PrepareRequest
{
    // Malicious impl: send the correct id, but a random value
    @Override
    public String toString()
    {
        return Command.PREPARE_REQUEST + " " + this.getId() + " " + Utils.getRandomValue() + " " + this.getSenderID() + "\n";
    }
    // TODO: Add more impls that lie about the id/value/senderId

}