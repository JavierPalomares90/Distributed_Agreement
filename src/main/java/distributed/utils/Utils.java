package distributed.utils;

import distributed.server.pojos.Server;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils
{
    private static Logger logger = Logger.getLogger(Utils.class);

    public static String sendTcpMessage(Server acceptor,String command, boolean waitForResponse)
    {
        Socket tcpSocket = null;
        String response = "";
        try
        {
            // Get the socket
            tcpSocket = new Socket(acceptor.getIpAddress(), acceptor.getPort());
            PrintWriter outputWriter = new PrintWriter(tcpSocket.getOutputStream(), true);
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            // Write the message
            outputWriter.write(command);
            outputWriter.flush();
            while(waitForResponse)
            {
                String input = inputReader.readLine();
                if (input == null)
                {
                    break;
                }
                response += input;
            }

        } catch (Exception e)
        {
            logger.error("Unable to send msg to " + acceptor.toString(),e);
        } finally
        {
            if (tcpSocket != null)
            {
                try
                {
                    tcpSocket.close();
                } catch (Exception e)
                {
                    logger.error("Unable to close socket",e);
                }
            }
        }
        return response;
    }

    public static int getQuorumSize(int numServers,int numFaulty)
    {
        return numServers/2 + numFaulty + 1;
    }

    public static int getAnchorSize(List<Server> servers, double p)
    {
        double weight = 0;
        int anchorSize = 0;
        for (Server server: servers)
        {
            if(weight > p)
            {
                break;
            }
            weight += server.getWeight();
            anchorSize++;
        }
        return anchorSize;
    }

    // Load the hosts from the yaml file
    public static List<Server> getHosts(String path)
    {
        logger.debug("Getting hosts from hosts.yaml");
        InputStream inputStream;

        try
        {
            inputStream = new FileInputStream(path);
        }catch (FileNotFoundException e)
        {
            logger.error("Unable to find hosts file",e);
            return null;
        }

        if(inputStream == null)
        {
            logger.error("Unable to load from hosts.yaml");
            return null;
        }
        Yaml yaml = new Yaml(new Constructor(Server.class));
        List<Server> hosts = new ArrayList<Server>();
        for (Object o : yaml.loadAll(inputStream))
        {
            hosts.add( (Server) o);
        }
        try
        {
            inputStream.close();
        }catch (IOException e)
        {

            logger.error("Unable to close input stream",e);
        }
        return hosts;
    }
}
