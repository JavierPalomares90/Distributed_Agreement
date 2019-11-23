package distributed.server.paxos.requests;

import lombok.Data;

@Data
public abstract class Request
{
    int id;
    String value;


}
