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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
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
	private boolean decodeAudio;


	public static void main(String[] args) throws IOException, URISyntaxException, RecognitionException {

		Properties pa = ProgramArguments.parseFrom(args);

		if (args.length == 0) {
			System.err.println("The BatchRecognizer sample can be used with the following arguments:");
			System.err.println(" --server <Server URL>. e.g.: ws://127.0.0.1:8025/asr-server/asr");
			System.err.println(" --lm <comma separated list of LM URIs and local filepath>. e.g.: @grammar/pt-br/digits.gram,builtin:grammar/alphacode,builtin:slm/general");
			System.err.println(" --audio <filepath or directory>. e.g.: audio/pt-br");
			System.err.println(" --<param> <value> (Parameters described at https://speechweb.cpqd.com.br/asr/docs/latest/config_asr)\n");
			System.err.println(" e.g.: BatchRecognizer --server ws://127.0.0.1:8025/asr-server/asr --lm builtin:slm/general --audio audio/pt-br/87431_8k.wav");
			System.err.println(" e.g.: BatchRecognizer --server ws://127.0.0.1:8025/asr-server/asr --lm @grammar/pt-br/digits.gram,builtin:grammar/alphacode,builtin:slm/general --audio audio/pt-br ");
			return;
		}

		BatchRecognizer recognizer = new BatchRecognizer(pa);
		recognizer.recognize(pa.getProperty("audio"), languageModel(pa.getProperty("lm")));
		recognizer.close();
	}

	public BatchRecognizer(Properties pa)
			throws URISyntaxException, IOException, RecognitionException {

		// Try to decode the audio file if possible
		this.decodeAudio = "true".equalsIgnoreCase(pa.getProperty("decodeAudio", "false"));

		String serverUrl = pa.getProperty("server");
		String user = pa.getProperty("user");
		String pwd = pa.getProperty("pwd");

		RecognitionConfig config = RecognitionConfig.builder()
			.maxSentences(1)
			.continuousMode("true".equals(pa.getProperty("decoder.continuousMode", "true")))
			.recognitionTimeoutEnabled(false)
			.noInputTimeoutEnabled(false)
			.endPointerLevelThreshold(Integer.parseInt(pa.getProperty("endpointer.levelThreshold", "2")))
			.confidenceThreshold(Integer.parseInt(pa.getProperty("decoder.confidenceThreshold", "30")))
			.waitEndMilis(Integer.parseInt(pa.getProperty("endpointer.waitEnd", "2000")))
			.loggingTag("BatchRecognizer")
			.wordHints(hints(pa.getProperty("hints.words")))
			.build();

		recognizer = SpeechRecognizer.builder().serverURL(serverUrl).userAgent("client=JavaSE;app=BatchRecognizer")
				.credentials(user, pwd).recogConfig(config)
				.addListener(new SimpleRecognizerListener() {
					@Override
					public void onRecognitionResult(RecognitionResult result) {
						result.getAlternatives().stream().findFirst().ifPresent(a -> {
							System.out.printf("  [%.2f - %.2f][%d] %s%n", result.getSegmentStartTime(), result.getSegmentEndTime(), a.getConfidence(), a.getText());
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
			AudioSource audio = null;
			if (audioFile.getAbsolutePath().endsWith(".raw")) {
				// RAW files are not converted
				audio = new FileAudioSource(audioFile, false, AudioSource.AUDIO_TYPE_RAW);
			} else {
				String contentType = decodeAudio ? AudioSource.AUDIO_TYPE_RAW : AudioSource.AUDIO_TYPE_DETECT;
				audio = new FileAudioSource(audioFile, decodeAudio, contentType);
			}
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
	public void recognize(String audioPath, LanguageModelList lmList) throws IOException, RecognitionException {

		File audioSource = new File(audioPath);
		if (audioSource.exists()) {
			if (audioSource.isDirectory()) {
				try (Stream<Path> paths = Files.walk(Paths.get(audioPath))) {
					paths.filter(Files::isRegularFile).filter(path -> !path.toString().endsWith(".txt"))
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

	private static String hints(String hints) throws IOException {
		// word hints
		if (hints != null && hints.startsWith("@"))
			return Files.readString(Paths.get(hints.substring(1)), StandardCharsets.UTF_8);
		return hints;
	}

	private static LanguageModelList languageModel(String lm) throws IOException {
		LanguageModelList.Builder lmBuilder = LanguageModelList.builder();
		for (String lmPath : lm.split(",")) {
			if (lmPath.startsWith("@")) {
				// gramatica inline (ler conteúdo do arquivo indicado no path)
				String grammarBody = new String(Files.readAllBytes(Paths.get(lmPath.substring(1))));
				String grammarName = lmPath.substring(lmPath.lastIndexOf("/")).replace(".", "_").replace("/", "_");
				System.out.println("[" + lmPath + "] Grammar name: " + grammarName);
				lmBuilder.addInlineGrammar(grammarName, grammarBody);
			} else {
				System.out.println("[" + lmPath + "] Grammar URI");
				lmBuilder.addFromURI(lmPath);
			}
		}
		return lmBuilder.build();
	}
}
