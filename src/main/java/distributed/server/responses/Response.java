package distributed.server.responses;

import lombok.Data;

@Data
public abstract class Response
{
    int id;
    String value;

    public Response(int id, String value)
    {
        this.id = id;
        this.value = value;
    }

}
