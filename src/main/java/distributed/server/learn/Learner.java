package distributed.server.learn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

static 

public class Learner
{
    private String value;

    public Learner(String value)
    {
        this.value = value;
    }
  
    public void sendLearnToPeer(Request request, Server peer)
    {
        String command = request.toString();
        // Send the command over TCP
        Socket tcpSocket = null;

        try
        {
            // Get socket
            tcpSocket = new Socket(peer.getIpAddress(), peer.getPort());
            PrintWriter outputWriter = new PrintWriter(tcpSocket.getOutputStream(), true);
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            // Send learned value message
            outputWriter.write("LEARN " + value + "\n");
            outputWriter.flush();

        } catch (Exception e)
        {
            logger.error("Unable to send msg to " + peer.toString(),e);
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


    }
  
}
