package org.reactor.voice;

import java.util.concurrent.ExecutorService;

import org.reactor.voice.microphone.Microphone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class SpeakingMonitor implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpeakingMonitor.class);

    private static final int VOLUME_SILENT = 55;
    private static final int TOTAL_SILENCE_FACTOR = 3;

    private final Microphone microphone;
    private final long silenceDuration;

    private ExecutorService executorService;
    private boolean active = true;
    private boolean paused = false;
    private boolean speaking = false;
    private boolean goingSilent = true;
    private long silenceStartedTimestamp = currentTimeMillis();

    public SpeakingMonitor(Microphone microphone, long silenceDuration) {
        this.microphone = microphone;
        this.silenceDuration = silenceDuration;

        initializeMonitorExecutor();
    }

    private void initializeMonitorExecutor() {
        executorService = newSingleThreadExecutor();
        executorService.execute(this);
    }

    public void shutDown() {
        active = false;
        executorService.shutdown();
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    @Override
    public void run() {
        while (active) {
            if (paused) {
                goIdle();
                continue;
            }
            if (isSpeaking()) {
                notifySpeaking();
            } else {
                notifySilent();
            }
            goIdle();
        }
        LOGGER.debug("Shutting down speaking monitor ...");
    }

    private void goIdle() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            LOGGER.error("An error occurred while going idle", e);
        }
    }

    private void notifySpeaking() {
        goingSilent = false;
        if (speaking) {
            return;
        }
        speaking = true;
        onSpeakingStarted();
    }

    private boolean isSpeaking() {
//        System.out.println(microphone.getVolumeLevel());
        return microphone.getVolumeLevel() > VOLUME_SILENT;
    }

    private void notifySilent() {
        if (speaking) {
            if (goingSilent) {
                long silenceSoFar = currentTimeMillis() - silenceStartedTimestamp;
                if (silenceSoFar > silenceDuration) {
                    onSpeakingFinished();
                    speaking = false;
                }
            } else {
                goingSilent = true;
                silenceStartedTimestamp = currentTimeMillis();
            }
        } else {
            long silenceSoFar = currentTimeMillis() - silenceStartedTimestamp;
            if ((silenceSoFar % (TOTAL_SILENCE_FACTOR * silenceDuration)) == 0) {
                onSilenceOnly(silenceSoFar);
                speaking = false;
                goingSilent = false;
            }
        }
    }

    protected void onSilenceOnly(long silenceSoFar) { }

    protected void onSpeakingFinished() { }

    protected void onSpeakingStarted() { }
}
