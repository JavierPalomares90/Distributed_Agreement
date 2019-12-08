package distributed.server.paxos.propose;

import distributed.server.pojos.Server;
import distributed.server.paxos.requests.AcceptRequest;
import distributed.server.paxos.requests.PrepareRequest;
import distributed.server.paxos.requests.Request;
import distributed.server.threads.ServerThread;
import distributed.utils.Command;
import distributed.utils.Utils;
import lombok.Data;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;


@Data
public class Proposer
{
    private static Logger logger = Logger.getLogger(Proposer.class);
    protected ServerThread serverThread;


    protected void sendRequestToAcceptor(Request request, Server acceptor, boolean waitForResponse)
    {
        logger.debug("Sending request " + request.toString() + " to acceptor" + acceptor);
        String command = request.toString();
        // Send the command over TCP
        String response = Utils.sendTcpMessage(acceptor, command, waitForResponse);
        parseResponseFromAcceptor(response);

    }

    /**
     * Parse the responses from the acceptors
     * @param response
     */
    private void parseResponseFromAcceptor(String response)
    {
        logger.debug("Response from acceptor " + response);
        String[] tokens = response.split("\\s+");
        if(tokens.length > 1)
        {
            if (Command.PROMISE.getCommand().equals(tokens[0]))
            {

                // Update the id
                updateIdAndValue(tokens);
                // the prepare request was accepted
                this.serverThread.incrementNumPromises();

            }else if (Command.ACCEPT.getCommand().equals(tokens[0]))
            {
                // the accept reqeust was accepted
                updateId(tokens);
                this.serverThread.incrementNumAccepts();
            }else if(Command.REJECT_PREPARE.getCommand().equals(tokens[0]))
            {
                // the prepare request was rejected
                updateIdAndValue(tokens);
                this.serverThread.incrementNumPromisesRejected();

            }else if(Command.REJECT_ACCEPT.getCommand().equals(tokens[0]))
            {
                // The accept request was rejected
                updateId(tokens);
                this.serverThread.incrementNumAcceptsRejected();
            }
        }
    }
    
    // TODO: Verify ID and value update; still not 100% sure this is correct. A value will be chosen but I don't think it
    // will always be the correct value
    protected void updateId(String[] tokens)
    {
        // Update the id
        if (tokens.length > 2)
        {
            int id = Integer.parseInt(tokens[1]);
            if (id > this.serverThread.getPaxosId().get())
            {
                this.serverThread.getPaxosId().set(id);
            }
        }
    }

    protected void updateIdAndValue(String[] tokens)
    {
        // Update the id
        if (tokens.length > 3)
        {
            int id = Integer.parseInt(tokens[1]);
            String value = tokens[2];
            if (id > this.serverThread.getPaxosId().get())
            {
                this.serverThread.getPaxosId().set(id);
                this.serverThread.setPaxosValue(value);
            }
        }
    }



    /**
     * Send the request to the acceptors
     *
     * @param acceptors
     * @return
     */
    protected void sendRequest(Request request, List<Server> acceptors, boolean waitForResponse)
    {
        for (Server acceptor : acceptors)
        {
            sendRequestToAcceptor(request, acceptor ,waitForResponse);
        }

    }

    protected void sendRequest(Request request, List<Server> acceptors)
    {
        sendRequest(request,acceptors,true);
    }

    public boolean accept(List<Server> acceptors)
    {
        int id = this.serverThread.getPaxosId().get();
        String value =  this.serverThread.getPaxosValue();
        AcceptRequest acceptRequest = new AcceptRequest();
        acceptRequest.setId(id);
        acceptRequest.setValue(value);
        // send the accept request to all acceptors
        sendAcceptRequest(acceptRequest,acceptors);
        return true;
    }

    /**
     * Broadcast the accept request to the servers
     * @param acceptRequest
     * @param acceptors
     */
    protected void sendAcceptRequest(AcceptRequest acceptRequest, List<Server> acceptors)
    {
        sendRequest(acceptRequest, acceptors);
        // Increment the number of accepts for ourself
        this.serverThread.incrementNumAccepts();
    }

    /**
     * Broadcast the prepare request to the servers
     * @param prepareRequest
     * @param acceptors
     */
    protected void sendPrepareRequest(PrepareRequest prepareRequest, List<Server> acceptors)
    {
        sendRequest(prepareRequest, acceptors);
        // Increment the number of promises for ourself
        this.serverThread.incrementNumPromises();
    }


    /**
     * Send the prepare request to the acceptors
     * @param acceptors
     * @return
     */
    public boolean propose(List<Server> acceptors)
    {
        int id = this.serverThread.getPaxosId().get();
        String value =  this.serverThread.getPaxosValue();
        logger.debug("Proposing value " + value);
        // send the prepare request to all peers
        PrepareRequest prepareRequest = new PrepareRequest();
        prepareRequest.setId(id);
        prepareRequest.setValue(value);
        logger.debug("Sending prepare request to acceptors");
        sendPrepareRequest(prepareRequest,acceptors);
        return true;
    }
}
