package org.reactor.voice.microphone;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MicrophoneSoundBuffer {

    private ByteArrayOutputStream bufferBytes = new ByteArrayOutputStream();

    public InputStream getSoundBufferInputStream() {
        return new ByteArrayInputStream(bufferBytes.toByteArray());
    }

    public synchronized void write(byte[] bytes) throws IOException {
        bufferBytes.write(bytes);
    }

    public void reset() {
        bufferBytes = new ByteArrayOutputStream();
    }
}
