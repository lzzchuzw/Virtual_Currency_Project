package com.utils.file;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 文件操作工具类
 * 封装自 Apache Commons io FileUtils
 */
public class FileUtil {

    /**
     * @param file   文件
     * @param data   数据
     * @param append 是否追加
     * @throws IOException
     */
    public static void writeStringToFile(File file, String data, boolean append) throws IOException {
        Charset encoding = Charset.forName("UTF-8"); // 使用utf-8编码
        FileUtils.writeStringToFile(file, data, encoding, append);
    }

}
