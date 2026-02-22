package com.media;

import com.google.gson.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * 火山引擎豆宝翻译API调用类
 * 用于将英文文本翻译成中文
 */
public class DoubaoTranslator {
    private static final Logger logger = LoggerFactory.getLogger(DoubaoTranslator.class);
    
    private static final String API_URL = "https://ark.cn-beijing.volces.com/api/v3/responses";
    private static final String MODEL = "doubao-seed-translation-250915";
    private static final String SOURCE_LANGUAGE = "en";
    private static final String TARGET_LANGUAGE = "zh";
    
    private final String apiKey;
    private final OkHttpClient httpClient;
    private final Gson gson;
    
    /**
     * 创建翻译器实例
     * @param apiKey 火山引擎API密钥，从环境变量ARK_API_KEY读取
     */
    public DoubaoTranslator(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API密钥不能为空，请设置环境变量ARK_API_KEY");
        }
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    /**
     * 从环境变量创建翻译器实例
     * @return 翻译器实例
     */
    public static DoubaoTranslator createFromEnv() {
        String apiKey = System.getenv("ARK_API_KEY");
        return new DoubaoTranslator(apiKey);
    }
    
    /**
     * 翻译单个文本
     * @param text 要翻译的文本（英文）
     * @return 翻译后的中文文本
     * @throws IOException 网络请求异常
     */
    public String translate(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        JsonObject requestBody = buildRequestBody(text);
        logger.debug("请求体: {}", requestBody);
        
        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                logger.error("翻译API返回错误 - 状态码: {}, 响应: {}", response.code(), errorBody);
                throw new IOException("翻译API错误 - 状态码: " + response.code() + ", 响应: " + errorBody);
            }
            
            String responseBody = response.body() != null ? response.body().string() : "{}";
            logger.debug("API响应: {}", responseBody);
            
            return parseTranslationResponse(responseBody);
        }
    }
    
    /**
     * 批量翻译多个文本
     * @param texts 要翻译的文本列表
     * @return 翻译结果映射 (原文 -> 译文)
     */
    public Map<String, String> translateBatch(List<String> texts) {
        Map<String, String> results = new LinkedHashMap<>();
        
        for (String text : texts) {
            try {
                String translated = translate(text);
                results.put(text, translated);
                logger.info("翻译成功: {} -> {}", text.substring(0, Math.min(50, text.length())), 
                        translated.substring(0, Math.min(50, translated.length())));
            } catch (IOException e) {
                logger.error("翻译失败: {}", text, e);
                results.put(text, text); // 失败时返回原文
            }
        }
        
        return results;
    }
    
    /**
     * 构建API请求体
     */
    private JsonObject buildRequestBody(String text) {
        JsonObject root = new JsonObject();
        root.addProperty("model", MODEL);
        
        // 构建内容数组
        JsonArray contentArray = new JsonArray();
        JsonObject contentItem = new JsonObject();
        contentItem.addProperty("type", "input_text");
        contentItem.addProperty("text", text);
        
        // 添加翻译选项
        JsonObject translationOptions = new JsonObject();
        translationOptions.addProperty("source_language", SOURCE_LANGUAGE);
        translationOptions.addProperty("target_language", TARGET_LANGUAGE);
        contentItem.add("translation_options", translationOptions);
        
        contentArray.add(contentItem);
        
        // 构建消息对象
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.add("content", contentArray);
        
        // 构建输入对象
        JsonArray inputArray = new JsonArray();
        inputArray.add(message);
        
        root.add("input", inputArray);
        
        return root;
    }
    
    /**
     * 解析API响应，提取翻译结果
     */
    private String parseTranslationResponse(String responseBody) {
        try {
            JsonObject response = JsonParser.parseString(responseBody).getAsJsonObject();
            
            // 检查响应结构是否包含翻译结果
            if (response.has("output") && response.getAsJsonArray("output").size() > 0) {
                JsonObject output = response.getAsJsonArray("output").get(0).getAsJsonObject();
                
                if (output.has("content") && output.getAsJsonArray("content").size() > 0) {
                    JsonObject content = output.getAsJsonArray("content").get(0).getAsJsonObject();
                    
                    if (content.has("text")) {
                        return content.get("text").getAsString();
                    }
                }
            }
            
            // 如果响应格式不同，检查其他可能的位置
            logger.warn("无法从响应中提取翻译结果，响应: {}", responseBody);
            return "";
        } catch (Exception e) {
            logger.error("解析翻译响应失败: {}", responseBody, e);
            return "";
        }
    }
    
    /**
     * 关闭HTTP客户端
     */
    public void close() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
        }
    }
    
    // ==================== 测试方法 ====================
    
    public static void main(String[] args) {
        DoubaoTranslator translator = null;
        try {
            translator = DoubaoTranslator.createFromEnv();
            
            // 测试单个翻译
            String englishText = "Getting Started with VS Code";
            logger.info("原文: {}", englishText);
            String translated = translator.translate(englishText);
            logger.info("译文: {}", translated);
            
            // 测试批量翻译
            List<String> texts = Arrays.asList(
                    "Welcome to Visual Studio Code",
                    "Install VS Code extensions to add features",
                    "Debug your Python code with VS Code"
            );
            
            logger.info("开始批量翻译...");
            Map<String, String> results = translator.translateBatch(texts);
            results.forEach((original, translation) -> {
                logger.info("英文: {} -> 中文: {}", original, translation);
            });
            
        } catch (Exception e) {
            logger.error("测试失败", e);
        } finally {
            if (translator != null) {
                translator.close();
            }
        }
    }
}
