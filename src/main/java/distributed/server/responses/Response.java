package distributed.server.responses;

import lombok.Data;

@Data
public abstract class Response
{
    int id;
    String value;
}
