package org.reactor.voice.microphone;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;

public class MicrophoneVolumeListenerRunnable implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(MicrophoneVolumeListenerRunnable.class);

    private final TargetDataLine targetDataLine;
    private final MicrophoneSoundBuffer soundBuffer;
    private final MicrophoneVolumeChangedListener volumeCallback;

    private boolean captureActive = true;

    public MicrophoneVolumeListenerRunnable(TargetDataLine targetDataLine, MicrophoneSoundBuffer soundBuffer,
                                            MicrophoneVolumeChangedListener volumeCallback) {
        this.targetDataLine = targetDataLine;
        this.soundBuffer = soundBuffer;
        this.volumeCallback = volumeCallback;
    }

    @Override
    public void run() {
        AudioInputStream stream = new AudioInputStream(targetDataLine);
        byte tempBuffer[] = new byte[500];
        while (captureActive) {
            try {
                int cnt = stream.read(tempBuffer);
                if (cnt > 0) {
                    soundBuffer.write(tempBuffer);
                    volumeCallback.volumeChanged(calculateRMSLevel(tempBuffer));
                }
            } catch (IOException e) {
                LOGGER.error("An error occurred while reading from microphone", e);
            }
        }
    }

    /**
     * Magic happens here.
     *
     * @param audioData
     * @return
     */
    private double calculateRMSLevel(byte[] audioData) {
        long lSum = 0;
        for (int i = 0; i < audioData.length; i++) {
            lSum = lSum + audioData[i];
        }

        double dAvg = lSum / audioData.length;

        double sumMeanSquare = 0d;
        for (int j = 0; j < audioData.length; j++) {
            sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - dAvg, 2d);
        }
        double averageMeanSquare = sumMeanSquare / audioData.length;
        return Math.pow(averageMeanSquare, 0.5d) + 0.5;
    }
}
