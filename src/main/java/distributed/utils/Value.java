package distributed.utils;

public enum Value
{
    ZERO("0"),
    ONE("1");

    private String value;

    Value(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return this.value;
    }
}