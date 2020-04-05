package distributed.malicious.utils;

import java.util.Random;

public class Utils
{
    // Generate a random 0 or 1
    public static String getRandomValue()
    {
        Random r = new Random();
        Integer i = r.nextInt(2);
        return i.toString();
    }
}