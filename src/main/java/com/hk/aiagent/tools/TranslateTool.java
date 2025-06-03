package com.hk.aiagent.tools;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

/**
 * @author huangkun
 * @date 2025/5/26 15:37
 */
@Slf4j
public class TranslateTool {

    private final static String URL = "https://fanyi.baidu.com/ait/text/translate";

    public String translate(String text,String from,String to) {
        /**
         * corpusIds
         * :
         * []
         * domain
         * :
         * "common"
         * from
         * :
         * "en"
         * milliTimestamp
         * :
         * 1748245001549
         * needPhonetic
         * :
         * true
         * query
         * :
         * "translate"
         * reference
         * :
         * ""
         * to
         * :
         * "zh"
         */
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("from", from);
        paramMap.put("to", to);
        paramMap.put("query", text);
        paramMap.put("domain", "common");
        paramMap.put("needPhonetic", true);
        paramMap.put("reference", "");
        paramMap.put("corpusIds", "");
        paramMap.put("milliTimestamp", System.currentTimeMillis());
        String response = HttpUtil.post(URL, paramMap);
        log.error("translate response: {}", response);
        return response;
    }
}
