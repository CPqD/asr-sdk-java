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
package br.com.cpqd.asr.recognizer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import br.com.cpqd.asr.recognizer.model.RecognitionConfig;
import br.com.cpqd.asr.recognizer.model.RecognitionResult;

/**
 * The SpeechRecognizer allows a client application to submit an audio input
 * source to the CPqD Speech Recognition Server.
 *
 */
public interface SpeechRecognizer {

	/**
	 * Release resources and close the server connection.
	 * 
	 * @throws IOException
	 *             some sort of I/O exception has ocurred.
	 */
	void close() throws IOException;

	/**
	 * Cancels the current recognition, closing the audio source.
	 * 
	 * @throws IOException
	 *             some sort of I/O exception has ocurred.
	 * @throws RecognitionException
	 *             in case the operation fails.
	 */
	void cancelRecognition() throws IOException, RecognitionException;

	/**
	 * Recognizes an audio source. The recognition session with the server must
	 * be created previously. The recognition result will be notified in the
	 * registered AsrListener callbacks. The audio source is automatically
	 * closed after the end of the recognition process.
	 * 
	 * @param lmList
	 *            the language model to use.
	 * @param audio
	 *            audio source.
	 * 
	 * @throws IOException
	 *             some sort of I/O exception has ocurred.
	 * @throws RecognitionException
	 *             in case the operation fails.
	 */
	void recognize(AudioSource audio, LanguageModelList lmList) throws IOException, RecognitionException;

	/**
	 * Recognizes an audio source. The recognition session with the server must
	 * be created previously. The recognition result will be notified in the
	 * registered AsrListener callbacks. The audio source is automatically
	 * closed after the end of the recognition process.
	 * 
	 * @param lmList
	 *            the language model to use.
	 * @param audio
	 *            audio source.
	 * @param config
	 *            recognition configuration parameters.
	 * 
	 * @throws IOException
	 *             some sort of I/O exception has ocurred.
	 * @throws RecognitionException
	 *             in case the operation fails.
	 */
	void recognize(AudioSource audio, LanguageModelList lmList, RecognitionConfig config)
			throws IOException, RecognitionException;

	/**
	 * Returns the recognition result. If audio packets are still being sent to
	 * the server, the method blocks and waits for the end of the recognition
	 * process.
	 * 
	 * @return the recognition result or null if there is no result available.
	 * @throws RecognitionException
	 *             in case an error in the recognition occurs.
	 */
	List<RecognitionResult> waitRecognitionResult() throws RecognitionException;

	/**
	 * Returns the recognition result. If audio packets are still being sent to the
	 * server, the method blocks and waits for the end of the recognition process.
	 * 
	 * @param timeout
	 *            the max wait time for a recognition result (in seconds). The timer
	 *            is started after the last audio packet is sent.
	 * @return the recognition result or null if there is no result available.
	 * @throws RecognitionException
	 *             in case an error in the recognition occurs.
	 */
	List<RecognitionResult> waitRecognitionResult(int timeout) throws RecognitionException;

	/**
	 * Creates a new instance of the object builder.
	 * 
	 * @return the Builder object.
	 */
	static SpeechRecognizer.Builder builder() {
		return new SpeechRecognizer.Builder();
	}

	/**
	 * The Builder object for the SpeechReconizer interface.
	 *
	 */
	public class Builder {

		/** The ASR Server URL. */
		protected URI uri;

		/** The User Agent data. */
		protected String userAgent;

		/** User access credentials. */
		protected String username;
		protected String password;

		/** The recognition configuration parameters. */
		protected RecognitionConfig recogConfig;

		/** Registered listener interfaces. */
		protected List<RecognitionListener> listeners = new ArrayList<>();

		/** the audio encoding. */
		protected AudioEncoding encoding;

		/** the audio sample rate. */
		protected Integer audioSampleRate;

		/** the audio language. */
		protected LanguageCode language;

		/** the audio packets length. */
		protected Integer chunkLength;

		/** the server Real Time Factor. */
		protected Float serverRTF;

		/** the maximum time to wait for a recognition result. */
		protected Integer maxWaitSeconds;

		/**
		 * If set to true, the ASR session is created at each recognition.
		 * Otherwise, it is created when the SpeechRecognizer instance is built.
		 */
		protected boolean connectOnRecognize;

		/**
		 * If set to true, the ASR session is automatically closed at the end of
		 * each recognition. Otherwise, it is kept open, available for further
		 * recognitions.
		 */
		protected boolean autoClose;

		/** The maximum time the ASR session is kept open and idle. */
		protected int maxSessionIdleSeconds;

		/**
		 * Private constructor. Defines default configuration parameters.
		 * 
		 */
		protected Builder() {
			this.audioSampleRate = 8000;
			this.encoding = AudioEncoding.LINEAR16;
			this.chunkLength = 250;
			this.serverRTF = 0.1F;
			this.maxWaitSeconds = 30;
			this.maxSessionIdleSeconds = 30;
		}

		/**
		 * Builds an SpeechRecognizer instance.
		 * 
		 * @return the recognizer instance.
		 * @throws URISyntaxException
		 *             if there is an error with the server URL parameter.
		 * @throws IOException
		 *             some sort of I/O exception has ocurred.
		 * @throws RecognitionException
		 *             error when creating the session.
		 */
		public SpeechRecognizer build() throws URISyntaxException, IOException, RecognitionException {
			SpeechRecognizerImpl recognizer = new SpeechRecognizerImpl(this);
			return recognizer;
		}

		/**
		 * Defines the Server URL.
		 * 
		 * @param url
		 *            the ASR Server endpoint URL (e.g.:
		 *            ws://192.168.100.1:8025/asr-server).
		 * @return the Builder object
		 * @throws URISyntaxException 
		 */
		public SpeechRecognizer.Builder serverURL(String url) throws URISyntaxException {
			this.uri = new URI(url);
			return this;
		}

		/**
		 * Sets user access credentials, if required by the server.
		 * 
		 * @param username
		 *            user id.
		 * @param password
		 *            password.
		 * @return the Builder object.
		 */
		public SpeechRecognizer.Builder credentials(String username, String password) {
			if (username != null && password != null) {
				this.username = username;
				this.password = password;
			}
			return this;
		}

		/**
		 * Configure the recognition parameters.
		 * 
		 * @param recogConfig
		 *            the configuration parameters.
		 * @return the Builder object.
		 */
		public SpeechRecognizer.Builder recogConfig(RecognitionConfig recogConfig) {
			this.recogConfig = recogConfig;
			return this;
		}

		/**
		 * Register a call back listener interface.
		 * 
		 * @param listener
		 *            the listener object.
		 * @return the Builder object.
		 */
		public SpeechRecognizer.Builder addListener(RecognitionListener listener) {
			this.listeners.add(listener);
			return this;
		}

		/**
		 * Sets the user agent data. This information indicates the
		 * characteristics of the client for logging and debug purposes.
		 * 
		 * @param userAgent
		 *            the user agent data.
		 * @return the Builder object.
		 */
		public SpeechRecognizer.Builder userAgent(String userAgent) {
			this.userAgent = userAgent;
			return this;
		}

		/**
		 * Sets the maximum time the client waits for the recognition result.
		 * 
		 * @param timeout
		 *            the timeout value (in seconds).
		 * @return the Builder object.
		 */
		public SpeechRecognizer.Builder maxWaitSeconds(int timeout) {
			this.maxWaitSeconds = timeout;
			return this;
		}

		/**
		 * Sets the connect on recognize property. If set to true, the ASR
		 * session is automatically created at each recognition. Otherwise, it
		 * is created when the SpeechRecognizer is built.
		 * 
		 * @param connectOnRecognize
		 *            the connectOnRecognize property value.
		 * @return the Builder object.
		 */
		public SpeechRecognizer.Builder connectOnRecognize(boolean connectOnRecognize) {
			this.connectOnRecognize = connectOnRecognize;
			return this;
		}

		/**
		 * Sets the auto close property. If set to true, the ASR session is
		 * automatically closed at the end of each recognition. Otherwise, it is
		 * kept open for the next recognition.
		 * 
		 * @param autoClose
		 *            the autoClose property value.
		 * @return the Builder object.
		 */
		public SpeechRecognizer.Builder autoClose(boolean autoClose) {
			this.autoClose = autoClose;
			return this;
		}

		/**
		 * Sets the maximum session idle time.
		 * 
		 * @param maxSessionIdleSeconds
		 *            the max session idle time in seconds.
		 * @return the Builder object.
		 */
		public SpeechRecognizer.Builder maxSessionIdleSeconds(int maxSessionIdleSeconds) {
			this.maxSessionIdleSeconds = maxSessionIdleSeconds;
			return this;
		}

		/**
		 * Sets the audio sample rate (in bps).
		 * 
		 * @param sampleRate
		 *            the audio sample rate.
		 * @return the Builder object.
		 */
		@SuppressWarnings("unused")
		private SpeechRecognizer.Builder audioSampleRate(int sampleRate) {
			this.audioSampleRate = sampleRate;
			return this;
		}

		/**
		 * Sets the audio encoding.
		 * 
		 * @param encoding
		 *            the audio encoding.
		 * @return the Builder object.
		 */
		@SuppressWarnings("unused")
		private SpeechRecognizer.Builder audioEncoding(AudioEncoding encoding) {
			this.encoding = encoding;
			return this;
		}

		/**
		 * Sets the audio language.
		 * 
		 * @param language
		 *            the audio language.
		 * @return the Builder object.
		 */
		@SuppressWarnings("unused")
		private SpeechRecognizer.Builder language(LanguageCode language) {
			this.language = language;
			return this;
		}
	}

}
