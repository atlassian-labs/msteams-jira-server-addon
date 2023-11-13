package com.microsoft.teams.utils;

import com.google.api.client.http.HttpRequestFactory;
import com.microsoft.teams.config.PluginImageSettings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ImageHelperImpl implements ImageHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ImageHelperImpl.class);
    private static final String ICON_PATTERN = "(?:\\QiconUrl\":\"\\E)(.*?)\\Q\"\\E";
    private static final String AVATAR_PATTERN = "(?:\\Q48x48\":\"\\E|\\Q24x24\":\"\\E|\\Q16x16\":\"\\E|\\Q32x32\":\"\\E)(.*?)\\Q\"\\E";
    private static final String ICON_URL = "iconUrl";
    private static final String PRIORITIES = "priorities";
    private static final String PROJECT_AVATAR = "projectavatar";
    private static final long TEN_MB = 10000000;
    private static Map<String, String> iconUrlBase64Map = new HashMap<>();
    private ImageEncoder imageEncoder;
    private PluginImageSettings pluginImageSettings;

    @Autowired
    public ImageHelperImpl(ImageEncoder imageEncoder, PluginImageSettings pluginImageSettings) {
        this.imageEncoder = imageEncoder;
        this.pluginImageSettings = pluginImageSettings;
    }

    public String replaceImagesInJson(String jsonString, String baseUrl, HttpRequestFactory factory) {
         long start = System.currentTimeMillis();

        if (pluginImageSettings.hasChanged()) {
            iconUrlBase64Map = new HashMap<>();

            pluginImageSettings.resetObservableState();
        }

        if (pluginImageSettings.getEmbedIconsSetting()) {
            collectImages(jsonString, baseUrl, factory, ICON_PATTERN, iconUrlBase64Map);

            if (!iconUrlBase64Map.isEmpty()) {
                jsonString = getConvertedJson(jsonString, iconUrlBase64Map);
            }
        }

        if (pluginImageSettings.getEmbedProjectAvatarsSetting() || pluginImageSettings.getEmbedAvatarsSetting()) {
            collectImages(jsonString, baseUrl, factory, AVATAR_PATTERN, iconUrlBase64Map);

            if (!iconUrlBase64Map.isEmpty()) {
                jsonString = getConvertedJson(jsonString, iconUrlBase64Map);
            }
        }

        int length = jsonString.getBytes().length;
        if (length > TEN_MB) {
            LOG.debug("===> Images embedding took {}ms", System.currentTimeMillis() - start);
            LOG.debug("===> Payload size after images embedding: {} Kb", length / 1024);
        }

        return jsonString;
    }

    private String getConvertedJson(String jsonString, Map<String, String> iconUrlBase64Map) {
        return iconUrlBase64Map.entrySet().stream()
                .map(entryToReplace -> (Function<String, String>) s -> s.replace(entryToReplace.getKey(), entryToReplace.getValue()))
                .reduce(Function.identity(), Function::andThen)
                .apply(jsonString);
    }

    private void collectImages(String jsonString, String baseUrl, HttpRequestFactory factory, String patternStr, Map<String, String> iconUrlBase64Map) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(jsonString);
        addEncodedImageToMap(baseUrl, factory, iconUrlBase64Map, matcher);
    }

    private void addEncodedImageToMap(String baseUrl, HttpRequestFactory factory, Map<String, String> iconUrlBase64Map, Matcher matcher) {
        while (matcher.find()) {
            String iconUrlString = matcher.group(0);
            if (iconUrlString.contains(baseUrl) && !iconUrlBase64Map.containsKey(iconUrlString)) {
                String[] splitKeyValue = iconUrlString.split("\":\"");
                String iconUrl = splitKeyValue[1].replaceAll("\"", "");
                //TODO:bypass base url icons instead of filtering them
                if (!StringUtils.equals(iconUrl, baseUrl + "/")) {
                    String iconUrlKey = splitKeyValue[0];
                    if (iconUrlString.contains(ICON_URL)) {
                        iconUrlBase64Map.put(iconUrlString, String.format("%s\":\"%s\"", iconUrlKey, imageEncoder.encodeImageToBase64(iconUrl, factory)));
                    }
                    if (pluginImageSettings.getEmbedProjectAvatarsSetting() && !pluginImageSettings.getEmbedAvatarsSetting()) {
                        if (iconUrl.contains(PRIORITIES) || iconUrl.contains(PROJECT_AVATAR)) {
                            iconUrlBase64Map.put(iconUrlString, String.format("%s\":\"%s\"", iconUrlKey, imageEncoder.encodeImageToBase64(iconUrl, factory)));
                        }
                    }
                    if (pluginImageSettings.getEmbedAvatarsSetting()) {
                        if (!pluginImageSettings.getEmbedProjectAvatarsSetting()) {
                            if (!(iconUrl.contains(PRIORITIES) || iconUrl.contains(PROJECT_AVATAR))) {
                                iconUrlBase64Map.put(iconUrlString, String.format("%s\":\"%s\"", iconUrlKey, imageEncoder.encodeImageToBase64(iconUrl, factory)));
                            }
                        } else {
                            iconUrlBase64Map.put(iconUrlString, String.format("%s\":\"%s\"", iconUrlKey, imageEncoder.encodeImageToBase64(iconUrl, factory)));
                        }
                    }
                }
            }
        }
    }
}
