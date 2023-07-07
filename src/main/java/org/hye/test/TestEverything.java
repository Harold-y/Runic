package org.hye.test;

import java.io.File;
import java.util.Arrays;

public class TestEverything {

    public static void main(String[] args) {
        int[] a1 = {1, 65, 234, 521, 5123334, 523};
        int[] a2 = Arrays.copyOfRange(a1, 0, 2);
        System.out.println(Arrays.toString(a2));

        File file = new File("G:\\Programming\\Programming.rar");
        String[] names = file.getName().split("\\.");
        System.out.println(names[names.length - 1]);
    }
}
