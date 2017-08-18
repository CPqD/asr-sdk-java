/*******************************************************************************
 * Copyright 2017 CPqD. All Rights Reserved.
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
package br.com.cpqd.asr.recognizer.sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.websocket.DeploymentException;

import br.com.cpqd.asr.recognizer.LanguageModelList;
import br.com.cpqd.asr.recognizer.MicAudioSource;
import br.com.cpqd.asr.recognizer.RecognitionException;
import br.com.cpqd.asr.recognizer.RecognitionListener;
import br.com.cpqd.asr.recognizer.SpeechRecognizer;
import br.com.cpqd.asr.recognizer.model.PartialRecognitionResult;
import br.com.cpqd.asr.recognizer.model.RecognitionAlternative;
import br.com.cpqd.asr.recognizer.model.RecognitionConfig;
import br.com.cpqd.asr.recognizer.model.RecognitionError;
import br.com.cpqd.asr.recognizer.model.RecognitionResult;

/**
 * Exemplo de uso do cliente Java SE do servidor de reconhecimento de fala,
 * utilizando uma entrada de áudio do tipo MicAudioSource, que captura áudio do
 * microfone do sistema. A captura é iniciada quando a tecla <ENTER> for
 * pressionada. A captura é finalizada ao pressionar novamente o <ENTER>.
 *
 */
public class MicAudioSourceSample {

	public static void main(String[] args) throws DeploymentException, IOException, URISyntaxException,
			RecognitionException, LineUnavailableException {

		if (!(args.length == 2 || args.length == 4)) {
			System.out.println("Usage: <ws_url> <lang_uri> [ <user> <password> ]");
			System.out.println("   eg: ws://127.0.0.1:8025/asr-server/asr builtin:grammar/samples/phone");
			System.out.println("  eg2: wss://contact/cpqd/and/request/a/key/ builtin:slm/general myusername mypassword");
			return;
		} else {
			System.out.println("Running with:");
			System.out.println("         url: " + args[0]);
			System.out.println("       lmURI: " + args[1]);
		}

		String url = args[0];
		String lmURI = args[1];
		String user = args[2];
		String pwd = args[3];

		SpeechRecognizer recognizer = getSpeechRecognizer(url, user, pwd);

		// modelo de linguagem de fala livre
		LanguageModelList lmList = LanguageModelList.builder().addFromURI(lmURI).build();

		// leitor da entrada do teclado para controlar o inicio e fim da captura do
		// microfone
		BufferedReader keyboardReader = new BufferedReader(new InputStreamReader(System.in));

		// realiza um reconhecimento
		try {
			// cria um objeto MicAudioSource para capturar audio do microfone do sistema
			MicAudioSource audioSource = new MicAudioSource(new AudioFormat(8000F, 16, 1, true, false));

			System.out.println("Press <ENTER> to start, and again to stop recording...");
			keyboardReader.readLine();
			recognizer.recognize(audioSource, lmList);
			System.err.println("line started");
			keyboardReader.readLine();
			audioSource.finish();
			System.err.println("line stopped");

			RecognitionResult result = recognizer.waitRecognitionResult().get(0);

			int i = 0;
			for (RecognitionAlternative alt : result.getAlternatives()) {
				System.out.println(
						String.format("Alternative [%s] (score = %s): %s", i++, alt.getConfidence(), alt.getText()));
			}
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

		RecognitionConfig config = RecognitionConfig.builder().maxSentences(2).build();

		// instancia do cliente
		SpeechRecognizer recognizer = SpeechRecognizer.builder().serverURL(url)
				// informacao opcional para log e estatisticas no servidor
				.userAgent("client=JavaSE;app=MicAudioSourceSample")
				.credentials(user, pwd)
				.recogConfig(config)
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
						System.out.println("Recognition result: " + result.getResultCode());
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
