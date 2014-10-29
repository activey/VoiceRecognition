package org.reactor.voice;

import static java.lang.System.currentTimeMillis;
import static javax.sound.sampled.AudioSystem.getAudioInputStream;
import static javax.sound.sampled.AudioSystem.getClip;
import static org.reactor.voice.VoiceCapture.capture;
import static org.reactor.voice.recognition.VoiceRecognizerFactory.create;

import java.io.*;
import java.util.Properties;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.reactor.voice.recognition.AbstractVoiceRecognizer;
import org.reactor.voice.recognition.VoiceRecognitionData;
import org.reactor.voice.recognition.VoiceRecognitionListener;
import org.reactor.voice.synthesis.VoiceSynthesiser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceControlTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceControlTest.class);
    private static final String LANGUAGE = "pl";

    private static final String SOUND_RECORDING_STARTED = "start.mp3";
    private static final String SOUND_RECORDING_STOPPED = "stop.mp3";

    private VoiceCapture voiceCapture;
    private AbstractVoiceRecognizer recognizer;
    private VoiceSynthesiser synthesiser;

    private long recognitionStart;

    private void initializeRecognizer() {
        recognizer = create(loadVoiceRecognitionProperties());
        recognizer.addListener(new VoiceRecognitionListener() {

            @Override
            public void onVoiceRecognized(VoiceRecognitionData recognitionData) {
                printRecognitionResponse(recognitionData);
            }

            @Override
            public void onError() {
                voiceCapture.resume();
            }
        });
    }

    private void printRecognitionResponse(VoiceRecognitionData recognitionData) {
        LOGGER.debug("Recognition took time: {} ms", currentTimeMillis() - recognitionStart);

        String recognitionResponse = recognitionData.getRecognizedVoice();
        if (recognitionResponse == null) {
            LOGGER.warn("Empty recognition response!");
            voiceCapture.resume();
            return;
        }
        LOGGER.debug("Recognition response: {}", recognitionResponse);
        playRecognitionText(recognitionResponse);
        voiceCapture.resume();
    }

    private Properties loadVoiceRecognitionProperties() {
        Properties properties = new Properties();
        try {
            InputStream voiceProperties = getClass().getClassLoader().getResourceAsStream("voice.properties");
            if (voiceProperties == null) {
                LOGGER.error("Unable to find properties file");
                return properties;
            }
            properties.load(voiceProperties);
        } catch (IOException e) {
            LOGGER.error("An error occurred while loading properties", e);
        }
        return properties;
    }

    private void initializeSynthesiser() {
        synthesiser = new VoiceSynthesiser();
        synthesiser.setLanguage(LANGUAGE.toString());
    }

    private void captureAndRecognize() {
        voiceCapture = capture(new VoiceCaptureListener() {

            @Override
            public void voiceCaptureStarted() {
                LOGGER.debug("STARTED SPEAKING");
                playSound(SOUND_RECORDING_STARTED);
            }

            @Override
            public void voiceCaptureEnded(File voiceFile) {
                LOGGER.debug("SPEAKING FINISHED");
                voiceCapture.pause();

                playSound(SOUND_RECORDING_STOPPED);
                recognizeVoiceInFile(voiceFile);

                voiceCapture.resume();
            }
        });
    }

    private void recognizeVoiceInFile(File voiceFile) {
        try {
            LOGGER.debug("Recognizing recorded speech");
            recognitionStart = currentTimeMillis();
            recognizer.recognizeVoice(voiceFile, voiceCapture.getSampleRate());
        } catch (IOException e) {
            LOGGER.error("An error occurred while recognizing voice", e);
        }
    }

//    private void playRecordedVoice(File voiceFile) {
//        try {
//            Clip clip = getClip();
//            LOGGER.debug("Playing back recorded voice from file: {}", voiceFile.getAbsolutePath());
//            AudioInputStream audioIn = getAudioInputStream(voiceFile);
//            clip.open(audioIn);
//            clip.start();
//        } catch (Exception e) {
//            LOGGER.error("An error occurred while playing back recorded voice", e);
//        }
//    }

    private void playRecognitionText(String recognitionResponse) {
        try {
            InputStream mp3File = synthesiser.getMP3Data(recognitionResponse);
            new Player(mp3File).play();
        } catch (IOException | JavaLayerException e) {
            LOGGER.error("An error occurred while playing recognized voice back", e);
        }
    }

    private void playSound(String soundFile) {
        try {
            playStream(getAudioFileStream(soundFile));
        } catch (Exception e) {
            LOGGER.error("An error occurred while playing sound {}", soundFile, e);
        }
    }

    private void playStream(InputStream stream) throws JavaLayerException {
        Player player = new Player(stream);
        player.play();
        player.close();
    }

    private InputStream getAudioFileStream(String soundFile) {
        return VoiceControlTest.class.getClassLoader().getResourceAsStream(soundFile);
    }

    public static void main(String[] args) {
        VoiceControlTest test = new VoiceControlTest();
        test.initializeRecognizer();
        test.initializeSynthesiser();
        test.captureAndRecognize();
    }
}
