package distributed.server.requests;

import distributed.utils.Command;

public class PrepareRequest extends Request
{
    @Override
    public String toString()
    {
        return Command.PREPARE + " " + this.getId() + " " + this.getValue();
    }
}
