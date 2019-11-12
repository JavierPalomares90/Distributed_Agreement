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
import java.util.concurrent.locks.Lock;

public class ServerThread implements Runnable
{
    private static Logger logger = Logger.getLogger(ServerThread.class);

    @Getter @Setter(AccessLevel.PUBLIC)
    private Integer port;
    @Getter @Setter(AccessLevel.PUBLIC)
    private String ipAddress;
    @Getter @Setter(AccessLevel.PUBLIC)
    private List<Server> peers;

    private Lock threadLock;
    private AtomicBoolean isRunning;

    /**
     * Listen for messages from the peers
     */
    public void run()
    {
        this.isRunning.getAndSet(true);
        logger.debug("Starting server thread");
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
                    ClientThread clientThread = new ClientThread();
                    clientThread.setSocket(socket);
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
