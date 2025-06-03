package com.hk.tools;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hk.config.PexelsConfig;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author huangkun
 * @date 2025/6/1 9:31
 */
@Component
public class ImageSearchTool {

    private final PexelsConfig pexelsConfig;

    public ImageSearchTool(PexelsConfig pexelsConfig) {
        this.pexelsConfig = pexelsConfig;
    }

    @Tool(description = "search image by keyword from web")
    public String searchImage(@ToolParam(description = "search keyword") String keyword) {
        Map<String, String> headMap = new HashMap<>();
        headMap.put("Authorization", pexelsConfig.getApiKey());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("query", keyword);
        try {
            HttpResponse response = HttpUtil.createGet(pexelsConfig.getUrl())
                    .addHeaders(headMap)
                    .form(paramMap)
                    .execute();

            if (response.isOk()) {
                String body = response.body();
                JSONObject jsonObject = JSONUtil.parseObj(body);
                JSONArray jsonArray = jsonObject.getJSONArray("photos");
                String mediumListStr = jsonArray.stream().map(photoObj -> {
                    JSONObject json = (JSONObject) photoObj;
                    return json.getJSONObject("src");
                }).map(photo -> photo.getStr("medium")).collect(Collectors.joining(","));
                return mediumListStr;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
