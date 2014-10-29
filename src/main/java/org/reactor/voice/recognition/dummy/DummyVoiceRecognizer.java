package org.reactor.voice.recognition.dummy;

import org.reactor.voice.recognition.AbstractVoiceRecognizer;
import org.reactor.voice.recognition.VoiceRecognitionData;

import java.util.Properties;

public class DummyVoiceRecognizer extends AbstractVoiceRecognizer {

    public static final String DUMMY_RECOGNITION = "yyyyyyy y.";

    @Override
    public void configureRecognizer(Properties properties) {

    }

    @Override
    protected void doRecognizeVoice(byte[] voiceData, int sampleRate) {
        voiceRecognized(new VoiceRecognitionData(DUMMY_RECOGNITION));
    }
}
