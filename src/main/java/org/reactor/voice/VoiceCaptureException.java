package org.reactor.voice;

public class VoiceCaptureException extends RuntimeException {

    public VoiceCaptureException(Exception rootException) {
        super(rootException);
    }
}
