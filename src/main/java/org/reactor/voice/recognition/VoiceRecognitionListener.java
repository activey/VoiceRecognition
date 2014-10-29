package org.reactor.voice.recognition;

public interface VoiceRecognitionListener {
	
	public void onVoiceRecognized(VoiceRecognitionData recognitionData);

    public void onError();
}
