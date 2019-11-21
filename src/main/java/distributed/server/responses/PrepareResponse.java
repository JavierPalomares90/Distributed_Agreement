package distributed.server.responses;

import distributed.utils.Command;

public class PrepareResponse extends Response
{
    public PrepareResponse(int id, String value, boolean accepted)
    {
        super(id,value,accepted);
    }

    @Override
    public String toString()
    {
        return Command.PREPARE_RESPONSE + " " + this.getId() + " " + this.getValue();
    }
}
