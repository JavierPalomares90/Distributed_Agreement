package distributed.client;

import distributed.server.pojos.Server;
import distributed.utils.Command;
import distributed.utils.Utils;
import distributed.utils.Value;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client
{
    private static Logger logger = Logger.getLogger(Client.class);


    private static Server pickServer(List<Server> servers, int index)
    {
        // Pick a server from the list round robin style

        int numServers = servers.size();
        return servers.get(index % numServers);

    }

    private static Socket getSocket(List<Server> servers)
    {
        Socket socket = null;
        int index = 0;

        while (socket == null)
        {
            // Pick a server to connect to
            Server toConnect = pickServer(servers, index);
            try
            {
                logger.debug("Connecting to server " + toConnect.toString());
                socket = new Socket(toConnect.getIpAddress(), toConnect.getPort());

            } catch (IOException e)
            {
                socket = null;
                logger.error("Unable to connect to server " + toConnect.toString(), e);

            }
            index++;
        }
        return socket;

    }

    public static void sendValue(List<Server> servers, String value)
    {
        String cmd = Command.PROPOSE.getCommand() + " " + value + "\n";
        sendCmd(servers, cmd);
    }

    public static void sendCmd(List<Server> servers, String cmd)
    {
        Socket socket = getSocket(servers);
        PrintWriter outputWriter = null;
        BufferedReader inputReader = null;
        try
        {
            outputWriter = new PrintWriter(socket.getOutputStream(), true);
            inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e)
        {
            logger.error("Unable to get input/output streams to socket", e);
            return;

        }
        // Write the purchase message
        outputWriter.write(cmd + "\n");
        outputWriter.flush();
        System.out.print("Wrote message to server. Waiting for response...");
        // Wait for the response from the server
        String response = "";

        while (true)
        {
            try
            {
                response = inputReader.readLine();
                System.out.print("...");
            } catch (IOException e)
            {
                logger.error("Unable to read line", e);
            }
            if (response == null)
            {
                break;
            }
            // Print the response
            System.out.println(response);
        }
        if (socket != null)
        {
            try
            {
                socket.close();
            } catch (IOException e)
            {
                logger.error("Unable to close socket ", e);
            }

        }

    }

    public static void main(String[] args)
    {
        if(args.length < 1)
        {
            logger.error("Usage: <hostsFilePath>" );
            System.exit(-1);
        }

        List<Server> servers = Utils.getHosts(args[0]);
        Scanner sc = new Scanner(System.in);
        System.out.println("Propose a value:\n [0] 0\n [1] 1");
        while (sc.hasNextLine())
        {

            String value = sc.nextLine();
            if(Value.ZERO.getValue().equals(value) || Value.ONE.getValue().equals(value))
            {
                sendValue(servers, value);
            }
            else
            {
                System.out.println("Invalid value");
            }
            System.out.println("Propose a value:\n [0] 0\n [1] 1");

        }
        sc.close();
    }
}
