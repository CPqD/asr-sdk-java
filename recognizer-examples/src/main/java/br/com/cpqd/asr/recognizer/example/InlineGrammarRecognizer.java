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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import javax.websocket.DeploymentException;

import br.com.cpqd.asr.recognizer.AudioSource;
import br.com.cpqd.asr.recognizer.FileAudioSource;
import br.com.cpqd.asr.recognizer.LanguageModelList;
import br.com.cpqd.asr.recognizer.RecognitionException;
import br.com.cpqd.asr.recognizer.SpeechRecognizer;
import br.com.cpqd.asr.recognizer.model.RecognitionConfig;
import br.com.cpqd.asr.recognizer.model.RecognitionResult;

/**
 * Exemplo de uso do cliente Java SE do servidor de reconhecimento de fala,
 * utilizando a função recognize() e uma gramática externa enviada no momento do
 * reconhecimento.
 * 
 */
public class InlineGrammarRecognizer {

	public static void main(String[] args) throws DeploymentException, IOException, URISyntaxException, RecognitionException {

		if (args.length == 0) {
			System.err.println("Usage: InlineGrammarRecognizer --server <Server URL> --lm <Local Grammar Path> --audio <Audio Path> [--user <username> --pwd <password>]");
			System.err.println(" e.g.: InlineGrammarRecognizer --server ws://127.0.0.1:8025/asr-server/asr --lm grammar/pt-br/digits.gram --audio audio/pt-br/87431_8k.wav");
			return;
		}

		Properties pa = ProgramArguments.parseFrom(args);
		
		RecognitionConfig config = RecognitionConfig.builder().maxSentences(1).confidenceThreshold(70).build();
		System.out.println(pa.getProperty("audio"));
		AudioSource audio = new FileAudioSource(new File(pa.getProperty("audio")));
		String grammarBody = new String(Files.readAllBytes(Paths.get(pa.getProperty("lm"))));
		LanguageModelList lm = LanguageModelList.builder().addInlineGrammar("mygram", grammarBody).build();
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
