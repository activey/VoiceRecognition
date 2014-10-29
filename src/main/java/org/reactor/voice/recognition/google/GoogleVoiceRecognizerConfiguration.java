package org.reactor.voice.recognition.google;

import java.util.Properties;

public class GoogleVoiceRecognizerConfiguration {

    private static final String KEY_API_KEY = "googleRecognizer.apiKey";
    private static final String KEY_LANGUAGE = "googleRecognizer.language";

    private final String apiKey;
    private final String language;

    public GoogleVoiceRecognizerConfiguration(Properties properties) {
        apiKey = properties.getProperty(KEY_API_KEY);
        language = properties.getProperty(KEY_LANGUAGE);
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getLanguage() {
        return language;
    }
}
