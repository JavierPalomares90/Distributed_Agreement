package distributed.server.requests;

import distributed.server.pojos.Server;
import distributed.server.responses.PrepareResponse;
import distributed.server.responses.Response;
import distributed.utils.Command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PrepareRequest extends Request
{
    @Override
    public String toString()
    {
        return Command.PREPARE_REQUEST + " " + this.getId() + " " + this.getValue();
    }
}
