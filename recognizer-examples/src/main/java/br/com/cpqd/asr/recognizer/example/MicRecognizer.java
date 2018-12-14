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
package br.com.cpqd.asr.recognizer.example;

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
import br.com.cpqd.asr.recognizer.SimpleRecognizerListener;
import br.com.cpqd.asr.recognizer.SpeechRecognizer;
import br.com.cpqd.asr.recognizer.model.RecognitionConfig;
import br.com.cpqd.asr.recognizer.model.RecognitionResult;

/**
 * Exemplo de uso do cliente Java SE do servidor de reconhecimento de fala,
 * utilizando uma entrada de áudio do tipo MicAudioSource, que captura áudio do
 * microfone do sistema. A captura é iniciada quando a tecla <ENTER> for
 * pressionada. A captura é finalizada quando o usuário termina de falar.
 *
 */
public class MicRecognizer {

	public static void main(String[] args) throws DeploymentException, IOException, URISyntaxException,
			RecognitionException, LineUnavailableException {

		if (args.length == 0) {
			System.err.println("Usage: MicRecognizer --server <Server URL> --lm <LM URI> [--user <username> --pwd <password>]");
			System.err.println(" e.g.: MicRecognizer --server ws://127.0.0.1:8025/asr-server/asr --lm builtin:slm/general");
			return;
		}
		
		ProgramArguments pa = ProgramArguments.from(args);
		
		RecognitionConfig config = RecognitionConfig.builder().maxSentences(1).confidenceThreshold(70).build();
		MicAudioSource audio = new MicAudioSource(new AudioFormat(8000F, 16, 1, true, false));
		LanguageModelList lm = LanguageModelList.builder().addFromURI(pa.getArg("lm")).build();
		
		SpeechRecognizer asr = SpeechRecognizer.builder()
				.serverURL(pa.getArg("server"))
				.credentials(pa.getArg("user"), pa.getArg("pwd"))
				.addListener(new SimpleRecognizerListener() {
					@Override
					public void onSpeechStart(Integer time) {
						System.out.println("-- speech started --");
					}
					
					@Override
					public void onSpeechStop(Integer time) {
						System.out.println("-- speech stopped --");
					}
				})
				.recogConfig(config).build();

		try {
			BufferedReader keyboardReader = new BufferedReader(new InputStreamReader(System.in));
			
			System.out.println("Press <ENTER> to start recording...");
			keyboardReader.readLine();
			asr.recognize(audio, lm);
			System.err.println("Recogniztion started");

			RecognitionResult result = asr.waitRecognitionResult().get(0);
			
			System.out.println("ResultCode = " + result.getResultCode());
			
			result.getAlternatives().stream().findFirst().ifPresent(a -> {
				System.out.println("Text: " + a.getText());
				System.out.println("Score: " + a.getConfidence());
				a.getInterpretations().stream().findFirst().ifPresent(i -> {
					System.out.println("Interpretation: " + i.getInterpretation());
				});
			});
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			asr.close();
		}
	}

}
