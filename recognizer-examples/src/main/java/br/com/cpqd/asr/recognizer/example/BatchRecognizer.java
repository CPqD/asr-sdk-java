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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import br.com.cpqd.asr.recognizer.AudioSource;
import br.com.cpqd.asr.recognizer.FileAudioSource;
import br.com.cpqd.asr.recognizer.LanguageModelList;
import br.com.cpqd.asr.recognizer.RecognitionException;
import br.com.cpqd.asr.recognizer.SimpleRecognizerListener;
import br.com.cpqd.asr.recognizer.SpeechRecognizer;
import br.com.cpqd.asr.recognizer.model.RecognitionAlternative;
import br.com.cpqd.asr.recognizer.model.RecognitionConfig;
import br.com.cpqd.asr.recognizer.model.RecognitionResult;

/**
 * Example of speech recognizer for a batch of audio files.
 */
public class BatchRecognizer {

	private SpeechRecognizer recognizer;

	public BatchRecognizer(String serverUrl, String user, String pwd)
			throws URISyntaxException, IOException, RecognitionException {

		RecognitionConfig config = RecognitionConfig.builder()
			.maxSentences(1)
			.continuousMode(true)
			.recognitionTimeoutEnabled(false)
			.noInputTimeoutEnabled(false)
			.build();

		recognizer = SpeechRecognizer.builder().serverURL(serverUrl).userAgent("client=JavaSE;app=BatchRecognizer")
				.credentials(user, pwd).recogConfig(config)
				.addListener(new SimpleRecognizerListener() {
					@Override
					public void onRecognitionResult(RecognitionResult result) {
						result.getAlternatives().stream().findFirst().ifPresent(a -> {
							System.out.printf("  [%.2f - %.2f] %s%n", result.getSegmentStartTime(), result.getSegmentEndTime(), a.getText());
						});
					}
				}).build();
	}

	/**
	 * Recognize a single audio file and generates a text file containing its transcription. 
	 *
	 * @param audioFile Audio file to recognize
	 * @param lmList Language model list
	 */
	private void recognizeFile(File audioFile, LanguageModelList lmList) {
		try (BufferedWriter textFile = Files.newBufferedWriter(Paths.get(audioFile.getAbsolutePath() + ".txt"))) {
			System.out.println("##### Processing audio: " + audioFile);
			System.out.println();
			// pode ser de qualquer fonte (neste caso é um arquivo)
			AudioSource audio = new FileAudioSource(audioFile);
			// envia o audio para o servidor
			recognizer.recognize(audio, lmList);
			// aguarda o resultado do reconhecimento
			List<RecognitionResult> resultList = recognizer.waitRecognitionResult();
			for (RecognitionResult result : resultList) {
				List<RecognitionAlternative> alts = result.getAlternatives();
				if (!alts.isEmpty()) {
					textFile.write(alts.get(0).getText() + " ");
				}
			}
			System.out.println("\n");
		} catch (Exception e) {
			System.err.println("ERROR: Failure to process audio: " + audioFile + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Transcribe a list of audio files in a directory
	 *
	 * @param audioPath Path to files.
	 * @param lmURI Language model to use.
	 *
	 * @throws IOException
	 * @throws RecognitionException
	 */
	public void recognize(String audioPath, String lmURI) throws IOException, RecognitionException {

		// modelo da língua
		LanguageModelList lmList = LanguageModelList.builder().addFromURI(lmURI).build();

		File audioSource = new File(audioPath);
		if (audioSource.exists()) {
			if (audioSource.isDirectory()) {
				try (Stream<Path> paths = Files.walk(Paths.get(audioPath))) {
					paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".wav"))
							.map(Path::toFile).forEach(file -> {
								recognizeFile(file, lmList);
							});
				}
			} else {
				recognizeFile(audioSource, lmList);
			}
		} else {
			throw new IOException("Audio path not found: " + audioPath);
		}
	}

	public void close() {
		try {
			recognizer.close();
		} catch (IOException e) {
			System.err.println("ERROR: Failure to close recognizer: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException, URISyntaxException, RecognitionException {

		ProgramArguments pa = ProgramArguments.from(args);

		if (args.length == 0) {
			System.err.println("Usage: BatchRecognizer --server <Server URL> --lm <LM URI> --audio <Audio Path> [--user <username> --pwd <password>]");
			System.err.println(" e.g.: BatchRecognizer --server ws://127.0.0.1:8025/asr-server/asr --lm builtin:slm/general --audio audio/pt-br/87431_8k.wav");
			return;
		}
		
		BatchRecognizer recognizer = new BatchRecognizer(pa.getArg("server"), pa.getArg("user"), pa.getArg("pwd"));
		recognizer.recognize(pa.getArg("audio"), pa.getArg("lm"));
		recognizer.close();
	}

}
