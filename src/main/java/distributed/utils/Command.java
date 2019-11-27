package distributed.utils;

public enum Command
{
    PROPOSE("PROPOSE"),
    PREPARE_REQUEST("PREPARE_REQUEST"),
    ACCEPT_REQUEST("ACCEPT_REQUEST"),
    LEARN_REQUEST("LEARN_REQUEST"),
    PROMISE("PROMISE"),
    ACCEPT("ACCEPT"),
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
