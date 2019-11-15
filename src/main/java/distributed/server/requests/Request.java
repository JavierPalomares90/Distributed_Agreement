package distributed.server.requests;

import distributed.server.pojos.Server;
import distributed.server.responses.Response;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class Request
{
    int id;
    String value;

    /**
     * Send a request to the peer. Return the response from the peer
     */

    public abstract Response sendRequestToPeer(Server peer);

    /**
     * Send the request to the peers
     * @param peers
     * @return
     */
    public List<Response> sendRequest(List<Server> peers)
    {
        List<Response> responses = new ArrayList<Response>();

        for(Server peer: peers)
        {
            Response response = sendRequestToPeer(peer);
            responses.add(response);
        }
        return responses;

    }

}
