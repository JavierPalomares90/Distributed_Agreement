package distributed.server.threads;

import distributed.server.paxos.Paxos;
import distributed.server.paxos.accept.Acceptor;
import distributed.server.pojos.Server;
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
import java.util.concurrent.locks.Lock;

public class MessageThread implements Runnable
{
    private static Logger logger = Logger.getLogger(MessageThread.class);

    @Setter(AccessLevel.PUBLIC)
    Socket socket;

    @Setter(AccessLevel.PUBLIC)
    List<Server> peers;

    @Setter(AccessLevel.PUBLIC)
    Lock phase1Lock;

    @Setter(AccessLevel.PUBLIC)
    Lock phase2Lock;


   // Start the paxos algorithm to reserve the value
    private String reserveValue(String value)
    {
        logger.debug("Reserving value using paxos: " + value);
        Paxos paxos = new Paxos();
        paxos.setValue(value);
        paxos.setServers(peers);
        paxos.setPhase1Lock(phase1Lock);
        paxos.setPhase2Lock(phase2Lock);
        Thread paxosThread = new Thread(paxos);
        paxosThread.start();
        try
        {
            paxosThread.join();
        } catch (InterruptedException e)
        {
            logger.error("Unable to wait for paxos thread to finish",e);
        }
        return "Value " + value + " is reserved";
    }


    public String receivePrepareRequest(String[] tokens)
    {
        return Acceptor.receivePrepareRequest(tokens);
    }

    public synchronized String receivePromiseRequest(String[] tokens)
    {
        int id = Integer.parseInt(tokens[1]);
        if(id > ServerThread.getPaxosId())
        {
            ServerThread.setPaxosId(id);
        }
        ServerThread.numPromises.getAndIncrement();
        int numServers = peers.size() + 1;
        if(ServerThread.numPromises.get() > (numServers/2) + 1)
        {
            // We've received enough promises. Can continue to phase 2 of paxos
            phase1Lock.notifyAll();
        }
        return "Moving onto phase 2 of paxos";
    }


    public String receiveAcceptRequest(String[] tokens)
    {
        int id = Integer.parseInt(tokens[1]);
        if(id > ServerThread.getPaxosId())
        {
            return Command.ACCEPT.getCommand();
        }
        return Command.REJECT_ACCEPT.getCommand();
    }

    public synchronized String receiveAcceptResponse(String[] tokens)
    {
        ServerThread.numAccepts.getAndIncrement();
        int numServers = peers.size() + 1;
        if(ServerThread.numAccepts.get() > (numServers/2) + 1)
        {
            // We've received enough accepts. can agree on a value
            phase2Lock.notifyAll();
        }
        return "Agreed to value";
    }


    private String processMessage(String msg)
    {
        String[] tokens = msg.split("\\s+");
        if(Command.RESERVE.getCommand().equals(tokens[0]))
        {
            // The client wants us to agree on a value. Start the paxos value
            if(tokens.length > 1)
            {
                String value = tokens[1];
                return reserveValue(value);

            }
        }else if(Command.PREPARE_REQUEST.getCommand().equals(tokens[0]))
        {
           // A peer sent a prepare request with a value
            if(tokens.length > 2)
            {
               return receivePrepareRequest(tokens);
            }
        }
        else if (Command.PROMISE.getCommand().equals(tokens[0]))
        {
            // A peer promised to accept our prepare request
            if(tokens.length > 2)
            {
                return receivePromiseRequest(tokens);
            }
        }
        else if(Command.ACCEPT_REQUEST.getCommand().equals(tokens[0]))
        {
            // A peer sent an accept request with a value
            if(tokens.length>2)
            {
                return receiveAcceptRequest(tokens) ;
            }
        }
        else if(Command.ACCEPT.getCommand().equals(tokens[0]))
        {
            // A peer sent a response to our accept request
            receiveAcceptResponse(tokens);

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
                logger.debug("Processing message: " + msg);
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
