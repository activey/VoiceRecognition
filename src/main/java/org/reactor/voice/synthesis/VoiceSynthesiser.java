package org.reactor.voice.synthesis;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class VoiceSynthesiser {

    private final static String GOOGLE_SYNTHESISER_URL = "http://translate.google.com/translate_tts?tl=";

    private String languageCode;

    public VoiceSynthesiser() {
        languageCode = "auto";
    }

    public VoiceSynthesiser(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getLanguage() {
        return languageCode;
    }

    public void setLanguage(String languageCode) {
        this.languageCode = languageCode;
    }

    public InputStream getMP3Data(String synthText) throws IOException {
        if (synthText.length() > 100) {
            List<String> fragments = parseString(synthText);
            InputStream out = getMP3Data(fragments);
            return out;
        }
        String encoded = URLEncoder.encode(synthText, "UTF-8");
        URL url = new URL(GOOGLE_SYNTHESISER_URL + languageCode + "&q=" + encoded);
        URLConnection urlConn = url.openConnection();
        urlConn.addRequestProperty("User-Agent",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0) Gecko/20100101 Firefox/4.0");
        return urlConn.getInputStream();
    }

    public InputStream getMP3Data(List<String> synthText) throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(1000);
        Set<Future<InputStream>> set = new LinkedHashSet<Future<InputStream>>(synthText.size());
        for (String part : synthText) {
            Callable<InputStream> callable = new MP3DataFetcher(part);
            Future<InputStream> future = pool.submit(callable);
            set.add(future);
        }
        List<InputStream> inputStreams = new ArrayList<>(set.size());
        for (Future<InputStream> future : set) {
            try {
                inputStreams.add(future.get());
            } catch (ExecutionException e) {
                Throwable ex = e.getCause();
                if (ex instanceof IOException) {
                    throw (IOException) ex;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return new SequenceInputStream(Collections.enumeration(inputStreams));
    }

    private List<String> parseString(String input) {
        return parseString(input, new ArrayList<>());
    }

    private List<String> parseString(String input, List<String> fragments) {
        if (input.length() <= 100) {
            fragments.add(input);
            return fragments;
        } else {
            int lastWord = findLastWord(input);
            if (lastWord <= 0) {
                fragments.add(input.substring(0, 100));
                return parseString(input.substring(100), fragments);
            } else {
                fragments.add(input.substring(0, lastWord));
                return parseString(input.substring(lastWord), fragments);
            }
        }
    }

    private int findLastWord(String input) {
        if (input.length() < 100) return input.length();
        int space = -1;
        for (int i = 99; i > 0; i--) {
            char tmp = input.charAt(i);
            if (isEndingPunctuation(tmp)) {
                return i + 1;
            }
            if (space == -1 && tmp == ' ') {
                space = i;
            }
        }
        if (space > 0) {
            return space;
        }
        return -1;
    }

    private boolean isEndingPunctuation(char input) {
        return input == '.' || input == '!' || input == '?' || input == ';' || input == ':' || input == '|';
    }

    private class MP3DataFetcher implements Callable<InputStream> {

        private String synthText;

        public MP3DataFetcher(String synthText) {
            this.synthText = synthText;
        }

        public InputStream call() throws IOException {
            return getMP3Data(synthText);
        }
    }

}
