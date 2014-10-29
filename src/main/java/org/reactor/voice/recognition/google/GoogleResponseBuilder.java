package org.reactor.voice.recognition.google;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class GoogleResponseBuilder {

    private List<String> possibleResponses = new ArrayList<String>();
    private String transcript;
    private double confidence;

    public static GoogleResponseBuilder fromJSONObject(JSONObject jsonObject) {
        GoogleResponseBuilder responseBuilder = new GoogleResponseBuilder();
        if (!jsonObject.has("result_index")) {
            return responseBuilder;
        }
        int resultIndex = jsonObject.getInt("result_index");
        JSONArray resultsArray = jsonObject.getJSONArray("result");
        JSONArray alternatives = resultsArray.getJSONObject(0).getJSONArray("alternative");

        for (int index = 0; index < alternatives.length(); index++) {
            JSONObject alternative = alternatives.getJSONObject(index);
            if (index == resultIndex) {
                responseBuilder.transcript(alternative.getString("transcript")).confidence(
                        alternative.optDouble("confidence", 0));
            } else {
                responseBuilder.possibleResponse(alternative.getString("transcript"));
            }
        }
        return responseBuilder;
    }

    private GoogleResponseBuilder possibleResponse(String transcript) {
        possibleResponses.add(transcript);
        return this;
    }

    private GoogleResponseBuilder confidence(double confidence) {
        this.confidence = confidence;
        return this;
    }

    private GoogleResponseBuilder transcript(String transcript) {
        this.transcript = transcript;
        return this;
    }

    private GoogleResponseBuilder() {

    }

    public GoogleResponse build() {
        GoogleResponse response = new GoogleResponse();
        response.setResponse(transcript);
        response.setConfidence("" + confidence);
        response.getOtherPossibleResponses().addAll(possibleResponses);
        return response;
    }
}
