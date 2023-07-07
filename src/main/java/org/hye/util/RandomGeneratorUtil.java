package org.hye.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomGeneratorUtil {
    public static String[] specialChars()
    {
        return new String[]{"+", "-", "@", "_", "#"};
    }
    public static String randomPass(Integer length, String[] specialChars)
    {
        int isNum;
        Random random = new Random();
        StringBuilder pass = new StringBuilder();
        for(int i = 0 ; i < length; i ++)
        {
            int determinant = random.nextInt(10);
            isNum = ThreadLocalRandom.current().nextInt(0, 2 + 1);
            if(isNum == 0)
                pass.append(determinant);
            else if (isNum == 1){
                int toAdd = random.nextInt(10);
                if(random.nextBoolean())
                {
                    char character = 'a';
                    character += toAdd;
                    pass.append(character);
                }else
                {
                    char character = 'A';
                    character += toAdd;
                    pass.append(character);
                }
            }else {
                int chooseNext = ThreadLocalRandom.current().nextInt(0, specialChars.length);
                pass.append(specialChars[chooseNext]);
            }
        }
        return pass.toString();
    }

    public static String randomPassNumChars(Integer length)
    {
        int isNum;
        Random random = new Random();
        StringBuilder pass = new StringBuilder();
        for(int i = 0 ; i < length; i ++)
        {
            int determinant = random.nextInt(10);
            isNum = ThreadLocalRandom.current().nextInt(0, 1 + 1);
            if(isNum == 0)
                pass.append(determinant);
            else if (isNum == 1){
                int toAdd = random.nextInt(10);
                if(random.nextBoolean())
                {
                    char character = 'a';
                    character += toAdd;
                    pass.append(character);
                }else
                {
                    char character = 'A';
                    character += toAdd;
                    pass.append(character);
                }
            }
        }
        return pass.toString();
    }
}
