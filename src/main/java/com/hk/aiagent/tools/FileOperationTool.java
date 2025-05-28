package com.hk.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.hk.aiagent.constant.CommonConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 提供给Ai调用的文件操作工具类
 *
 * @author huangkun
 * @date 2025/5/24 14:52
 */
public class FileOperationTool {

    private final String FILE_DIR = System.getProperty("user.dir") + CommonConstant.FILE_TEMP_PATH + "/file";

    /**
     * 读取文件内容
     *
     * @param fileName
     * @return
     */
    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of the file to read") String fileName) {
        try {
            String filePath = FILE_DIR + "/" + fileName;
            String read = FileUtil.readUtf8String(filePath);
            return read;
        } catch (IORuntimeException e) {
            return "Error reading file: " + e.getMessage();

        }

    }

    /**
     * 写入文件内容
     *
     * @param fileName
     * @param content
     * @return
     */
    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of the file to write") String fileName,
                            @ToolParam(description = "Content to write to the file") String content) {
        try {
            String filePath = FILE_DIR + "/" + fileName;
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return filePath;
        } catch (IORuntimeException e) {
            return "Error writing file: " + e.getMessage();
        }

    }

}
