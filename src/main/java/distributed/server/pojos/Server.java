package distributed.server.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Server
{
    private String ipAddress;
    private Integer port;
    private Integer serverId;

    public Server(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}
}
