package distributed.server.requests;

import lombok.Data;

@Data
public abstract class Request
{
    int id;
    String value;
}
