package br.com.cpqd.asr.recognizer;

import br.com.cpqd.asr.recognizer.model.PartialRecognitionResult;
import br.com.cpqd.asr.recognizer.model.RecognitionError;
import br.com.cpqd.asr.recognizer.model.RecognitionResult;

public class SimpleRecognizerListener implements RecognitionListener {

	@Override
	public void onListening() {
	}

	@Override
	public void onSpeechStart(Integer time) {
	}

	@Override
	public void onSpeechStop(Integer time) {
	}

	@Override
	public void onPartialRecognitionResult(PartialRecognitionResult result) {
	}

	@Override
	public void onRecognitionResult(RecognitionResult result) {
	}

	@Override
	public void onError(RecognitionError error) {
	}

}
