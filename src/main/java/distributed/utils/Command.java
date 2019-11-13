package distributed.utils;

public enum Command
{
    RESERVE("RESERVE"),
    PREPARE("PREPARE"),
    ACCEPT("ACCEPT"),
    LEARN("LEARN");

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
