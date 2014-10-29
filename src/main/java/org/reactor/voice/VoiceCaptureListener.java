package org.reactor.voice;

import java.io.File;

public interface VoiceCaptureListener {

    void voiceCaptureStarted();

    void voiceCaptureEnded(File capturedVoiceFile);
}
