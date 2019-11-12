package distributed.client;

import distributed.server.pojos.Server;
import distributed.utils.Utils;
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

    private static String RESERVE = "reserve";

    private static Server pickServer(List<Server> servers, int index)
    {
    	// Pick a server from the list round robin style

	    int numServers = servers.size();
	    return servers.get(index % numServers);

    }

    private static void getSocket(List<Server> servers)
    {

    }

    private static void sendCmd(List<Server> servers, String cmd)
    {
    	Socket socket = null;
    	int index = 0;

    	// Pick a server to connect to
    	Server toConnect = pickServer(servers,index);
    	try
	    {
		    socket = new Socket(toConnect.getIpAddress(),toConnect.getPort());

		}catch (IOException e)
		{
			logger.error("Unable to connect to server " + toConnect.toString(),e);

		}

		logger.debug("Connecting to server " + toConnect.toString());
		PrintWriter outputWriter = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		// Write the purchase message
		outputWriter.write(cmd + "\n");
		outputWriter.flush();
		logger.debug("Wrote message to server. Waiting for response");
		// Wait for the response from the server
		String response = "";

		while(true)
		{
			response = inputReader.readLine();
			if (response == null)
			{
				break;
			}
			// Print the response
			System.out.println(response);
			logger.debug("Response from server: " + response)
		}
		finally
		{
			if (socket != null)
			{
				try
				{
					socket.close();
				}catch(IOException e)
				{
					logger.error("Unable to close socket ",e);
				}

			}
		}

    }

    public static void main(String[] args)
    {
        List<Server> servers = Utils.getHosts();
	    Scanner sc = new Scanner(System.in);
	    while(sc.hasNextLine())
	    {
	    	String cmd = sc.nextLine();
	    	sendCmd(servers,cmd);
	    }
    }
}
