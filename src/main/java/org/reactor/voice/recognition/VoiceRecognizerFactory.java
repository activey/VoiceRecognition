package org.reactor.voice.recognition;

import org.reactor.voice.recognition.dummy.DummyVoiceRecognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.ServiceLoader;

public class VoiceRecognizerFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(VoiceRecognizerFactory.class);

    public static AbstractVoiceRecognizer create(Properties recognizerProperties) {
        VoiceRecognizerFactory factory = new VoiceRecognizerFactory();
        ServiceLoader<AbstractVoiceRecognizer> recognizerLoader = ServiceLoader.load(AbstractVoiceRecognizer.class);
        recognizerLoader.forEach(factory::loadRecognizer);

        AbstractVoiceRecognizer recognizer = factory.loadedRecognizer;
        if (recognizer == null) {
            return createDefaultRecognizer();
        }
        recognizer.configureRecognizer(recognizerProperties);
        return recognizer;
    }

    private AbstractVoiceRecognizer loadedRecognizer;

    private void loadRecognizer(AbstractVoiceRecognizer recognizer) {
        if (loadedRecognizer != null) {
            return;
        }
        LOGGER.debug("Loading recognizer implementation: {}", recognizer.getClass().getName());
        loadedRecognizer = recognizer;
    }

    private static AbstractVoiceRecognizer createDefaultRecognizer() {
        return new DummyVoiceRecognizer();
    }

    private VoiceRecognizerFactory() { }
}
