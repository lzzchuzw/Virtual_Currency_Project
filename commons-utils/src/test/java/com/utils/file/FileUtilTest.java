package com.utils.file;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class FileUtilTest {

    @Test
    public void testWriteStringToFile() {
        File file = new File("D:/test.txt");
        try {
            String data = "this is test content ";
            FileUtil.writeStringToFile(file, data, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
