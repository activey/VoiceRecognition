package org.reactor.voice;

import static java.io.File.createTempFile;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import org.reactor.voice.microphone.Microphone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceCapture {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceCapture.class);

    private static final int DEFAULT_SILENCE_SECONDS = 1;
    private static final String CAPTURE_FILE_PREFIX = "capture";
    private static final String CAPTURE_FILE_EXTENSION = ".flac";

    private final VoiceCaptureListener captureListener;
    private Microphone microphone;
    private SpeakingMonitor speakingMonitor;

    public static VoiceCapture capture(int silenceDelayMs, VoiceCaptureListener captureListener) {
        return new VoiceCapture(silenceDelayMs, captureListener);
    }

    public static VoiceCapture capture(VoiceCaptureListener captureListener) {
        return new VoiceCapture(SECONDS.toMillis(DEFAULT_SILENCE_SECONDS), captureListener);
    }

    private VoiceCapture(long silenceDelayMs, VoiceCaptureListener captureListener) {
        this.captureListener = captureListener;
        try {
            initializeMicrophone();
            initializeSpeakingMonitor(silenceDelayMs);
        } catch (IOException | LineUnavailableException e) {
            LOGGER.error("An error has occurred while initializing microphone", e);
        }
    }

    private void initializeMicrophone() throws LineUnavailableException, IOException {
        LOGGER.info("Initializing microphone");
        microphone = new Microphone();
        microphone.listenStart();

        LOGGER.info("Initialized microphone, sample rate = {}", microphone.getSampleRate());
    }

    private void initializeSpeakingMonitor(long silenceDelayMilis) {
        speakingMonitor = new SpeakingMonitor(microphone, silenceDelayMilis) {

            @Override
            public void onSpeakingStarted() {
                captureListener.voiceCaptureStarted();
            }

            @Override
            public void onSpeakingFinished() {
                captureRecordedVoice();
            }

            @Override
            public void onSilenceOnly(long silenceSoFar) {
                LOGGER.debug("Silence so far: {} ms", silenceSoFar);
                restartMicrophone();
            }
        };
    }

    private void captureRecordedVoice() {
        File captureFile = null;
        try {
            captureFile = createCaptureFile();
            microphone.listenStop();
            microphone.captureSoundBuffer(captureFile);
            captureListener.voiceCaptureEnded(captureFile);
            microphone.listenStart();
        } catch (LineUnavailableException | IOException e) {
            LOGGER.error("An error has occurred while capturing recorded voice", e);
        } finally {
            removeCaptureFile(captureFile);
        }
    }

    private void removeCaptureFile(File captureFile) {
        if (!captureFile.delete()) {
            LOGGER.debug("Unable to remove capture file: {}", captureFile.getAbsolutePath());
            return;
        }
        LOGGER.debug("Removed capture file: {}", captureFile.getAbsolutePath());
    }

    private void restartMicrophone() {
        try {
            microphone.listenStop();
            microphone.listenStart();
        } catch (LineUnavailableException e) {
            LOGGER.error("An error occurred while restarting voice capture", e);
        }
    }

    private File createCaptureFile() throws IOException {
        File homeDirectory = new File(System.getProperty("user.home"));
        File captureFile = createTempFile(CAPTURE_FILE_PREFIX, CAPTURE_FILE_EXTENSION, homeDirectory);
        LOGGER.debug("Created temporary capture file: {}", captureFile.getAbsolutePath());
        return captureFile;
    }

    public int getSampleRate() {
        return (int) microphone.getSampleRate();
    }

    public void pause() {
        speakingMonitor.pause();
        LOGGER.debug("pausing");
    }

    public void resume() {
        speakingMonitor.resume();
        LOGGER.debug("resuming");
    }
}
