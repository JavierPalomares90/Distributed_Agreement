package distributed.server.paxos.propose;

import distributed.server.pojos.Server;
import distributed.server.paxos.requests.AcceptRequest;
import distributed.server.paxos.requests.PrepareRequest;
import distributed.server.paxos.requests.Request;
import distributed.server.paxos.responses.AcceptResponse;
import distributed.server.paxos.responses.PromiseResponse;
import distributed.server.paxos.responses.Response;
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
     * Send a request to the peer. Don't wait for the response
     */
    public void sendRequestToPeer(Request request, Server peer)
    {
        logger.debug("Sending request " + request.toString() + " to peer" + peer);
        String command = request.toString();
        // Send the command over TCP
        Socket tcpSocket = null;

        try
        {
            // Get the socket
            tcpSocket = new Socket(peer.getIpAddress(), peer.getPort());
            PrintWriter outputWriter = new PrintWriter(tcpSocket.getOutputStream(), true);
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            // Write the message
            outputWriter.write(command+"\n");
            outputWriter.flush();
            // Don't wait for the response from the peer

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

    /**
     * Send the request to the peers
     *
     * @param peers
     * @return
     */
    public void sendRequest(Request request, List<Server> peers)
    {
        for (Server peer : peers)
        {
            sendRequestToPeer(request, peer);
        }
    }

    public boolean accept(List<Server> peers)
    {
        AcceptRequest acceptRequest = new AcceptRequest();
        acceptRequest.setId(ServerThread.getPaxosId());
        acceptRequest.setValue(value);
        // send the accept request to all peers
        sendAcceptRequest(acceptRequest,peers);
        return true;
    }

    /**
     * Broadcast the accept request to the servers
     * @param acceptRequest
     * @param peers
     */
    public void sendAcceptRequest(AcceptRequest acceptRequest, List<Server> peers)
    {
        sendRequest(acceptRequest, peers);
    }

    /**
     * Broadcast the prepare request to the servers
     * @param prepareRequest
     * @param peers
     */
    public void sendPrepareRequest(PrepareRequest prepareRequest, List<Server> peers)
    {
        sendRequest(prepareRequest, peers);
    }


    public boolean propose(List<Server> peers)
    {
        logger.debug("Proposing value " + this.value);
        // send the prepare request to all peers
        PrepareRequest prepareRequest = new PrepareRequest();
        prepareRequest.setId(ServerThread.getPaxosId());
        prepareRequest.setValue(this.value);
        logger.debug("Sending prepare request to peers");
        sendPrepareRequest(prepareRequest,peers);
        return true;
    }
}
