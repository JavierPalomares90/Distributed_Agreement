package distributed.server.requests;

import distributed.server.pojos.Server;
import distributed.server.responses.AcceptResponse;
import distributed.server.responses.Response;
import distributed.utils.Command;

import java.util.ArrayList;
import java.util.List;

public class AcceptRequest extends  Request
{
    @Override
    public String toString()
    {
        return Command.ACCEPT_REQUEST + " " + this.getId() + " " + this.getValue();
    }


}
