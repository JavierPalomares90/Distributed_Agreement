package distributed.client.malicious;

import distributed.client.Client;
import distributed.server.pojos.Server;
import distributed.utils.Command;
import distributed.utils.Utils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;


/**
 * Implementation of a malicious Client. Always lies about the values in the proposal
 */
public class MaliciousClient extends Client
{
    private static Logger logger = Logger.getLogger(MaliciousClient.class);


    public static void sendValue(List<Server> servers, String value)
    {
        // Flip the proposed value
        if(Client.ZERO.equals(value))
        {
            value = Client.ONE;
        }else
        {
            value = Client.ZERO;
        }
        logger.debug("I'm a bad client so I'm going to propose " + value);
        String cmd = Command.PROPOSE.getCommand() + " " + value + "\n";
        sendCmd(servers, cmd);
    }

}