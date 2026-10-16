package com.gk.study.ai.client;

import com.google.gson.Gson;
import com.gk.study.ai.dto.AiProductImageResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

@Component
public class AiImageModelClient {
    private static final Logger logger = LoggerFactory.getLogger(AiImageModelClient.class);
    private static final String PROVIDER_JIMENG = "jimeng";
    private static final String ACTION_SUBMIT = "CVSync2AsyncSubmitTask";
    private static final String ACTION_GET_RESULT = "CVSync2AsyncGetResult";
    private static final String DEFAULT_VERSION = "2022-08-31";
    private static final String DEFAULT_SERVICE = "cv";
    private static final String DEFAULT_REGION = "cn-north-1";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final Gson gson = new Gson();

    @Value("${ai.image.enabled:false}")
    private Boolean enabled;

    @Value("${ai.image.provider:jimeng}")
    private String provider;

    @Value("${ai.image.base-url:https://visual.volcengineapi.com}")
    private String baseUrl;

    @Value("${ai.image.endpoint-path:/images/generations}")
    private String endpointPath;

    @Value("${ai.image.api-key:}")
    private String apiKey;

    @Value("${ai.image.model:}")
    private String model;

    @Value("${ai.image.access-key:}")
    private String accessKey;

    @Value("${ai.image.secret-key:}")
    private String secretKey;

    @Value("${ai.image.req-key:jimeng_t2i_v30}")
    private String reqKey;

    @Value("${ai.image.region:cn-north-1}")
    private String region;

    @Value("${ai.image.service:cv}")
    private String service;

    @Value("${ai.image.version:2022-08-31}")
    private String version;

    @Value("${ai.image.size:1328x1328}")
    private String size;

    @Value("${ai.image.response-format:url}")
    private String responseFormat;

    @Value("${ai.image.timeout-seconds:90}")
    private Integer timeoutSeconds;

    @Value("${ai.image.poll-interval-millis:3000}")
    private Integer pollIntervalMillis;

    @Value("${ai.image.poll-max-times:20}")
    private Integer pollMaxTimes;

    public boolean available() {
        if (!Boolean.TRUE.equals(enabled) || StringUtils.isBlank(baseUrl)) {
            return false;
        }
        if (PROVIDER_JIMENG.equalsIgnoreCase(provider)) {
            return StringUtils.isNotBlank(accessKey)
                    && StringUtils.isNotBlank(secretKey)
                    && StringUtils.isNotBlank(reqKey);
        }
        return StringUtils.isNotBlank(apiKey) && StringUtils.isNotBlank(model);
    }

    public List<AiProductImageResponse.ImageItem> generate(String prompt, int count, String requestedSize, String style) {
        if (!available()) {
            throw new IllegalStateException("AI image model API is not configured");
        }
        if (StringUtils.isBlank(prompt)) {
            throw new IllegalArgumentException("image prompt is required");
        }

        if (PROVIDER_JIMENG.equalsIgnoreCase(provider)) {
            return generateByJimeng(prompt, count, requestedSize, style);
        }
        return generateByOpenAiCompatible(prompt, count, requestedSize, style);
    }

    private List<AiProductImageResponse.ImageItem> generateByJimeng(String prompt, int count, String requestedSize, String style) {
        String resolvedSize = StringUtils.defaultIfBlank(requestedSize, size);
        int[] wh = parseSize(resolvedSize);
        int imageCount = Math.max(1, Math.min(count, 4));

        Map<String, Object> body = new HashMap<>();
        body.put("req_key", reqKey);
        body.put("prompt", prompt);
        body.put("width", wh[0]);
        body.put("height", wh[1]);
        body.put("seed", -1);
        body.put("scale", 2.5);
        body.put("use_pre_llm", false);
        body.put("negative_prompt", "花朵, 植物, 花瓣, 卡通, 宠物, 动物, 人像, 随机摆件, 无关物体, 风景, 抽象装饰, 水印, logo, 二维码, 乱码文字");

        List<AiProductImageResponse.ImageItem> result = new ArrayList<>();
        for (int i = 0; i < imageCount; i++) {
            Map submitResponse = postJimeng(ACTION_SUBMIT, body);
            String taskId = readTaskId(submitResponse);
            result.addAll(pollJimengResult(taskId, prompt, style));
        }
        if (result.isEmpty()) {
            throw new IllegalStateException("Jimeng image response images are empty");
        }
        return result;
    }

    private Map<String, Object> logoInfo() {
        Map<String, Object> logoInfo = new HashMap<>();
        logoInfo.put("add_logo", false);
        return logoInfo;
    }

    private String readTaskId(Map response) {
        Map data = readDataMap(response);
        Object taskId = data.get("task_id");
        if (taskId == null) {
            taskId = data.get("taskId");
        }
        if (taskId == null) {
            throw new IllegalStateException("Jimeng submit response has no task_id");
        }
        return String.valueOf(taskId);
    }

    private List<AiProductImageResponse.ImageItem> pollJimengResult(String taskId, String prompt, String style) {
        int maxTimes = pollMaxTimes == null ? 20 : Math.max(1, pollMaxTimes);
        int interval = pollIntervalMillis == null ? 3000 : Math.max(500, pollIntervalMillis);

        Map<String, Object> reqJson = new HashMap<>();
        reqJson.put("return_url", "url".equalsIgnoreCase(responseFormat));
        reqJson.put("logo_info", logoInfo());

        Map<String, Object> body = new HashMap<>();
        body.put("req_key", reqKey);
        body.put("task_id", taskId);
        body.put("req_json", gson.toJson(reqJson));

        for (int i = 0; i < maxTimes; i++) {
            if (i > 0) {
                sleep(interval);
            }
            Map resultResponse = postJimeng(ACTION_GET_RESULT, body);
            Map data = readDataMap(resultResponse);
            if (isTaskFailed(data)) {
                throw new IllegalStateException("Jimeng image generation task failed: " + gson.toJson(data));
            }
            List<AiProductImageResponse.ImageItem> images = readJimengImages(data, prompt, style);
            if (!images.isEmpty()) {
                return images;
            }
        }
        throw new IllegalStateException("Jimeng image generation task timeout");
    }

    private boolean isTaskFailed(Map data) {
        String status = stringValue(data.get("status"));
        String taskStatus = stringValue(data.get("task_status"));
        return containsAny(status, "failed", "fail", "error")
                || containsAny(taskStatus, "failed", "fail", "error");
    }

    private boolean containsAny(String value, String... tokens) {
        if (StringUtils.isBlank(value)) return false;
        String normalized = value.toLowerCase(Locale.ROOT);
        for (String token : tokens) {
            if (normalized.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private List<AiProductImageResponse.ImageItem> readJimengImages(Map data, String prompt, String style) {
        List<AiProductImageResponse.ImageItem> result = new ArrayList<>();
        collectImagesFromNestedJson(result, data.get("resp_data"), prompt, style);
        collectImagesFromNestedJson(result, data.get("respData"), prompt, style);
        collectImages(result, data.get("image_urls"), prompt, style, true);
        collectImages(result, data.get("imageUrls"), prompt, style, true);
        collectImages(result, data.get("binary_data_base64"), prompt, style, false);
        collectImages(result, data.get("binaryDataBase64"), prompt, style, false);
        collectImages(result, data.get("images"), prompt, style, true);
        return result;
    }

    private void collectImagesFromNestedJson(List<AiProductImageResponse.ImageItem> result,
                                             Object raw,
                                             String prompt,
                                             String style) {
        if (!(raw instanceof String) || StringUtils.isBlank((String) raw)) {
            return;
        }
        try {
            Object parsed = gson.fromJson((String) raw, Object.class);
            if (parsed instanceof Map) {
                Map map = (Map) parsed;
                collectImages(result, map.get("image_urls"), prompt, style, true);
                collectImages(result, map.get("imageUrls"), prompt, style, true);
                collectImages(result, map.get("binary_data_base64"), prompt, style, false);
                collectImages(result, map.get("binaryDataBase64"), prompt, style, false);
                collectImages(result, map.get("images"), prompt, style, true);
            }
        } catch (Exception ignored) {
            // Some Jimeng result fields are plain status text, not nested JSON.
        }
    }

    private void collectImages(List<AiProductImageResponse.ImageItem> result,
                               Object raw,
                               String prompt,
                               String style,
                               boolean preferUrl) {
        if (!(raw instanceof List)) {
            return;
        }
        for (Object item : (List) raw) {
            AiProductImageResponse.ImageItem view = new AiProductImageResponse.ImageItem();
            if (item instanceof Map) {
                Map map = (Map) item;
                Object url = firstNotNull(map.get("url"), map.get("image_url"), map.get("imageUrl"));
                Object b64 = firstNotNull(map.get("b64_json"), map.get("base64"), map.get("data"));
                if (url != null) view.setUrl(String.valueOf(url));
                if (b64 != null) view.setBase64(String.valueOf(b64));
            } else if (item != null) {
                String value = String.valueOf(item);
                if (preferUrl && value.startsWith("http")) {
                    view.setUrl(value);
                } else {
                    view.setBase64(value);
                }
            }
            view.setPrompt(prompt);
            view.setStyle(style);
            if (StringUtils.isNotBlank(view.getUrl()) || StringUtils.isNotBlank(view.getBase64())) {
                result.add(view);
            }
        }
    }

    private Object firstNotNull(Object... values) {
        for (Object value : values) {
            if (value != null) return value;
        }
        return null;
    }

    private Map readDataMap(Map response) {
        if (response == null) {
            throw new IllegalStateException("Jimeng response is empty");
        }
        Object code = response.get("code");
        if (code != null && !"10000".equals(String.valueOf(code)) && !"0".equals(String.valueOf(code))) {
            throw new IllegalStateException("Jimeng response error: " + gson.toJson(response));
        }
        Object data = response.get("data");
        if (!(data instanceof Map)) {
            throw new IllegalStateException("Jimeng response has no data: " + gson.toJson(response));
        }
        return (Map) data;
    }

    private Map postJimeng(String action, Map<String, Object> body) {
        String bodyJson = gson.toJson(body);
        String query = "Action=" + action + "&Version=" + StringUtils.defaultIfBlank(version, DEFAULT_VERSION);
        String url = normalizedBaseUrl() + "/?" + query;
        HttpHeaders headers = buildJimengHeaders(bodyJson, query);
        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
        logger.info("Calling Jimeng image API action={}, reqKey={}, url={}", action, reqKey, url);
        ResponseEntity<Map> response = buildRestTemplate().exchange(URI.create(url), HttpMethod.POST, entity, Map.class);
        logJimengResponse(action, response.getBody());
        return response.getBody();
    }

    private void logJimengResponse(String action, Map response) {
        if (response == null) {
            logger.warn("Jimeng image API response is empty. action={}", action);
            return;
        }
        logger.info("Jimeng image API response action={}, code={}, message={}, requestId={}",
                action,
                response.get("code"),
                response.get("message"),
                response.get("request_id"));
    }

    private HttpHeaders buildJimengHeaders(String bodyJson, String canonicalQueryString) {
        String host = URI.create(normalizedBaseUrl()).getHost();
        String xDate = utcNow("yyyyMMdd'T'HHmmss'Z'");
        String shortDate = xDate.substring(0, 8);
        String payloadHash = sha256Hex(bodyJson);
        String contentType = MediaType.APPLICATION_JSON_VALUE;
        String signedHeaders = "content-type;host;x-content-sha256;x-date";

        String canonicalHeaders = "content-type:" + contentType + "\n"
                + "host:" + host + "\n"
                + "x-content-sha256:" + payloadHash + "\n"
                + "x-date:" + xDate + "\n";
        String canonicalRequest = "POST\n"
                + "/\n"
                + canonicalQueryString + "\n"
                + canonicalHeaders + "\n"
                + signedHeaders + "\n"
                + payloadHash;

        String regionValue = StringUtils.defaultIfBlank(region, DEFAULT_REGION);
        String serviceValue = StringUtils.defaultIfBlank(service, DEFAULT_SERVICE);
        String credentialScope = shortDate + "/" + regionValue + "/" + serviceValue + "/request";
        String stringToSign = "HMAC-SHA256\n"
                + xDate + "\n"
                + credentialScope + "\n"
                + sha256Hex(canonicalRequest);
        String signature = bytesToHex(hmac(signingKey(shortDate, regionValue, serviceValue), stringToSign));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Host", host);
        headers.set("X-Date", xDate);
        headers.set("X-Content-Sha256", payloadHash);
        headers.set("Authorization", "HMAC-SHA256 Credential=" + accessKey + "/" + credentialScope
                + ", SignedHeaders=" + signedHeaders
                + ", Signature=" + signature);
        return headers;
    }

    private byte[] signingKey(String date, String regionValue, String serviceValue) {
        byte[] kDate = hmac(secretKey.getBytes(StandardCharsets.UTF_8), date);
        byte[] kRegion = hmac(kDate, regionValue);
        byte[] kService = hmac(kRegion, serviceValue);
        return hmac(kService, "request");
    }

    private byte[] hmac(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("HMAC signature failed", ex);
        }
    }

    private String sha256Hex(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return bytesToHex(digest.digest(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 failed", ex);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b & 0xff));
        }
        return builder.toString();
    }

    private String utcNow(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }

    private void sleep(int interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Jimeng image generation interrupted", ex);
        }
    }

    private int[] parseSize(String sizeText) {
        String value = StringUtils.defaultIfBlank(sizeText, "1328x1328").toLowerCase(Locale.ROOT);
        String[] parts = value.split("x");
        if (parts.length != 2) {
            return new int[]{1328, 1328};
        }
        try {
            int width = Math.max(512, Math.min(2048, Integer.parseInt(parts[0].trim())));
            int height = Math.max(512, Math.min(2048, Integer.parseInt(parts[1].trim())));
            return new int[]{width, height};
        } catch (NumberFormatException ex) {
            return new int[]{1328, 1328};
        }
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String normalizedBaseUrl() {
        String normalized = StringUtils.defaultIfBlank(baseUrl, "https://visual.volcengineapi.com").trim();
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private List<AiProductImageResponse.ImageItem> generateByOpenAiCompatible(String prompt,
                                                                               int count,
                                                                               String requestedSize,
                                                                               String style) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("prompt", prompt);
        body.put("n", Math.max(1, Math.min(count, 4)));
        body.put("size", StringUtils.defaultIfBlank(requestedSize, size));
        body.put("response_format", StringUtils.defaultIfBlank(responseFormat, "url"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> entity = new HttpEntity<>(gson.toJson(body), headers);
        ResponseEntity<Map> response = buildRestTemplate().postForEntity(buildOpenAiCompatibleUrl(), entity, Map.class);
        return readOpenAiCompatibleImages(response.getBody(), prompt, style);
    }

    private RestTemplate buildRestTemplate() {
        int timeout = timeoutSeconds == null ? 90000 : timeoutSeconds * 1000;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return new RestTemplate(factory);
    }

    private String buildOpenAiCompatibleUrl() {
        String path = StringUtils.defaultIfBlank(endpointPath, "/images/generations");
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return normalizedBaseUrl() + path;
    }

    private List<AiProductImageResponse.ImageItem> readOpenAiCompatibleImages(Map body, String prompt, String style) {
        if (body == null) {
            throw new IllegalStateException("AI image model response is empty");
        }
        Object data = body.get("data");
        if (!(data instanceof List)) {
            Object images = body.get("images");
            if (images instanceof List) {
                data = images;
            }
        }
        if (!(data instanceof List)) {
            throw new IllegalStateException("AI image model response has no data");
        }

        List<AiProductImageResponse.ImageItem> result = new ArrayList<>();
        collectImages(result, data, prompt, style, true);
        if (result.isEmpty()) {
            throw new IllegalStateException("AI image model response images are empty");
        }
        return result;
    }
}
