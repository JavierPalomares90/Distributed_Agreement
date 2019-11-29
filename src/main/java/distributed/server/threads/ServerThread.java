package distributed.server.threads;

import distributed.server.paxos.Paxos;
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
import java.util.concurrent.locks.Condition;
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


    @Getter @Setter(AccessLevel.PRIVATE)
    private AtomicInteger numPromises;
    @Getter @Setter(AccessLevel.PRIVATE)
    private AtomicInteger numAccepts;
    @Getter @Setter(AccessLevel.PRIVATE)
    private AtomicInteger numPromisesRejected;
    @Getter @Setter(AccessLevel.PRIVATE)
    private AtomicInteger numAcceptsRejected;

    // The Paxos Id
    @Getter @Setter(AccessLevel.PUBLIC)
    private AtomicInteger paxosId;
    // The Paxos value
    @Getter @Setter(AccessLevel.PUBLIC)
    private String paxosValue;

    @Getter @Setter(AccessLevel.PUBLIC)
    private Thread paxosThread;

    private final Lock threadLock;
    // Conditionals to wait for promises and accepts from a majority
    private final Condition waitForPromises;
    private final Condition waitForAccepts;

    public void init()
    {
        numPromises = new AtomicInteger(0);
        numAccepts = new AtomicInteger(0);
        numAcceptsRejected = new AtomicInteger(0);
        numPromisesRejected = new AtomicInteger(0);
    }


    public ServerThread()
    {
        paxosId = new AtomicInteger(0);
        threadLock = new ReentrantLock();
        waitForPromises = threadLock.newCondition();
        waitForAccepts = threadLock.newCondition();
    }

    public void incrementNumPromisesRejected()
    {
        int numPromisesRejected = this.numPromisesRejected.incrementAndGet();
        int numServers = this.peers.size() + 1;
        if(numPromisesRejected > (numServers/2) + 1)
        {
            logger.debug("Majority of servers rejected the prepare");
            // majority of peers have rejected, stop waiting for phase2
            notifyPromises();
        }

    }


    public void incrementNumAcceptsRejected()
    {
        int numAcceptsRejected = this.numAcceptsRejected.incrementAndGet();
        int numServers = this.peers.size() + 1;
        if(numAcceptsRejected > (numServers/2) + 1)
        {
            logger.debug("Majority of servers rejected the accept");
            notifyAccepts();

        }

    }

    private void notifyAccepts()
    {
        // majority of peers have rejected, stop waiting for agreement
        threadLock.lock();
        synchronized (waitForAccepts)
        {
            waitForAccepts.notifyAll();
        }
        threadLock.unlock();

    }

    private void notifyPromises()
    {
        threadLock.lock();
        synchronized (waitForPromises)
        {
            waitForPromises.notifyAll();

        }
        threadLock.unlock();
    }

    public void incrementNumPromises()
    {
        int numPromises = this.numPromises.incrementAndGet();
        int numServers = this.peers.size() + 1;
        if(numPromises >= (numServers/2) + 1)
        {
            notifyPromises();
        }
    }

    public void incrementNumAccepts()
    {
        int numAccepts = this.numAccepts.incrementAndGet();
        int numServers = this.peers.size() + 1;
        if(numAccepts >= (numServers/2) + 1)
        {
            notifyAccepts();
        }
    }


    private AtomicBoolean isRunning = new AtomicBoolean(false);

    public void setPaxosValue(String value)
    {
        threadLock.lock();
        paxosValue = String.copyValueOf(value.toCharArray());
        threadLock.unlock();
    }

    public String getPaxosValue()
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
        init();
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
                    clientThread.setPhase1Condition(waitForPromises);
                    clientThread.setPhase2Condition(waitForAccepts);
                    clientThread.setServerThread(this);
                    clientThread.setLock(threadLock);
                    new Thread(clientThread).start();
                }
            }

        }catch (Exception e)
        {
            logger.error("Unable to listen for clients",e);
        }finally
        {
            if(tcpServerSocket != null)
            {
                try
                {
                    tcpServerSocket.close();
                }catch (Exception e)
                {
                    logger.error("Unable to close server socket");
                }
            }
        }

        logger.debug("Stopping server thread");

    }

}
