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
        return Command.ACCEPT + " " + this.getId() + " " + this.getValue();
    }

    @Override
    public Response sendRequestToPeer(Server peer)
    {
        Response response = new AcceptResponse();
        /**
         * TODO: Send request to peer, parse the response
         */

        return response;
    }


}
