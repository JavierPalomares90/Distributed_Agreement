package distributed.server.threads;

import distributed.server.pojos.Server;
import distributed.server.requests.PrepareRequest;
import distributed.server.requests.Request;
import distributed.server.responses.Response;
import distributed.utils.Command;
import distributed.utils.Utils;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class MessageThread implements Runnable
{
    private static Logger logger = Logger.getLogger(MessageThread.class);

    @Setter(AccessLevel.PUBLIC)
    Socket socket;

    @Setter(AccessLevel.PUBLIC)
    List<Server> peers;



   // Start the paxos algorithm to reserve the value
    private String reserveValue(String value)
    {
        logger.debug("Reserving value " + value);
        // Prepare request
        Request request = new PrepareRequest();
        request.setId(Utils.getId());
        request.setValue(value);
        // send the prepare request to all peers
        List<Response> prepareResponses = request.sendRequest(peers);
        /**
         * TODO: Parse the prepare responses, then send accept response
         */

        return null;
    }


    private String processMessage(String msg)
    {
        String[] tokens = msg.split("\\s+");
        if(Command.RESERVE.getCommand().equals(tokens[0]))
        {
            if(tokens.length > 1)
            {
                String value = tokens[1];
                return reserveValue(value);

            }
        }
        return "Unable to process msg " + msg;
    }

    public void run()
    {
        try
        {
            //We have received a TCP socket from the client.  Receive message and reply.
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outputWriter = new PrintWriter(socket.getOutputStream(), true);
            String inputLine = inputReader.readLine();
            if (inputLine != null && inputLine.length() > 0)
            {
                String msg = inputLine;
                logger.debug("Processing message from client");
                String response = processMessage(msg);
                // Increment the logical clock on response
                if(response != null)
                {
                    outputWriter.write(response);
                    outputWriter.flush();
                }
                outputWriter.close();
            }
        }catch (IOException e)
        {

            logger.error("Unable to receive msg from client",e);
        }
        finally
        {
            if(socket != null)
            {
                try
                {
                    socket.close();
                }catch (IOException e)
                {
                    logger.error("Unable to close client socket",e);
                }
            }

        }

    }
}
