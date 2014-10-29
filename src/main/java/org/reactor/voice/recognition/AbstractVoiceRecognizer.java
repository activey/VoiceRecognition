package org.reactor.voice.recognition;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public abstract class AbstractVoiceRecognizer {

    private List<VoiceRecognitionListener> recognitionListeners = new ArrayList();

    public abstract void configureRecognizer(Properties properties);

    public final void recognizeVoice(File voiceFile, int sampleRate) throws IOException {
        doRecognizeVoice(mapFileIn(voiceFile), sampleRate);
    }

    protected abstract void doRecognizeVoice(byte[] voiceData, int sampleRate);

    protected final void voiceRecognized(VoiceRecognitionData recognitionData) {
        for (VoiceRecognitionListener listener : recognitionListeners) {
            listener.onVoiceRecognized(recognitionData);
        }
    }

    protected final void voiceNotRecognized() {
        for (VoiceRecognitionListener listener : recognitionListeners) {
            listener.onError();
        }
    }

    public final void addListener(VoiceRecognitionListener listener) {
        recognitionListeners.add(listener);
    }

    public final void removeListener(VoiceRecognitionListener listener) {
        recognitionListeners.remove(listener);
    }

    private byte[] mapFileIn(File infile) throws IOException {
        FileInputStream fileInputStream = null;
        FileChannel fileChannel = null;
        try {
            fileInputStream = new FileInputStream(infile);
            fileChannel = fileInputStream.getChannel();
            MappedByteBuffer byteBuffer = fileChannel.map(READ_ONLY, 0, fileChannel.size());

            byte[] fileData = new byte[byteBuffer.remaining()];
            byteBuffer.get(fileData);
            return fileData;
        } finally {
            try {
                fileInputStream.close();
                fileChannel.close();
            } catch (Exception e) {}
        }
    }
}
