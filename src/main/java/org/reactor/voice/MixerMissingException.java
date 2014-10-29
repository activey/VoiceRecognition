package org.reactor.voice;

import static java.lang.String.format;

public class MixerMissingException extends RuntimeException {

    private static final String MISSING_MIXER_MESSAGE = "Missing mixer with name: %s";
    private static final String MISSING_ANY_MIXER_MESSAGE = "Cant find any mixer";

    public MixerMissingException(String missingMixerName) {
        super(format(MISSING_MIXER_MESSAGE, missingMixerName));
    }

    public MixerMissingException() {
        super(MISSING_ANY_MIXER_MESSAGE);
    }
}
