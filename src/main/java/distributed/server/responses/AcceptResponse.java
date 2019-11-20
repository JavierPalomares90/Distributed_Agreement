package distributed.server.responses;

import distributed.utils.Command;

public class AcceptResponse extends Response
{
    public AcceptResponse(int id, String value)
    {
        super(id,value);
    }

    @Override
    public String toString()
    {
        return Command.ACCEPT_RESPONSE + " " + this.getId() + " " + this.getValue();
    }

}
