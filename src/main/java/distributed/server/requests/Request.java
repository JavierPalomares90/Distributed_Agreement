package distributed.server.requests;

import distributed.server.pojos.Server;
import distributed.server.responses.AcceptResponse;
import distributed.server.responses.PrepareResponse;
import distributed.server.responses.Response;
import lombok.Data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Data
public abstract class Request
{
    int id;
    String value;

    /**
     * Send a request to the peer. Return the response from the peer
     */

    public Response sendRequestToPeer(Server peer)
    {
        Response prepareResponse = null;
        String command = this.toString();
        // Send the command over TCP
        Socket tcpSocket = null;

        try
        {
            // Get the socket
            tcpSocket = new Socket(peer.getIpAddress(),peer.getPort());
            PrintWriter outputWriter = new PrintWriter(tcpSocket.getOutputStream(), true);
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            // Write the purchase message
            outputWriter.write(command + "\n");
            outputWriter.flush();

            // Wait for the response from the server
            String response = "";

            while(true)
            {
                response = inputReader.readLine();
                if (response == null)
                {
                    break;
                }
                String[] tokens = response.split("\\s+");
                int id = Integer.parseInt(tokens[1]);
                String value = tokens[2];
                if(this instanceof PrepareRequest)
                {
                    prepareResponse = new PrepareResponse(id,value);
                }else
                {
                    prepareResponse = new AcceptResponse(id,value);

                }
            }

        }catch(Exception e)
        {
            System.err.println("Unable to send msg to " + peer.toString());
            e.printStackTrace();
        }finally
        {
            if (tcpSocket != null)
            {
                try
                {
                    tcpSocket.close();
                }catch(Exception e)
                {
                    System.err.println("Unable to close socket");
                    e.printStackTrace();
                }
            }

        }

        return prepareResponse;
    }

    /**
     * Send the request to the peers
     * @param peers
     * @return
     */
    public List<Response> sendRequest(List<Server> peers)
    {
        List<Response> responses = new ArrayList<Response>();

        for(Server peer: peers)
        {
            Response response = sendRequestToPeer(peer);
            responses.add(response);
        }
        return responses;

    }

}
