package distributed.server.threads;

import distributed.server.pojos.Server;
import distributed.server.propose.Proposer;
import distributed.utils.Command;
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
    private String paxos(String value)
    {
        logger.debug("Reserving value " + value);

        // Phase 1 of Paxos: Propose the value
        Proposer proposer = new Proposer(value);
        boolean proposalAccepted = proposer.propose(peers);
        if(proposalAccepted == false)
        {
            return Command.REJECT_PREPARE.getCommand();
        }
        // Phase 2 of Paxos: Accept the value
        boolean accepted = proposer.accept(peers);
        if(accepted == false)
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

    public String receivePrepareRequest(String[] tokens)
    {
        return processRequest(Command.PROMISE.getCommand(),Command.REJECT_PREPARE.getCommand(),tokens);
    }


    public String receiveAcceptRequest(String[] tokens)
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
            // The client wants us to agree on a value
            if(tokens.length > 1)
            {
                String value = tokens[1];
                return paxos(value);

            }
        }else if(Command.PREPARE_REQUEST.getCommand().equals(tokens[0]))
        {
           // A peer sent a prepare request with a value

            if(tokens.length>2)
            {
               return receivePrepareRequest(tokens) ;
            }

        }
        else if(Command.ACCEPT_REQUEST.getCommand().equals(tokens[0]))
        {
            if(tokens.length>2)
            {
                return receiveAcceptRequest(tokens) ;
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
                logger.debug("Processing message from client: " + msg);
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
