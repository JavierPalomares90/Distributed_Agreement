package distributed.server.paxos.propose;

import distributed.server.pojos.Server;
import distributed.server.paxos.requests.AcceptRequest;
import distributed.server.paxos.requests.PrepareRequest;
import distributed.server.paxos.requests.Request;
import distributed.server.threads.ServerThread;
import distributed.utils.Command;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
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
    @Getter @Setter
    private int id;
    @Getter @Setter
    private String value;
    @Getter @Setter
    private ServerThread serverThread;

    /**
     * Send a request to the peer. Don't wait for the response
     */
    private void sendRequestToAcceptor(Request request, Server peer)
    {
        logger.debug("Sending request " + request.toString() + " to peer" + peer);
        String command = request.toString();
        // Send the command over TCP
        Socket tcpSocket = null;
        String response = "";
        try
        {
            // Get the socket
            tcpSocket = new Socket(peer.getIpAddress(), peer.getPort());
            PrintWriter outputWriter = new PrintWriter(tcpSocket.getOutputStream(), true);
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            // Write the message
            outputWriter.write(command);
            outputWriter.flush();
            while(true)
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
        parseResponseFromAcceptor(response);

    }

    private void updateId(String[] tokens)
    {
        // Received a promise from an acceptor
        // Update the id if needed
        if (tokens.length > 2)
        {
            int id = Integer.parseInt(tokens[1]);
            if (id > this.serverThread.getPaxosId().get())
            {
                this.serverThread.getPaxosId().set(id);
            }
        }
    }

    /**
     * Parse the responses from the acceptors
     * @param response
     */
    private void parseResponseFromAcceptor(String response)
    {
        String[] tokens = response.split("\\s+");
        if(tokens.length > 1)
        {
            if (Command.PROMISE.getCommand().equals(tokens[0]))
            {
                updateId(tokens);
                this.serverThread.incrementNumPromises();

            }else if (Command.ACCEPT.getCommand().equals(tokens[0]))
            {
                // Receive an accept from an acceptor
                updateId(tokens);
                this.serverThread.incrementNumAccepts();
            }else if(Command.REJECT_PREPARE.getCommand().equals(tokens[0]))
            {
                // the prepare request was rejected
                // Update the id
                updateId(tokens);
                this.serverThread.incrementNumPromisesRejected();

            }else if(Command.REJECT_ACCEPT.getCommand().equals(tokens[0]))
            {
                // The accept request was rejected
                updateId(tokens);
                this.serverThread.incrementNumAcceptsRejected();
            }
        }
    }

    /**
     * Send the request to the acceptors
     *
     * @param acceptors
     * @return
     */
    private void sendRequest(Request request, List<Server> acceptors)
    {
        for (Server acceptor : acceptors)
        {
            sendRequestToAcceptor(request, acceptor);
        }
    }

    public boolean accept(List<Server> peers)
    {
        AcceptRequest acceptRequest = new AcceptRequest();
        acceptRequest.setId(this.id);
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
    private void sendAcceptRequest(AcceptRequest acceptRequest, List<Server> peers)
    {
        sendRequest(acceptRequest, peers);
        // Increment the number of accepts for ourself
        this.serverThread.incrementNumAccepts();
    }

    /**
     * Broadcast the prepare request to the servers
     * @param prepareRequest
     * @param peers
     */
    private void sendPrepareRequest(PrepareRequest prepareRequest, List<Server> peers)
    {
        sendRequest(prepareRequest, peers);
        // Increment the number of promises for ourself
        this.serverThread.incrementNumPromises();
    }


    public boolean propose(List<Server> peers)
    {
        logger.debug("Proposing value " + this.value);
        // send the prepare request to all peers
        PrepareRequest prepareRequest = new PrepareRequest();
        prepareRequest.setId(this.id);
        prepareRequest.setValue(this.value);
        logger.debug("Sending prepare request to peers");
        sendPrepareRequest(prepareRequest,peers);
        return true;
    }
}
