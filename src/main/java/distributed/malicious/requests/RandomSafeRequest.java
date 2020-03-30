package distributed.malicious.requests;

import distributed.malicious.utils.Utils;
import distributed.server.byzantine.requests.SafeRequest;
import distributed.utils.Command;

public class RandomSafeRequest extends SafeRequest
{
    @Override
    public String toString()
    {
        return Command.SAFE_REQUEST + " " + this.getId() + " " + Utils.getRandomValue() + " " + this.getSenderID() + "\n";
    }
}