package distributed.malicious.server.byzantine;

import org.apache.log4j.Logger;

import distributed.server.byzantine.ByzPaxos;
import distributed.server.pojos.Server;

import java.util.List;

public class MaliciousByzPaxos extends ByzPaxos
{
    private static Logger logger = Logger.getLogger(MaliciousByzPaxos.class);

    // Maliciously propose a value
    @Override
    public String proposeValue(String value, List<Server> servers)
    {
        //TODO: Complete impl

        return null;

    }


}