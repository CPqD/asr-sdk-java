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
import java.util.Properties;

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
 * pressionada. A captura é finalizada ao pressionar novamente o <ENTER>.
 *
 */
public class MicContinuousRecognizer {

	public static void main(String[] args) throws DeploymentException, IOException, URISyntaxException,
			RecognitionException, LineUnavailableException {
		
		Properties pa = ProgramArguments.parseFrom(args);

		if (args.length == 0) {
			System.err.println("Usage: MicContinuousRecognizer --server <Server URL> --lm <LM URI> [--user <username> --pwd <password>]");
			System.err.println(" e.g.: MicContinuousRecognizer --server ws://127.0.0.1:8025/asr-server/asr --lm builtin:slm/general");
			return;
		}
		
		RecognitionConfig config = RecognitionConfig.builder()
				.maxSentences(1)
				.continuousMode(true)
				.recognitionTimeoutEnabled(false)
				.noInputTimeoutEnabled(false)
				.endPointerLevelThreshold(Integer.parseInt(pa.getProperty("endpointer.levelThreshold", "10")))
				.confidenceThreshold(Integer.parseInt(pa.getProperty("decoder.confidenceThreshold", "70")))
				.waitEndMilis(Integer.parseInt(pa.getProperty("endpointer.waitEnd", "1000")))
				.build();

		MicAudioSource audio = new MicAudioSource(new AudioFormat(8000F, 16, 1, true, false));
		LanguageModelList lm = LanguageModelList.builder().addFromURI(pa.getProperty("lm")).build();
		
		SpeechRecognizer asr = SpeechRecognizer.builder()
				.serverURL(pa.getProperty("server"))
				.credentials(pa.getProperty("user"), pa.getProperty("pwd"))
				.recogConfig(config)
				.addListener(new SimpleRecognizerListener() {
					@Override
					public void onRecognitionResult(RecognitionResult result) {
						result.getAlternatives().stream().findFirst().ifPresent(a -> {
							System.out.printf("  [%.2f - %.2f][%d] %s%n", result.getSegmentStartTime(), result.getSegmentEndTime(), a.getConfidence(), a.getText());
							a.getInterpretations().stream().findFirst().ifPresent(si -> {
								System.out.printf("  >> Interpretation: %s%n", si.getInterpretation());
							});
							System.out.println();
						});
					}
				}).build();

		BufferedReader keyboardReader = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			System.out.println("Press <ENTER> to start, and again to stop recording...");
			keyboardReader.readLine();
			
			asr.recognize(audio, lm);
			
			System.err.println("Recognition started");
			keyboardReader.readLine();
			audio.finish();
			System.err.println("Recognition stopped");
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			asr.close();
		}
	}
}
