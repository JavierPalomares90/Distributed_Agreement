package distributed.server.threads;

import distributed.server.pojos.Server;
import distributed.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.security.InvalidParameterException;
import java.util.concurrent.Callable;

@Data
@AllArgsConstructor
public class BroadcastThread implements Callable<String>
{
    private Server acceptor;
    private String command;
    private boolean waitForResponse;

    public String call() throws InvalidParameterException
    {
        String response = Utils.sendTcpMessage(this.acceptor, this.command,this.waitForResponse);
        return response;
    }

}
