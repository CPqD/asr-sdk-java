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
import java.util.Properties;

import br.com.cpqd.asr.recognizer.AudioSource;
import br.com.cpqd.asr.recognizer.FileAudioSource;
import br.com.cpqd.asr.recognizer.LanguageModelList;
import br.com.cpqd.asr.recognizer.SpeechRecognizer;
import br.com.cpqd.asr.recognizer.model.RecognitionConfig;
import br.com.cpqd.asr.recognizer.model.RecognitionResult;

/**
 * Very simple example of using SpeechRecognizer.
 */
public class FileRecognizer {
	
	public static void main(String[] args) throws Exception {

		Properties pa = ProgramArguments.parseFrom(args);

		if (args.length == 0) {
			System.err.println("Usage: FileRecognizer --server <Server URL> --lm <LM URI> --audio <Audio Path> [--user <username> --pwd <password>]");
			System.err.println(" e.g.: FileRecognizer --server ws://127.0.0.1:8025/asr-server/asr --lm builtin:slm/general --audio audio/pt-br/87431_8k.wav");
			return;
		}
		
		RecognitionConfig config = RecognitionConfig.builder().maxSentences(1).confidenceThreshold(70).build();
		AudioSource audio = new FileAudioSource(new File(pa.getProperty("audio")));
		LanguageModelList lm = LanguageModelList.builder().addFromURI(pa.getProperty("lm")).build();
		SpeechRecognizer asr = SpeechRecognizer.builder().serverURL(pa.getProperty("server")).credentials(pa.getProperty("user"), pa.getProperty("pwd")).recogConfig(config).build();

		try {
			asr.recognize(audio, lm);
			RecognitionResult result = asr.waitRecognitionResult().get(0);
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
