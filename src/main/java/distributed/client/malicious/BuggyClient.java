package distributed.client.malicious;

import distributed.client.Client;
import distributed.server.pojos.Server;
import distributed.utils.Command;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Random;


/**
 * Implementation of a buggy client that proposes 0 or 1 by chance
 */
public class BuggyClient extends Client
{
    private static Logger logger = Logger.getLogger(MaliciousClient.class);

    public static void sendValue(List<Server> servers, String value)
    {
        // Propose a value by chance
        Random r = new Random();
        int randomValue = r.nextInt(2);
        value = Integer.toString(randomValue);
        logger.debug("I'm a buggy client so I'm going to propose " + value);
        String cmd = Command.PROPOSE.getCommand() + " " + value + "\n";
        sendCmd(servers, cmd);
    }

}
