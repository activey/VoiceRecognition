package org.reactor.voice.recognition;

public class VoiceRecognitionData {

    private final String recognizedVoice;

    public VoiceRecognitionData(String recognizedVoice) {
        this.recognizedVoice = recognizedVoice;
    }

    public String getRecognizedVoice() {
        return recognizedVoice;
    }
}
