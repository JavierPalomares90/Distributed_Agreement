package distributed.server.propose;

import distributed.server.pojos.Server;
import distributed.server.requests.PrepareRequest;
import lombok.Data;

import java.util.List;

public class Proposer
{
    private PrepareRequest request;

    public Proposer(int id, String value)
    {
        request = new PrepareRequest();
        request.setId(id);
        request.setValue(value);
    }

    public boolean propose(List<Server> peers)
    {
        return false;

    }
}
