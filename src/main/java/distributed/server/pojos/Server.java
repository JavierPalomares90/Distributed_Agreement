package distributed.server.pojos;

import lombok.Data;

@Data
public class Server
{
    private String ipAddress;
    private Integer port;
    private Integer serverId;
}
