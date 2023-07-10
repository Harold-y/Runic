package org.hye.test;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class TestEverything {

    public static void main(String[] args) {
        /*
        int[] a1 = {1, 65, 234, 521, 5123334, 523};
        int[] a2 = Arrays.copyOfRange(a1, 0, 2);
        System.out.println(Arrays.toString(a2));

        File file = new File("G:\\Programming\\Programming.rar");
        String[] names = file.getName().split("\\.");
        System.out.println(names[names.length - 1]);
        System.out.println(FilenameUtils.removeExtension("Programming.tar.gz"));
         */

        String mtnPath = "F:/";
        String filePath = "F:/Test/userImg/ea2d83b8-6943-4195-b563-53184537ac75.ru.md";
        /*
        File file = new File(filePath);
        String confirmPath = filePath.split(mtnPath)[1];
        System.out.println(System.getProperty("file.separator"));
        System.out.println(confirmPath);
        System.out.println(file.getAbsolutePath().replaceAll("\\\\", "/"));

        String[] extensionLists = file.getAbsolutePath().split("\\.");
        String extension = "";
        if (extensionLists.length > 2)
            extension = extensionLists[extensionLists.length - 2];
        else
            extension = extensionLists[extensionLists.length - 1];
        System.out.println(extension);
         */


        int startPos = filePath.indexOf(mtnPath);
        int endingPos = startPos + mtnPath.length();
        System.out.println(filePath.substring(endingPos));

        /*
        Path path = Paths.get(filePath);
        String directory = path.getParent().toString();
        System.out.println(directory);

         */
    }
}
