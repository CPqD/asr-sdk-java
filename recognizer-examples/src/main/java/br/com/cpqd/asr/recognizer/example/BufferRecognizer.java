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

import java.io.FileInputStream;

import br.com.cpqd.asr.recognizer.BufferAudioSource;
import br.com.cpqd.asr.recognizer.LanguageModelList;
import br.com.cpqd.asr.recognizer.SpeechRecognizer;
import br.com.cpqd.asr.recognizer.model.RecognitionConfig;
import br.com.cpqd.asr.recognizer.model.RecognitionResult;

/**
 * Exemplo de uso do cliente Java SE do servidor de reconhecimento de fala,
 * utilizando uma entrada de áudio do tipo BufferAudioSource. Essa classe
 * permite que uma aplicação escreva sequências de bytes sem bloquear a thread
 * principal. Os bytes escritos serão enviados para o serviço de reconhecimento
 * através de uma thread secundária.
 * 
 */
public class BufferRecognizer {

	public static void main(String[] args) throws Exception {

		ProgramArguments pa = ProgramArguments.from(args);

		if (args.length == 0) {
			System.err.println("Usage: BufferRecognizer --server <Server URL> --lm <LM URI> --audio <Audio Path> [--user <username> --pwd <password>]");
			System.err.println(" e.g.: BufferRecognizer --server ws://127.0.0.1:8025/asr-server/asr --lm builtin:slm/general --audio audio/pt-br/87431_8k.wav");
			return;
		}
		
		RecognitionConfig config = RecognitionConfig.builder().maxSentences(1).confidenceThreshold(70).build();
		LanguageModelList lm = LanguageModelList.builder().addFromURI(pa.getArg("lm")).build();
		SpeechRecognizer asr = SpeechRecognizer.builder()
				.serverURL(pa.getArg("server"))
				.credentials(pa.getArg("user"), pa.getArg("pwd"))
				.recogConfig(config).build();

		// cria um objeto BufferAudioSource para receber os bytes de audio
		BufferAudioSource audio = new BufferAudioSource();
		
		// Usando outra thead para enviar o áudio para o ASR
		ReadAudioThread thread = new ReadAudioThread(audio, pa.getArg("audio"));
		thread.start();
		
		try {
			asr.recognize(audio, lm);
			RecognitionResult result = asr.waitRecognitionResult().get(0);
			result.getAlternatives().stream().findFirst().ifPresent(a -> {
				System.out.println("Text: " + a.getText());
				System.out.println("Score: " + a.getConfidence());
			});
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			asr.close();
		}
	}
	
	protected static class ReadAudioThread extends Thread {
		
		private String audioFile;
		private BufferAudioSource audioSource;

		public ReadAudioThread(BufferAudioSource audioSource, String audioFile) {
			this.audioFile = audioFile;
			this.audioSource = audioSource;
		}
		
		@Override
		public void run() {
			System.out.println("Sending audio to ASR...");
			// utiliza um arquivo como fonte de bytes de áudio
			try(FileInputStream input = new FileInputStream(audioFile)) {
				// faz a leitura do arquivo e escreve no AudioSource
				byte[] buffer = new byte[1600]; // segmento de 100 ms (tx 8kHz)
				int len;
				boolean keepWriting = true;
				while ((len = input.read(buffer)) != -1 && keepWriting) {
					keepWriting = audioSource.write(buffer, len);
					// Atraso para simular uma captura de audio em tempo real.
					Thread.sleep(100);
				}
				audioSource.finish();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Audio finished!");
		}
	}

}
