/*******************************************************************************
 * Copyright 2018 CPqD. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package br.com.cpqd.asr.recognizer.example;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.websocket.DeploymentException;

import br.com.cpqd.asr.recognizer.AudioSource;
import br.com.cpqd.asr.recognizer.FileAudioSource;
import br.com.cpqd.asr.recognizer.LanguageModelList;
import br.com.cpqd.asr.recognizer.RecognitionException;
import br.com.cpqd.asr.recognizer.RecognitionListener;
import br.com.cpqd.asr.recognizer.SpeechRecognizer;
import br.com.cpqd.asr.recognizer.model.PartialRecognitionResult;
import br.com.cpqd.asr.recognizer.model.RecognitionAlternative;
import br.com.cpqd.asr.recognizer.model.RecognitionConfig;
import br.com.cpqd.asr.recognizer.model.RecognitionError;

/**
 * Exemplo de uso do cliente Java SE com áudio de múltiplos segmentos de fala.
 * 
 */
public class ContinuousModeSample {

	public static void main(String[] args)
			throws DeploymentException, IOException, URISyntaxException, RecognitionException {

		if (!(args.length == 3 || args.length == 5)) {
			System.out.println("Usage: <ws_url> <lang_uri> <wav_path> [ <user> <password> ]");
			System.out.println(
					"   eg: ws://127.0.0.1:8025/asr-server/asr builtin:grammar/samples/phone /path/to/audio.wav");
			System.out.println(
					"  eg2: wss://contact/cpqd/and/request/a/key/ builtin:slm/general /path/to/audio.wav myusername mypassword");
			return;
		} else {
			System.out.println("Running with:");
			System.out.println("         url: " + args[0]);
			System.out.println("       lmURI: " + args[1]);
			System.out.println("       audio: " + args[2]);
		}

		String url = args[0];
		String lmURI = args[1];
		String audioFile = args[2];
		String user = null;
		String pwd = null;
		try {
			user = args[3];
			pwd = args[4];
		} catch (Exception e) {
			// credentials not defined
		}

		// instancia do cliente
		SpeechRecognizer recognizer = getSpeechRecognizer(url, user, pwd);

		// modelo de linguagem (conforme instalado no ambiente do servidor)
		LanguageModelList lmList = LanguageModelList.builder().addFromURI(lmURI).build();

		// realiza um reconhecimento
		try {
			// pode ser de qualquer fonte (neste caso é um arquivo)
			AudioSource audio = new FileAudioSource(new File(audioFile));
			// envia o audio para o servidor
			recognizer.recognize(audio, lmList);
			// aguarda o resultado do reconhecimento
			recognizer.waitRecognitionResult();

		} catch (IOException e) {
			System.out.println("Error reading " + audioFile);
		} catch (RecognitionException e) {
			System.err.println("Generic error: " + e.toString());
		} finally {
			// se nao tem mais audio para reconhecer, encerra a sessao
			System.out.println("Closing session");
			recognizer.close();
		}

	}

	private static SpeechRecognizer getSpeechRecognizer(String url, String user, String pwd)
			throws URISyntaxException, IOException, RecognitionException {

		RecognitionConfig config = RecognitionConfig.builder().maxSentences(1).continuousMode(true).build();

		// instancia do cliente
		SpeechRecognizer recognizer = SpeechRecognizer.builder().serverURL(url)
				// informacao opcional para log e estatisticas no servidor
				.userAgent("client=JavaSE;app=ContinuousModeSample").credentials(user, pwd).recogConfig(config)
				.addListener(new RecognitionListener() {

					@Override
					public void onSpeechStop(Integer time) {
						System.out.println(String.format("End of speech"));
					}

					@Override
					public void onSpeechStart(Integer time) {
						System.out.println(String.format("Speech started"));
					}

					@Override
					public void onRecognitionResult(br.com.cpqd.asr.recognizer.model.RecognitionResult result) {
						System.out.println(String.format("[%s] Recognition result: %s", result.getSpeechSegmentIndex(),
								result.getResultCode()));
						for (RecognitionAlternative alt : result.getAlternatives()) {
							System.out.println(String.format("    %s", alt.getText()));
						}
					}

					@Override
					public void onPartialRecognitionResult(PartialRecognitionResult result) {
						System.out.println(String.format("Partial result: %s", result.getText()));
					}

					@Override
					public void onListening() {
						System.out.println("Server is listening");
					}

					@Override
					public void onError(RecognitionError error) {
						System.out.println(
								String.format("Recognition error: [%s] %s", error.getCode(), error.getMessage()));

					}
				}).build();

		return recognizer;
	}

}
