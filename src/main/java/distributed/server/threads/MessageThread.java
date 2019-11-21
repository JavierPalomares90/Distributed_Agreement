package distributed.server.threads;

import distributed.server.pojos.Server;
import distributed.server.requests.AcceptRequest;
import distributed.server.requests.PrepareRequest;
import distributed.server.requests.Request;
import distributed.server.responses.AcceptResponse;
import distributed.server.responses.PrepareResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageThread implements Runnable
{
    private static Logger logger = Logger.getLogger(MessageThread.class);

    @Setter(AccessLevel.PUBLIC)
    Socket socket;

    @Setter(AccessLevel.PUBLIC)
    List<Server> peers;

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


   // Start the paxos algorithm to reserve the value
    private String reserveValue(String value)
    {
        logger.debug("Reserving value " + value);
        // Prepare request
        Request request = new PrepareRequest();
        request.setId(ServerThread.getPaxosId());
        request.setValue(value);
        // send the prepare request to all peers
        List<Response> prepareResponses = sendRequest(request, peers);
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
        // send the prepare request to all peers
        List<Response> acceptResponses = sendRequest(request, peers);
        boolean acceptRequestAccepted = requestAccepted(acceptResponses);
        if(acceptRequestAccepted == false)
        {
            return Command.REJECT_ACCEPT.getCommand();
        }
        // The value is agreed to
        return Command.AGREE.getCommand() + " " + value;
    }


    private String processRequest(String accept, String reject, String[] tokens)
    {
        if (tokens.length < 3)
        {
            return reject + " " + ServerThread.getPaxosId() + " " + ServerThread.getPaxosValue();
        }
        int id = Integer.parseInt(tokens[1]);
        String value = tokens[2];
        // Compare the id to our current id
        if(ServerThread.getPaxosId() > id)
        {
            // REJECT the request and send the paxos id and value
            return reject + " " + ServerThread.getPaxosId() + " " + ServerThread.getPaxosValue();
        }
        // Update the new paxosId and accept the request
        ServerThread.setPaxosId(id);
        ServerThread.setPaxosValue(value);

        return accept + " " + ServerThread.getPaxosId() + " " + ServerThread.getPaxosValue();

    }

    public String processPrepareRequest(String[] tokens)
    {
        return processRequest(Command.PREPARE_RESPONSE.getCommand(),Command.REJECT_PREPARE.getCommand(),tokens);
    }


    public String processAcceptRequest(String[] tokens)
    {
        /**
         * TODO: Complete impl
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
        }else if(Command.PREPARE_REQUEST.getCommand().equals(tokens[0]))
        {

            if(tokens.length>2)
            {
               return processPrepareRequest(tokens) ;
            }

        }
        else if(Command.ACCEPT_REQUEST.getCommand().equals(tokens[0]))
        {
            if(tokens.length>2)
            {
                return processAcceptRequest(tokens) ;
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
