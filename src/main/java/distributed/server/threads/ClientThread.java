package distributed.server.threads;

import distributed.utils.Command;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread implements Runnable
{
    private static Logger logger = Logger.getLogger(ClientThread.class);

    @Setter(AccessLevel.PUBLIC)
    Socket socket;


   // Start the paxos algorithm to reserve the value
    private void reserveValue(String value)
    {
        //TODO: Complete impl

    }


    private String processMessage(String msg)
    {
        String[] tokens = msg.split("\\s+");
        if(Command.RESERVE.getCommand().equals(tokens[0]))
        {
            if(tokens.length > 1)
            {
                String value = tokens[1];
                reserveValue(value);

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
