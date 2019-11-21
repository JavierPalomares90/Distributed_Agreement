package distributed.server.requests;

import distributed.server.pojos.Server;
import distributed.server.responses.AcceptResponse;
import distributed.server.responses.PrepareResponse;
import distributed.server.responses.Response;
import distributed.utils.Command;
import lombok.Data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Data
public abstract class Request
{
    int id;
    String value;


}
