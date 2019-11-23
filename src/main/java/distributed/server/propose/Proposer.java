package distributed.server.propose;

import distributed.server.pojos.Server;
import distributed.server.requests.AcceptRequest;
import distributed.server.requests.PrepareRequest;
import distributed.server.requests.Request;
import distributed.server.responses.AcceptResponse;
import distributed.server.responses.PromiseResponse;
import distributed.server.responses.Response;
import distributed.server.threads.ServerThread;
import distributed.utils.Command;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Proposer
{
    private static Logger logger = Logger.getLogger(Proposer.class);
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
                    responseFromPeer = new PromiseResponse(id, value, false);

                } else if (Command.REJECT_ACCEPT.getCommand().equals(resCmd))
                {
                    responseFromPeer = new AcceptResponse(id, value, false);
                    // The accept request was rejected
                } else if (Command.PROMISE.getCommand().equals(resCmd))
                {
                    responseFromPeer = new PromiseResponse(id, value, true);
                    // the Prepare request was accepted
                } else if (Command.ACCEPT.getCommand().equals(resCmd))
                {
                    responseFromPeer = new AcceptResponse(id, value, true);
                    // the accept request was accepted
                }
            }

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

        return responseFromPeer;
    }

    private boolean requestAccepted(List<Response> prepareResponses, int numServers)
    {
        int numAccepted = 1; // Count our own self acceptance
        for (Response r : prepareResponses)
        {
            if (r == null || r.isResponseAccepted() == false)
            {
                continue;
            }
            numAccepted++;
            int id = r.getId();
            if(id > ServerThread.getPaxosId())
            {
                ServerThread.setPaxosId(id);

            }
        }

        // Request accepted if at least half of all the nodes accepted the request
        if (numAccepted >= (numServers / 2) + 1)
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
        List<Response> responses = new ArrayList<>();

        for (Server peer : peers)
        {
            Response response = sendRequestToPeer(request, peer);
            responses.add(response);
        }
        return responses;

    }

    public boolean accept(List<Server> peers)
    {
        Request acceptRequest = new AcceptRequest();
        acceptRequest.setId(ServerThread.getPaxosId());
        acceptRequest.setValue(value);
        // send the accept request to all peers
        List<Response> acceptResponses = sendRequest(acceptRequest, peers);
        boolean acceptRequestAccepted = requestAccepted(acceptResponses,peers.size());
        return acceptRequestAccepted;
    }

    /**
     * Broadcast the prepare request to the servers
     * @param prepareRequest
     * @param peers
     * @return
     */
    public List<Response> sendPrepareRequest(PrepareRequest prepareRequest, List<Server> peers)
    {
        return sendRequest(prepareRequest, peers);
    }


    public boolean propose(List<Server> peers)
    {
        logger.debug("Proposing value " + this.value);
        // send the prepare request to all peers
        PrepareRequest prepareRequest = new PrepareRequest();
        prepareRequest.setId(ServerThread.getPaxosId());
        prepareRequest.setValue(this.value);
        logger.debug("Sending prepare request to peers");
        List<Response> promises = sendPrepareRequest(prepareRequest,peers);
        boolean promised = requestAccepted(promises,peers.size());
        return promised;
    }
}
