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
package br.com.cpqd.asr.recognizer;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.cpqd.asr.recognizer.model.PartialRecognitionResult;
import br.com.cpqd.asr.recognizer.model.RecognitionConfig;
import br.com.cpqd.asr.recognizer.model.RecognitionError;
import br.com.cpqd.asr.recognizer.model.RecognitionResult;

public class RecognizerBuilderTest {

	private static final String url = "wss://speech.cpqd.com.br/asr/ws/estevan/recognize/8k"; // "wss://speech.cpqd.com.br/asr/ws/v2/recognize/8k";
	private static final String user = "estevan";
	private static final String passwd = "Thect195";
	private static final String filename = "./src/test/resources/audio/pizza-veg-8k.wav";
	private static final String lmName = "builtin:slm/general";

	@Test
	public void urlNull() {
		try {
			SpeechRecognizer.builder().build();
			fail("URISyntaxException expected");
		} catch (URISyntaxException e) {
			assertTrue(e.getMessage(), true);
		} catch (NullPointerException e) {
			assertTrue(e.getMessage(), true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("NullPointerException expected, instead of " + e.getCause());
		}
	}

	@Test
	public void urlInvalid() {
		try {
			String url = "abcdasr";
			SpeechRecognizer.builder().serverURL(url).build();
			fail("URISyntaxException expected");
		} catch (URISyntaxException e) {
			assertTrue(e.getMessage(), true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("URISyntaxException expected, instead of " + e.getCause());
		}
	}

	@Test
	public void credentialValid() {
		try {
			SpeechRecognizer recognizer = SpeechRecognizer.builder().serverURL(url).credentials(user, passwd).build();
			recognizer.close();
			assertTrue("Credentials accepted", true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Invalid credentials: " + e.getMessage());
		}
	}

	@Test
	public void credentialNotValid() {
		String user = "unkownuser";
		String passwd = "anypasswd";

		try {
			SpeechRecognizer recognizer = SpeechRecognizer.builder().serverURL(url).credentials(user, passwd).build();
			recognizer.close();
			fail("Connection failure expected");
		} catch (IOException e) {
			assertTrue("Credentials rejected", true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Invalid credentials: " + e.getMessage());
		}
	}

	@Test
	public void credentialNull() {
		String user = null;
		String passwd = null;

		try {
			SpeechRecognizer recognizer = SpeechRecognizer.builder().serverURL(url).credentials(user, passwd).build();
			recognizer.close();
			fail("Connection failure expected");
		} catch (NullPointerException e) {
			// o erro NPE é o desejado mas o que ocorre é erro de conexão
			assertTrue("NullPointerException", true);
		} catch (IOException e) {
			assertTrue("DeploymentException expected", true);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void recogConfig() {
		int numberOfSentences = 3;

		try {
			SpeechRecognizer recognizer = SpeechRecognizer.builder().serverURL(url).credentials(user, passwd)
					.recogConfig(RecognitionConfig.builder().maxSentences(numberOfSentences).build()).build();
			AudioSource audio = new FileAudioSource(new File(filename));
			recognizer.recognize(audio, LanguageModelList.builder().addFromURI(lmName).build());
			List<RecognitionResult> results = recognizer.waitRecognitionResult();
			assertTrue("Number of alternatives is " + numberOfSentences,
					results.get(0).getAlternatives().size() == numberOfSentences);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Test failed: " + e.getMessage());
		}
	}

	@Test
	public void multipleListeners() {

		int[] startCounter = new int[2];
		int[] stopCounter = new int[2];
		int[] listeningCounter = new int[2];
		int[] partialCounter = new int[2];
		int[] finalCounter = new int[2];
		int pos1 = 0;
		int pos2 = 1;

		try {
			SpeechRecognizer recognizer = SpeechRecognizer.builder().serverURL(url).credentials(user, passwd)
					.recogConfig(RecognitionConfig.builder().build()).addListener(new RecognitionListener() {

						@Override
						public void onSpeechStop(Integer time) {
							stopCounter[pos1]++;
						}

						@Override
						public void onSpeechStart(Integer time) {
							startCounter[pos1]++;
						}

						@Override
						public void onRecognitionResult(br.com.cpqd.asr.recognizer.model.RecognitionResult result) {
							finalCounter[pos1]++;
						}

						@Override
						public void onPartialRecognitionResult(PartialRecognitionResult result) {
							partialCounter[pos1]++;
						}

						@Override
						public void onListening() {
							listeningCounter[pos1]++;
						}

						@Override
						public void onError(RecognitionError error) {
						}
					}).addListener(new RecognitionListener() {

						@Override
						public void onSpeechStop(Integer time) {
							stopCounter[pos2]++;
						}

						@Override
						public void onSpeechStart(Integer time) {
							startCounter[pos2]++;
						}

						@Override
						public void onRecognitionResult(br.com.cpqd.asr.recognizer.model.RecognitionResult result) {
							finalCounter[pos2]++;
						}

						@Override
						public void onPartialRecognitionResult(PartialRecognitionResult result) {
							partialCounter[pos2]++;
						}

						@Override
						public void onListening() {
							listeningCounter[pos2]++;
						}

						@Override
						public void onError(RecognitionError error) {
						}
					}).build();

			AudioSource audio = new FileAudioSource(new File(filename));
			recognizer.recognize(audio, LanguageModelList.builder().addFromURI(lmName).build());
			recognizer.waitRecognitionResult();
			assertTrue("Compare start counter", startCounter[pos1] == startCounter[pos2]);
			assertTrue("Compare stop counter", stopCounter[pos1] == stopCounter[pos2]);
			assertTrue("Compare listen counter", listeningCounter[pos1] == listeningCounter[pos2]);
			assertTrue("Compare partial counter", partialCounter[pos1] == partialCounter[pos2]);
			assertTrue("Compare final counter", finalCounter[pos1] == finalCounter[pos2]);

		} catch (Exception e) {
			e.printStackTrace();
			fail("Test failed: " + e.getMessage());
		}
	}

	@Test
	public void maxWaitSeconds() {
		int numberOfSentences = 3;

		try {
			SpeechRecognizer recognizer = SpeechRecognizer.builder().serverURL(url).credentials(user, passwd)
					.recogConfig(RecognitionConfig.builder().maxSentences(numberOfSentences).build()).maxWaitSeconds(1)
					.build();
			AudioSource audio = new FileAudioSource(new File(filename));
			recognizer.recognize(audio, LanguageModelList.builder().addFromURI(lmName).build());
			recognizer.waitRecognitionResult();
			fail("Recognition timeout expected");
		} catch (RecognitionException e) {
			assertTrue("Recognition timeout", true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Test failed: " + e.getMessage());
		}
	}
}
