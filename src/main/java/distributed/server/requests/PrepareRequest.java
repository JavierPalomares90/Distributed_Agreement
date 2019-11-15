package distributed.server.requests;

import distributed.server.pojos.Server;
import distributed.server.responses.PrepareResponse;
import distributed.server.responses.Response;
import distributed.utils.Command;

import java.util.ArrayList;
import java.util.List;

public class PrepareRequest extends Request
{
    @Override
    public String toString()
    {
        return Command.PREPARE + " " + this.getId() + " " + this.getValue();
    }

    @Override
    public Response sendRequestToPeer(Server peer)
    {
        Response response = new PrepareResponse();
        /**
         * TODO: Send request to peer, parse the response
         */

        return response;
    }
}
