package distributed.malicious.requests;

import distributed.malicious.utils.Utils;
import distributed.server.byzantine.requests.SafeRequest;
import distributed.utils.Command;

public class RandomSafeRequest extends SafeRequest
{
    @Override
    public String toString()
    {
        String s = Command.SAFE_REQUEST + " " + this.getId() + " " + Utils.getRandomValue() + " " + this.getSenderID() + "\n";
        return s;
    }
}