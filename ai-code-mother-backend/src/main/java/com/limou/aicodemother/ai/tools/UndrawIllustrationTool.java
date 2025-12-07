package com.limou.aicodemother.ai.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.limou.aicodemother.langgraph4j.model.ImageResource;
import com.limou.aicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class UndrawIllustrationTool {
    private static final String UNDRAW_API_URL = "https://undraw.co/_next/data/N6M_hYvpIPjDtR8MHPCqU/search/%s.json?term=%s";

    @Tool("搜索插画图片，用于网站的美化和修饰")
    public List<ImageResource> searchIllustrations(@P("搜索关键词") String query) {
        int searchCount = 12;
        List<ImageResource> imageList = new ArrayList<>();
        //1.构造查询请求
        String apiUrl = String.format(UNDRAW_API_URL, query, query);
        try (HttpResponse response = HttpRequest.get(apiUrl).timeout(10000).execute()) {
            if (!response.isOk()) {
                return imageList;
            }
            JSONObject result = JSONUtil.parseObj(response.body());
            JSONObject pageProps = result.getJSONObject("pageProps");
            if (pageProps == null)
                return imageList;
            JSONArray illustrations = pageProps.getJSONArray("initialResults");
            if (illustrations == null || illustrations.isEmpty())
                return imageList;
            int actualCount = Math.min(searchCount, illustrations.size());
            for (int i = 0; i < actualCount; i++) {
                JSONObject illustration = illustrations.getJSONObject(i);
                String title = illustration.getStr("title", "插画");
                String media = illustration.getStr("media", "");
                if (StrUtil.isBlank(media)) {
                    continue;
                }
                ImageResource imageResource = ImageResource.builder()
                        .description(title)
                        .url(media)
                        .category(ImageCategoryEnum.ILLUSTRATION)
                        .build();
                imageList.add(imageResource);

            }
        } catch (Exception e) {
            log.error("Undraw API 调用失败: {}", e.getMessage(), e);

        }
        return imageList;

    }
}
