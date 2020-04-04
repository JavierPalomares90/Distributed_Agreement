package distributed.malicious.client;

import distributed.client.Client;
import distributed.server.pojos.Server;
import distributed.utils.Command;
import distributed.utils.Value;

import org.apache.log4j.Logger;

import java.util.List;


/**
 * Implementation of a malicious Client. Always lies about the values in the proposal
 */
public class MaliciousClient extends Client
{
    private static Logger logger = Logger.getLogger(MaliciousClient.class);

    public static void sendValue(List<Server> servers, String value)
    {
        // Flip the proposed value
        if(Value.ZERO.getValue().equals(value))
        {
            value = Value.ONE.getValue();
        }else
        {
            value = Value.ZERO.getValue();
        }
        logger.debug("I'm a bad client so I'm going to propose " + value);
        String cmd = Command.PROPOSE.getCommand() + " " + value + "\n";
        sendCmd(servers, cmd);
    }

    public static void main(String[] args)
    {
        Client.main(args);
    }

}