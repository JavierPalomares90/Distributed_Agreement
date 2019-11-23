package distributed.server.propose;

import distributed.server.pojos.Server;
import distributed.server.requests.AcceptRequest;
import distributed.server.requests.PrepareRequest;
import distributed.server.requests.Request;
import distributed.server.responses.AcceptResponse;
import distributed.server.responses.PrepareResponse;
import distributed.server.responses.Response;
import distributed.server.threads.ServerThread;
import distributed.utils.Command;
import lombok.Data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Proposer
{
    private String value;

    public Proposer(String value)
    {
        this.value = value;
    }

    /**
     * Send a request to the peer. Return the response from the peer
     */
    public Response sendRequestToPeer(Request request, Server peer)
    {
        Response responseFromPeer = null;
        String command = request.toString();
        // Send the command over TCP
        Socket tcpSocket = null;

        try
        {
            // Get the socket
            tcpSocket = new Socket(peer.getIpAddress(), peer.getPort());
            PrintWriter outputWriter = new PrintWriter(tcpSocket.getOutputStream(), true);
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            // Write the purchase message
            outputWriter.write(command + "\n");
            outputWriter.flush();

            // Wait for the response from the server
            String response = "";

            while (true)
            {
                response = inputReader.readLine();
                if (response == null)
                {
                    break;
                }
                String[] tokens = response.split("\\s+");
                String resCmd = tokens[0];
                int id = Integer.parseInt(tokens[1]);
                String value = tokens[2];
                if (Command.REJECT_PREPARE.getCommand().equals(resCmd))
                {
                    // The Prepare request was rejected
                    responseFromPeer = new PrepareResponse(id, value, false);

                } else if (Command.REJECT_ACCEPT.getCommand().equals(resCmd))
                {
                    responseFromPeer = new AcceptResponse(id, value, false);
                    // The accept request was rejected
                } else if (Command.PREPARE_RESPONSE.getCommand().equals(resCmd))
                {
                    responseFromPeer = new PrepareResponse(id, value, true);
                    // the Prepare request was accepted
                } else if (Command.ACCEPT_RESPONSE.getCommand().equals(resCmd))
                {
                    responseFromPeer = new AcceptResponse(id, value, true);
                    // the accept request was accepted
                }
            }

        } catch (Exception e)
        {
            System.err.println("Unable to send msg to " + peer.toString());
            e.printStackTrace();
        } finally
        {
            if (tcpSocket != null)
            {
                try
                {
                    tcpSocket.close();
                } catch (Exception e)
                {
                    System.err.println("Unable to close socket");
                    e.printStackTrace();
                }
            }

        }

        return responseFromPeer;
    }

    private void updateToGreatestPaxosId(List<Response> responses)
    {
        for (Response r: responses)
        {
            int id = r.getId();
            if (id > ServerThread.getPaxosId())
            {
                ServerThread.setPaxosId(id);
                /**
                 * TODO: Do we need to set the value as well?
                 */
            }
        }

    }

    private boolean requestAccepted(List<Response> prepareResponses)
    {
        //True if the majority of peers accepted the prepare
        int numResponses = prepareResponses.size();
        int numAccepted = 0;
        for (Response r : prepareResponses)
        {
            if (r == null || r.isResponseAccepted() == false)
            {
                continue;
            }
            numAccepted++;
        }

        if (numAccepted >= (numResponses + 1) / 2)
        {
            return true;
        }
        return false;
    }

    /**
     * Send the request to the peers
     *
     * @param peers
     * @return
     */
    public List<Response> sendRequest(Request request, List<Server> peers)
    {
        List<Response> responses = new ArrayList<Response>();

        for (Server peer : peers)
        {
            Response response = sendRequestToPeer(request, peer);
            responses.add(response);
        }
        return responses;

    }


    public String propose(List<Server> peers)
    {
        // send the prepare request to all peers
        Request prepareRequest = new PrepareRequest();
        prepareRequest.setId(ServerThread.getPaxosId());
        prepareRequest.setValue(this.value);
        List<Response> prepareResponses = sendRequest(prepareRequest, peers);
        boolean prepareRequestAccepted = requestAccepted(prepareResponses);
        if(prepareRequestAccepted == false)
        {
            // Prepare response was not accepted
            // Update the id from the responses
            updateToGreatestPaxosId(prepareResponses);
            return Command.REJECT_PREPARE.getCommand();
        }
        // Prepare requests is good, now send accept request
        /**
         * TODO: Parse the prepare responses, then send accept response
         */
        Request acceptRequest = new AcceptRequest();
        acceptRequest.setId(ServerThread.getPaxosId());
        acceptRequest.setValue(value);
        // send the accept request to all peers
        List<Response> acceptResponses = sendRequest(acceptRequest, peers);
        boolean acceptRequestAccepted = requestAccepted(acceptResponses);
        if(acceptRequestAccepted == false)
        {
            return Command.REJECT_ACCEPT.getCommand();
        }
        // The value is agreed to
        return Command.AGREE.getCommand() + " " + value;


    }
}
