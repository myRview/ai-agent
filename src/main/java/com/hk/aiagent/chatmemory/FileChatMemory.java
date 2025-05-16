package com.hk.aiagent.chatmemory;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huangkun
 * @date 2025/5/14 16:05
 */
public class FileChatMemory implements BaseChatMemory {

    private static final String CHAT_MEMORY_DIR = "/chatmemory/";
    private static final String CHAT_MEMORY_FILE_SUFFIX = ".kryo";
    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    private File getConversationFile(String conversationId) {
        return FileUtil.file(System.getProperty("user.dir") + CHAT_MEMORY_DIR + conversationId + CHAT_MEMORY_FILE_SUFFIX);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        //获取历史信息
        List<Message> historyMessage = getHistoryMessage(conversationId);
        historyMessage.addAll(messages);
        //写入文件
        saveMessage(conversationId, historyMessage);

    }

    private void saveMessage(String conversationId, List<Message> historyMessage) {

        File file = getConversationFile(conversationId);
        try (
                Output output = new Output(new FileOutputStream(file))
        ) {
            kryo.writeObject(output, historyMessage);
        } catch (IORuntimeException | FileNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取历史对话
     *
     * @param conversationId
     * @return
     */

    private List<Message> getHistoryMessage(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (file.exists()) {
            try (
                    Input input = new Input(new FileInputStream(file))
            ) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return messages;
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> historyMessage = getHistoryMessage(conversationId);
        return historyMessage.stream().skip(Math.max(historyMessage.size() - lastN, 0)).toList();
    }

    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) FileUtil.del(file);
    }
}
