package distributed.server.requests;

import distributed.utils.Command;

public class AcceptRequest extends  Request
{
    @Override
    public String toString()
    {
        return Command.ACCEPT + " " + this.getId() + " " + this.getValue();
    }


}
