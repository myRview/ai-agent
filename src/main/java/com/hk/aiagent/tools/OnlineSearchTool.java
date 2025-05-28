package com.hk.aiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 联网搜索工具
 *
 * @author huangkun
 * @date 2025/5/24 15:33
 */
@Slf4j
public class OnlineSearchTool {


    private final String search_api_key;

    private final String search_url = "https://www.searchapi.io/api/v1/search";

    public OnlineSearchTool(String searchApiKey) {
        this.search_api_key = searchApiKey;
    }

    /**
     * 联网搜索
     */
    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchBaidu(@ToolParam(description = "Search query keyword") String query) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", search_api_key);
        paramMap.put("engine", "bing");
        try {
            String response = HttpUtil.get(search_url, paramMap);
            JSONObject jsonObject = JSONUtil.parseObj(response);
            // 提取 organic_results 部分
            JSONArray jsonArray = jsonObject.getJSONArray("organic_results");
            List<Object> objectList = jsonArray.subList(0, 10);
            String result = objectList.stream().map(o -> {
                        JSONObject temJson = (JSONObject) o;
                        return temJson.getStr("title") + " " + temJson.getStr("link") + " " + temJson.getStr("snippet");
                    }
            ).collect(Collectors.joining(";"));
            return result;
        } catch (Exception e) {
            return "Error request search api " + e.getMessage();
        }


    }
}

