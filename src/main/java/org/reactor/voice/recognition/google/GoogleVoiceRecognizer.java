package org.reactor.voice.recognition.google;

import static org.reactor.voice.recognition.google.GoogleResponse.FROM_GOOGLE_RESPONSE;
import static org.reactor.voice.recognition.google.GoogleResponseBuilder.fromJSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.reactor.voice.recognition.AbstractVoiceRecognizer;

/**
 * TODO needs refactoring
 */
public class GoogleVoiceRecognizer extends AbstractVoiceRecognizer {

    private static final long MIN = 10000000;
    private static final long MAX = 900000009999999L;
    private static final String GOOGLE_DUPLEX_SPEECH_BASE = "https://www.google.com/speech-api/full-duplex/v1/";

    private String apiKey;
    private String language;

    @Override
    public void configureRecognizer(Properties properties) {
        configureGoogleVoiceRecognizer(new GoogleVoiceRecognizerConfiguration(properties));
    }

    private void configureGoogleVoiceRecognizer(GoogleVoiceRecognizerConfiguration recognizerConfiguration) {
        apiKey = recognizerConfiguration.getApiKey();
        language = recognizerConfiguration.getLanguage();
    }

    public void doRecognizeVoice(byte[] data, int sampleRate) {
        long PAIR = MIN + (long) (Math.random() * ((MAX - MIN) + 1L));
        String API_DOWN_URL = GOOGLE_DUPLEX_SPEECH_BASE + "down?maxresults=1&pair=" + PAIR;
        String API_UP_URL = GOOGLE_DUPLEX_SPEECH_BASE + "up?lang=" + language + "&lm=dictation&client=chromium&pair="
                + PAIR + "&key=" + apiKey;

        downChannel(API_DOWN_URL);
        upChannel(API_UP_URL, data, sampleRate);
    }

    private void downChannel(String urlStr) {
        final String url = urlStr;
        new Thread() {

            public void run() {
                Scanner inStream = openHttpsConnection(url);
                if (inStream == null) {
                    // TODO error!
                    return;
                }
                while (inStream.hasNextLine()) {
                    JSONObject jsonObject = new JSONObject(inStream.nextLine());
                    System.out.println(jsonObject.toString());
                    GoogleResponse googleResponse = fromJSONObject(jsonObject).build();
                    if (googleResponse.isEmpty()) {
                        continue;
                    }
                    voiceRecognized(FROM_GOOGLE_RESPONSE.apply(googleResponse));
                    return;
                }
                voiceNotRecognized();
            }
        }.start();
    }

    private void upChannel(String urlStr, byte[] data, int sampleRate) {
        final String murl = urlStr;
        final byte[] mdata = data;
        final int mSampleRate = sampleRate;
        new Thread() {

            public void run() {
                openHttpsPostConnection(murl, mdata, mSampleRate);
            }
        }.start();
    }

    private Scanner openHttpsConnection(String urlStr) {
        try {
            URL url = new URL(urlStr);
            URLConnection urlConn = url.openConnection();
            if (!(urlConn instanceof HttpsURLConnection)) {
                throw new IOException("URL is not an Https URL");
            }
            HttpsURLConnection httpConn = (HttpsURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");

            httpConn.connect();
            int resCode = httpConn.getResponseCode();
            if (resCode == HttpsURLConnection.HTTP_OK) {
                return new Scanner(httpConn.getInputStream());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Scanner openHttpsPostConnection(String urlStr, byte[] data, int sampleRate) {
        byte[] mextrad = data;
        int resCode = -1;
        OutputStream out = null;
        try {
            URL url = new URL(urlStr);
            URLConnection urlConn = url.openConnection();
            if (!(urlConn instanceof HttpsURLConnection)) {
                throw new IOException("URL is not an Https URL");
            }
            HttpsURLConnection httpConn = (HttpsURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setChunkedStreamingMode(0);
            httpConn.setRequestProperty("Content-Type", "audio/x-flac; rate=" + sampleRate);
            httpConn.connect();
            try {
                out = httpConn.getOutputStream();
                out.write(mextrad);
                resCode = httpConn.getResponseCode();
                if (resCode / 100 != 2) {
                    System.out.println("ERROR");
                }
            } catch (IOException e) {

            }
            if (resCode == HttpsURLConnection.HTTP_OK) {
                return new Scanner(httpConn.getInputStream());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
