package org.reactor.voice.microphone;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static javaFlacEncoder.FLACFileWriter.FLAC;
import static javax.sound.sampled.AudioSystem.NOT_SPECIFIED;
import static javax.sound.sampled.AudioSystem.getMixer;
import static javax.sound.sampled.Mixer.Info;
import static org.reactor.voice.microphone.MicrophoneState.CLOSED;
import static org.reactor.voice.microphone.MicrophoneState.LISTENING;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import javax.sound.sampled.*;

import org.reactor.voice.MixerMissingException;

public class Microphone implements AutoCloseable {

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(16000, 16, 1, true, false);
    private static final DataLine.Info TARGET_DL_INFO = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);

    private Mixer mixer;
    private TargetDataLine targetRecordLine;
    private MicrophoneSoundBuffer soundBuffer;
    private ExecutorService listenExecutorService;
    private MicrophoneState state = CLOSED;
    private double volumeLevel;

    private static Info getFirstMixerInfo() {
        for (Info mixerInfo : AudioSystem.getMixerInfo()) {
            return mixerInfo;
        }
        throw new MixerMissingException();
    }

    private static Info getMixerInfo(String mixerName) {
        Info[] allMixers = AudioSystem.getMixerInfo();
        for (Info mixerInfo : allMixers) {
            if (mixerName.equals(mixerInfo.getName())) {
                return mixerInfo;
            }
        }
        throw new MixerMissingException(mixerName);
    }

    public Microphone() {
        this(getFirstMixerInfo());
    }

    public Microphone(String mixerName) {
        this(getMixerInfo(mixerName));
    }

    public Microphone(Info mixerInfo) {
        initializeMixer(mixerInfo);
        initializeListenExecutorService();
    }

    private void initializeMixer(Info mixerInfo) {
        mixer = getMixer(mixerInfo);
    }

    private void initializeListenExecutorService() {
        listenExecutorService = newSingleThreadExecutor();
    }

    public void listenStart() throws LineUnavailableException {
        if (verifyState(LISTENING)) {
            return;
        }
        initializeTargetRecordLine();
        initializeSoundBuffer();

        listenExecutorService.submit(new MicrophoneVolumeListenerRunnable(targetRecordLine, soundBuffer,
            newVolumeLevel -> volumeLevel = newVolumeLevel));
        changeState(LISTENING);
    }

    private void initializeSoundBuffer() {
        if (soundBuffer == null) {
            soundBuffer = new MicrophoneSoundBuffer();
        } else {
            soundBuffer.reset();
        }
    }

    private void initializeTargetRecordLine() throws LineUnavailableException {
        if (targetRecordLine == null) {
            targetRecordLine = (TargetDataLine) mixer.getLine(TARGET_DL_INFO);
        }
        targetRecordLine.open();
        targetRecordLine.start();
    }

    public void listenStop() {
        if (verifyState(CLOSED)) {
            return;
        }
        targetRecordLine.stop();
        targetRecordLine.close();
        changeState(CLOSED);
    }

    public void close() {
        mixer.close();
        listenExecutorService.shutdown();
    }

    public void captureSoundBuffer(File captureFile) throws IOException {
        AudioSystem.write(new AudioInputStream(soundBuffer.getSoundBufferInputStream(), AUDIO_FORMAT, NOT_SPECIFIED),
            FLAC, captureFile);
    }

    private boolean verifyState(MicrophoneState wantedState) {
        return state == wantedState;
    }

    private void changeState(MicrophoneState newState) {
        state = newState;
    }

    public double getVolumeLevel() {
        return volumeLevel;
    }

    public float getSampleRate() {
        return AUDIO_FORMAT.getSampleRate();
    }
}
