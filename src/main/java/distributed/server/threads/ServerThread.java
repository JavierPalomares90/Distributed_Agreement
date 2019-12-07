package distributed.server.threads;

import distributed.server.paxos.Paxos;
import distributed.server.pojos.Server;
import distributed.server.pojos.AtomicFloat;
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
    private Integer serverId;
    @Getter @Setter(AccessLevel.PUBLIC)
    private Integer port;
    @Getter @Setter(AccessLevel.PUBLIC)
    private String ipAddress;
    @Getter @Setter(AccessLevel.PUBLIC)
    private List<Server> peers;


    @Getter @Setter(AccessLevel.PRIVATE)
    private AtomicFloat weightedPromises;
    @Getter @Setter(AccessLevel.PRIVATE)
    private AtomicFloat weightedAccepts;
    @Getter @Setter(AccessLevel.PRIVATE)
    private AtomicFloat weightPromisesRejected;
    @Getter @Setter(AccessLevel.PRIVATE)
    private AtomicFloat weightAcceptsRejected;

    // The Paxos Id
    @Getter @Setter(AccessLevel.PUBLIC)
    private AtomicInteger paxosId;
    // The Paxos value
    @Getter @Setter(AccessLevel.PUBLIC)
    private String paxosValue;

    // The Proposed Paxos Id
    @Getter @Setter(AccessLevel.PUBLIC)
    private AtomicInteger proposedPaxosId;
    // The Propose Paxos value
    @Getter @Setter(AccessLevel.PUBLIC)
    private String proposedPaxosValue;

    // The Safe Paxos Id
    @Getter @Setter(AccessLevel.PUBLIC)
    private AtomicInteger safePaxosId;
    // The Safe Paxos value
    @Getter @Setter(AccessLevel.PUBLIC)
    private String safePaxosValue;
    @Getter @Setter(AccessLevel.PUBLIC)
    private AtomicBoolean valueIsSafe = new AtomicBoolean(false);

    @Getter @Setter(AccessLevel.PUBLIC)
    private Thread paxosThread;

    private final Lock threadLock;
    // Conditionals to wait for promises and accepts from a Byzquorum of weights
    private final Condition waitForPromises;
    private final Condition waitForAccepts;

    public void init()
    {
        weightedPromises = new AtomicFloat();
        weightedAccepts = new AtomicFloat();
        weightAcceptsRejected = new AtomicFloat();
        weightPromisesRejected = new AtomicFloat();
    }


    public ServerThread()
    {
        paxosId = new AtomicInteger(0);
        threadLock = new ReentrantLock();
        waitForPromises = threadLock.newCondition();
        waitForAccepts = threadLock.newCondition();
    }

    public void updateWeightPromisesRejected(float responderWeight)
    {
        this.weightPromisesRejected.set(this.weightPromisesRejected.get() + responderWeight);
        double weightPromisesRejected = this.weightPromisesRejected.get();
        if(weightAcceptsRejected > 0.33)
        {
            logger.debug("No Byzquorum possible.");
            // enough weights of peers have rejected, stop waiting for promise (phase 1)
            notifyPromises();
        }

    }


    public void updateWeightAcceptsRejected(float responderWeight)
    {
        this.weightAcceptsRejected.set(this.weightAcceptsRejected.get() + responderWeight);
        double weightAcceptsRejected = this.weightAcceptsRejected.get();
        if(weightAcceptsRejected > 0.33)
        {
            logger.debug("No Byzquorum possible.");
            notifyAccepts();

        }

    }

    private void notifyAccepts()
    {
        // enough weights of peers have rejected, stop waiting for agreement (phase 2)
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

    public void updatePromisedWeight(double responderWeight)
    {
        this.weightedPromises.set(this.weightedPromises.get() + responderWeight);
        float weightedPromises = this.weightedPromises.get();
        if(weightedPromises >= 0.67)
        {
            notifyPromises();
        }
    }

    public void updateAcceptedWeight(double responderWeight)
    {
        this.weightedAccepts.set(this.weightedAccepts.get() + responderWeight);
        float weightedAccepts = this.weightedAccepts.get();
        if(weightedAccepts >= 0.67)
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

    public void setSafePaxosValue(String value)
    {
        threadLock.lock();
        safePaxosValue = String.copyValueOf(value.toCharArray());
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
