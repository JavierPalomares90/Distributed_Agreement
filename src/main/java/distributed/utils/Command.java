package distributed.utils;

public enum Command
{
    RESERVE("RESERVE"),
    PREPARE_REQUEST("PREPARE_REQUEST"),
    ACCEPT_REQUEST("ACCEPT_REQUEST"),
    LEARN_REQUEST("LEARN_REQUEST"),
    PREPARE_RESPONSE("PREPARE_RESPONSE"),
    ACCEPT_RESPONSE("ACCEPT_RESPONSE"),
    LEARN_RESPONSE("LEARN_RESPONSE"),
    REJECT_PREPARE("REJECT_PREPARE"),
    REJECT_ACCEPT("REJECT_ACCEPT"),
    AGREE("AGREE");


    private String command;

    Command(String command)
    {
        this.command = command;
    }

    public String getCommand()
    {
        return this.command;
    }

}
