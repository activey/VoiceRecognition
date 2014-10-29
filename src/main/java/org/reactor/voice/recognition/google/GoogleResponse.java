package org.reactor.voice.recognition.google;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.reactor.voice.recognition.VoiceRecognitionData;

public class GoogleResponse {

    public final static Function<GoogleResponse, VoiceRecognitionData> FROM_GOOGLE_RESPONSE = googleResponse -> new VoiceRecognitionData(
        googleResponse.getResponse());

    private String response;
    private String confidence;
    private List<String> otherPossibleResponses = new ArrayList<>();

    public String getResponse() {
        return response;
    }

    protected void setResponse(String response) {
        this.response = response;
    }

    public String getConfidence() {
        return confidence;
    }

    protected void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public List<String> getOtherPossibleResponses() {
        return otherPossibleResponses;
    }

    public void addOtherPossibleResponse(String possibleResponse) {
        otherPossibleResponses.add(possibleResponse);
    }

    public List<String> getAllPossibleResponses() {
        otherPossibleResponses.add(0, response);
        return otherPossibleResponses;
    }

    public boolean isEmpty() {
        return response == null || response.trim().length() == 0;
    }
}
