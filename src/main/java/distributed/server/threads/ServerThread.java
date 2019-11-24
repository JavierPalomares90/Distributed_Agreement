package distributed.server.threads;

import distributed.server.pojos.Server;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerThread implements Runnable
{
    private static Logger logger = Logger.getLogger(ServerThread.class);

    @Getter @Setter(AccessLevel.PUBLIC)
    private Integer port;
    @Getter @Setter(AccessLevel.PUBLIC)
    private String ipAddress;
    @Getter @Setter(AccessLevel.PUBLIC)
    private List<Server> peers;

    // The Paxos Id
    private static AtomicInteger paxosId = new AtomicInteger(0);
    // The Paxos value
    private static String paxosValue;

    public static AtomicInteger numPromises = new AtomicInteger(0);
    public static AtomicInteger numAccepts = new AtomicInteger(0);

    private static Lock threadLock = new ReentrantLock();
    // Locks for phase 1 and phase 2 of paxos
    private static Lock phase1Lock = new ReentrantLock();
    private static Lock phase2Lock = new ReentrantLock();

    private static AtomicBoolean isRunning = new AtomicBoolean(false);

    public static synchronized int incrementPaxosId()
    {
        return paxosId.incrementAndGet();
    }

    public static void setPaxosId(int value)
    {
        paxosId.set(value);
    }

    public static int getPaxosId()
    {
        return paxosId.get();
    }

    public static void setPaxosValue(String value)
    {
        threadLock.lock();
        paxosValue = String.copyValueOf(value.toCharArray());
        threadLock.unlock();
    }

    public static String getPaxosValue()
    {
        if (paxosValue == null)
        {
            return null;
        }
        String result;
        threadLock.lock();
        result = String.copyValueOf(paxosValue.toCharArray());
        threadLock.unlock();
        return result;
    }

    /**
     * Listen for messages from the peers
     */
    public void run()
    {
        this.isRunning.getAndSet(true);
        logger.debug("Starting server thread with ip: " + this.ipAddress + " port: " + this.port);
        ServerSocket tcpServerSocket = null;
        try
        {
            tcpServerSocket = new ServerSocket(this.port);
            while(this.isRunning.get() == true)
            {
                Socket socket = null;
                try
                {
                    // Open a new socket with clients
                    socket = tcpServerSocket.accept();
                    logger.debug("Accepted client connection");
                }catch (IOException e)
                {
                    logger.error("Unable to accept client socker",e);
                }
                if(socket != null)
                {
                    // Spawn off a new thread to process messages from this client
                    MessageThread clientThread = new MessageThread();
                    clientThread.setSocket(socket);
                    clientThread.setPeers(peers);
                    clientThread.setPhase1Lock(phase1Lock);
                    clientThread.setPhase2Lock(phase2Lock);
                    new Thread(clientThread).start();
                }
            }

        }catch (IOException e)
        {
            logger.error("Unable to listen for clients",e);
        }
        logger.debug("Stopping server thread");

    }

}
