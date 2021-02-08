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

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.cpqd.asr.protocol.CancelRecognition;
import br.com.cpqd.asr.protocol.CreateSession;
import br.com.cpqd.asr.protocol.DefineGrammarMessage;
import br.com.cpqd.asr.protocol.LanguageModel;
import br.com.cpqd.asr.protocol.ReleaseSession;
import br.com.cpqd.asr.protocol.ResponseMessage;
import br.com.cpqd.asr.protocol.ResponseMessage.Result;
import br.com.cpqd.asr.protocol.SendAudio;
import br.com.cpqd.asr.protocol.SessionStatus;
import br.com.cpqd.asr.protocol.SetParametersMessage;
import br.com.cpqd.asr.protocol.StartRecognition;
import br.com.cpqd.asr.recognizer.model.PartialRecognitionResult;
import br.com.cpqd.asr.recognizer.model.RecognitionConfig;
import br.com.cpqd.asr.recognizer.model.RecognitionError;
import br.com.cpqd.asr.recognizer.model.RecognitionErrorCode;
import br.com.cpqd.asr.recognizer.model.RecognitionResult;
import br.com.cpqd.asr.recognizer.ws.AsrClientEndpoint;

/**
 * The SpeechRecognizer implementation.
 *
 */
public class SpeechRecognizerImpl implements SpeechRecognizer, RecognitionListener {

	private static Logger logger = LoggerFactory.getLogger(SpeechRecognizerImpl.class.getName());

	/** The session handle. */
	private Long handle;

	/** The Builder object. */
	private SpeechRecognizer.Builder builder;

	/** the websocket client. */
	private AsrClientEndpoint client;

	/** blocking queue to read recognition result. */
	private BlockingQueue<RecognitionResult> sentencesQueue = new LinkedBlockingQueue<RecognitionResult>();

	/** Temporarily stores a recognition error. */
	private RecognitionError error;

	/** the audiosource object. */
	private AudioSource audio;

	/** the asynchronous reader task. */
	private ReaderTask readerTask;

	//** Momento de recebimento do último resultado */
	private Instant lastResultTime;

	/** Status definition of the reader task. */
	private enum ReaderTaskStatus {
		IDLE, RUNNING, FINISHED, CANCELED
	};

	/**
	 * Constructor.
	 *
	 * @param builder
	 *            the Builder object.
	 * @throws URISyntaxException
	 *             if there is an error with the server URL parameter.
	 * @throws IOException
	 *             some sort of I/O exception has ocurred.
	 * @throws RecognitionException
	 *             error when creating the session.
	 *
	 */
	public SpeechRecognizerImpl(SpeechRecognizer.Builder builder)
			throws URISyntaxException, IOException, RecognitionException {
		this.builder = builder;

		client = new AsrClientEndpoint(builder.uri, builder.username, builder.password);
		client.getListeners().add(this);
		if (builder.listeners.size() > 0)
			client.getListeners().addAll(builder.listeners);
		client.setSessionTimeoutTime(builder.maxSessionIdleSeconds >= 0 ? builder.maxSessionIdleSeconds * 1000 : -1);

		try {
			if (!builder.connectOnRecognize) {
				open();
			}
		} catch (DeploymentException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void cancelRecognition() throws IOException, RecognitionException {
		logger.debug("[{}] Cancel called... Reader task is {}. Client is {}.", handle, getReaderTaskStatus(),
				client.isOpen() ? "opened" : "closed");

		if (!client.isOpen()) {
			return;
		}

		if (readerTask != null && readerTask.isRunning()) {
			// cancela a reader task.
			readerTask.cancel();
			logger.debug("[{}] Reader task cancelled.", handle);
		}

		if (client.getStatus() == SessionStatus.IDLE) {
			return;
		}

		CancelRecognition message = new CancelRecognition();
		message.setHandle(this.handle);
		try {
			ResponseMessage response = client.sendMessageAndWait(message);

			if (response == null) {
				logger.error("[{}] Timeout canceling recognition.", handle);
				throw new RecognitionException(RecognitionErrorCode.FAILURE, "Operation timeout");

			} else if (Result.SUCCESS.equals(response.getResult())) {
				logger.debug("[{}] Recognition canceled ({}).", handle, response.getSessionStatus());

				// cancelamento com sucesso. fecha a sessao
				if (builder.autoClose) {
					try {
						close();
					} catch (IOException e) {
						logger.error("[{}] Error closing session", handle, e);
					}
				}
			} else {
				logger.error("[{}] Error canceling recognition ({}): {}.", handle, response.getSessionStatus(),
						response.getErrorMessage());
				throw new RecognitionException(RecognitionErrorCode.FAILURE, response.getErrorMessage());
			}

		} catch (EncodeException e) {
			logger.error("[{}] Encode error.", this.handle, e);
			handle = null;
			throw new RecognitionException(RecognitionErrorCode.FAILURE, "Encode error", e);
		}
	}

	/**
	 * Opens a websocket channel and creates a new recognition session with the
	 * server.
	 *
	 * @throws DeploymentException
	 *             error when starting the endpoint.
	 * @throws IOException
	 *             some sort of I/O exception has ocurred.
	 * @throws RecognitionException
	 *             error when creating the session.
	 */
	private void open() throws DeploymentException, IOException, RecognitionException {
		logger.debug("[{}] Open called... Reader task is {}. Client is {}.", handle, getReaderTaskStatus(),
				client.isOpen() ? "opened" : "closed");

		if (!client.isOpen()) {
			client.open();

			CreateSession message = new CreateSession();
			message.setUserAgent(this.builder.userAgent);

			try {
				ResponseMessage response = client.sendMessageAndWait(message);

				if (response == null) {
					logger.error("Timeout creating session.");
					this.handle = null;
					throw new RecognitionException(RecognitionErrorCode.FAILURE, "Operation timeout");
				} else if (Result.SUCCESS.equals(response.getResult())
						&& SessionStatus.IDLE.equals(response.getSessionStatus())) {
					this.handle = response.getHandle();
					logger.trace("[{}] Session created ({}).", handle, response.getSessionStatus());

					if (builder.recogConfig != null) {
						setRecognitionParameters(builder.recogConfig);
					}

				} else {
					logger.error("Error creating session ({}): {}",
							(response != null ? response.getSessionStatus() : null),
							(response != null ? response.getErrorMessage() : null));
					handle = null;
					throw new RecognitionException(RecognitionErrorCode.FAILURE, response.getErrorMessage());
				}

			} catch (EncodeException e) {
				logger.error("Encode error", e);
				handle = null;
				throw new RecognitionException(RecognitionErrorCode.FAILURE, "Encode error", e);
			}
		}
	}

	@Override
	public void close() throws IOException {
		logger.debug("[{}] Close called... Reader task is {}. Client is {}.", handle, getReaderTaskStatus(),
				client.isOpen() ? "opened" : "closed");

		if (!client.isOpen()) {
			return;
		}

		// encerra thread de leitura se estiver sendo executada
		if (readerTask != null && readerTask.isRunning()) {
			readerTask.cancel();
			logger.trace("[{}] Reader task cancelled.", handle);
		}

		ReleaseSession message = new ReleaseSession();
		message.setHandle(this.handle);

		try {
			ResponseMessage response = client.sendMessageAndWait(message);
			if (response == null) {
				logger.warn("[{}] Timeout calling release session.", this.handle);
			} else if (Result.SUCCESS.equals(response.getResult())) {
				logger.debug("[{}] Session released.", handle);
			} else {
				logger.error("[{}] Error calling release session: {}", handle, response.getErrorMessage());
			}

		} catch (NullPointerException | EncodeException e) {
			logger.error("[{}] Error calling release session: {}", handle, e.getMessage());
		} finally {
			client.close();
		}
	}

	@Override
	public void recognize(AudioSource audio, LanguageModelList lm) throws IOException, RecognitionException {
		recognize(audio, lm, null);
	}

	@Override
	public synchronized void recognize(AudioSource audio, LanguageModelList lm, RecognitionConfig recogConfig)
			throws IOException, RecognitionException {
		logger.debug("[{}] Recognize called... Reader task is {}. Client is {}.", handle, getReaderTaskStatus(),
				client.isOpen() ? "opened" : "closed");

		if (readerTask != null && readerTask.isRunning()) {
			logger.warn("[{}] Another recognition is running [{}]", this.handle, readerTask.getThreadName());
			throw new RecognitionException(RecognitionErrorCode.FAILURE, "Another recognition is running");
		}

		if (!client.isOpen()) {
			try {
				open();
			} catch (DeploymentException e) {
				throw new IOException(e);
			}
		}

		// limpa eventual lixo na fila de resposta
		sentencesQueue.clear();
		this.error = null;
		this.audio = audio;

		// cria uma thread para ler o audio source e enviar os pacotes para o servidor
		readerTask = new ReaderTask(audio);

		startRecognition(lm, recogConfig);
	}

	@Override
	public synchronized List<RecognitionResult> waitRecognitionResult() throws RecognitionException {
		return waitRecognitionResult(builder.maxWaitSeconds);
	}

	@Override
	public synchronized List<RecognitionResult> waitRecognitionResult(int timeout) throws RecognitionException {
		logger.debug("[{}] Wait called... Reader task is {}. Client is {}. Status = {}", handle, getReaderTaskStatus(),
				client.isOpen() ? "opened" : "closed", client.getStatus());

		if (readerTask == null || (readerTask != null && readerTask.isCancelled())) {
			// chamou wait sem executar um reconhecimento
			return new ArrayList<>(0);
		}

		// se o audio está sendo enviado, bloqueia a thread aguardando o fim do processo
		while (!readerTask.hasFinished()) {
			try {
				synchronized (audio) {
					audio.wait(3000);
				}
			} catch (InterruptedException e) {
			}
		}

		if (readerTask.isCancelled()) {
			logger.debug("Audio transfer canceled");
			// se tarefa foi cancelada, devolve resultado vazio
			return new ArrayList<>(0);
		}

		// Espera todos os resultados chegarem
		boolean isTimeoutWaitingResult = false;
		while (client.isOpen() && client.getStatus() != SessionStatus.IDLE) {
			try {
				synchronized (sentencesQueue) {
					sentencesQueue.wait(2000);
				}
				// Verifica se o timeout por resultado foi atingido
				if (Duration.ofSeconds(timeout).minus(Duration.between(lastResultTime, Instant.now())).isNegative()) {
					isTimeoutWaitingResult = true;
					break;
				}
			} catch (InterruptedException e) {
			}
		}

		readerTask = null;

		if (isTimeoutWaitingResult) {
			logger.warn("[{}] Timeout waiting for recognition result.", this.handle);
			for (RecognitionListener listener : client.getListeners()) {
				listener.onError(new RecognitionError(RecognitionErrorCode.FAILURE, "Recognition timeout"));
			}
			throw new RecognitionException(RecognitionErrorCode.FAILURE, "Recognition timeout");
		} else if (error != null) {
			throw new RecognitionException(error);
		} else {
			return Arrays.asList(sentencesQueue.toArray(new RecognitionResult[sentencesQueue.size()]));
		}

	}

	@Override
	public void onListening() {
		readerTask.readerStatus = ReaderTaskStatus.RUNNING;
		new Thread(readerTask).start();
		logger.debug("[{}] Server is listening.", handle);
	}

	@Override
	public void onSpeechStart(Integer time) {
		logger.debug("[{}] Speech started.", handle);
	}

	@Override
	public void onSpeechStop(Integer time) {
		logger.debug("[{}] Speech stopped.", handle);
	}

	@Override
	public void onRecognitionResult(RecognitionResult result) {
		logger.debug("[{}] Recognition result (last={}): {}", this.handle, result.isLastSpeechSegment(),
				result);

		this.lastResultTime = Instant.now();

		if (!sentencesQueue.offer(result)) {
			logger.warn("[{}] Messsage discarded, sentences queue is full: {}", this.handle, result);
		}

		// recebeu resultado final do ultimo segmento. fecha a sessao
		if (result.isLastSpeechSegment()) {
			synchronized (sentencesQueue) {
				sentencesQueue.notifyAll();
			}

			// O servidor nao esta mais ouvindo. Encerra o envio de audio
			try {
				this.audio.finish();
			} catch (Exception e) {
				logger.error("[{}] Error calling finish audio.", this.handle, e);
			}

			if (builder.autoClose) {
				new Thread(() -> {
					// executa o fechamento da sessao e canal em outra thread para nao bloquear
					// a thread que ouve/recebe mensagens pelo websocket
					try {
						close();
					} catch (IOException e) {
						logger.error("[{}] Error closing session", handle, e);
					}
				}).start();
			}
		}
	}

	@Override
	public void onPartialRecognitionResult(PartialRecognitionResult result) {
		logger.debug("[{}] Partial recognition result: {}", this.handle, result);
	}

	@Override
	public void onError(RecognitionError error) {
		if (error.getCode() == RecognitionErrorCode.SESSION_TIMEOUT) {
			// ignora evento de timeout de sessao
			return;
		}

		logger.warn("[{}] Recognition error: {}", this.handle, error);

		if (readerTask != null && readerTask.isRunning()) {
			// o servidor nao esta mais ouvindo. Encerra o envio de audio
			try {
				this.audio.finish();
			} catch (Exception e) {
				logger.error("[{}] Error calling finish audio.", this.handle, e);
			}

			synchronized (sentencesQueue) {
				sentencesQueue.notifyAll();
			}
		}

		if (builder.autoClose) {
			// acabou o reconhecimento. fecha a sessao
			try {
				close();
			} catch (IOException e) {
				logger.error("[{}] Error closing session", handle, e);
			}
		}
	}

	/**
	 * Indicates if the server is listening for audio packets in the recognition
	 * process.
	 *
	 * @return true if the server is listening.
	 */
	private boolean isListening() {
		try {
			return client.getStatus() == SessionStatus.LISTENING;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Sets the recognition parameters which will be valid for the entire session.
	 *
	 * @param parameters
	 *            the recognition parameters.
	 * @throws IOException
	 *             in case an I/O error occurs.
	 * @throws RecognitionException
	 *             some error in the recogniton process.
	 */
	private void setRecognitionParameters(RecognitionConfig parameters) throws IOException, RecognitionException {
		if (parameters == null) {
			return;
		}

		SetParametersMessage message = new SetParametersMessage();
		message.setHandle(this.handle);
		message.setRecognitionParameters(parameters.getParameterMap());

		try {
			ResponseMessage response = client.sendMessageAndWait(message);
			if (response == null) {
				logger.error("[{}] Timeout configuring session parameters.", handle);
				throw new RecognitionException(RecognitionErrorCode.FAILURE, "Operation timeout");
			} else if (response.getResult().equals(Result.SUCCESS)) {
				logger.trace("[{}] Session configured.", handle);
			} else {
				logger.error("[{}] Error configuring session parameters: {} {}", handle, response.getSessionStatus(),
						response.getErrorMessage());
				throw new RecognitionException(RecognitionErrorCode.FAILURE, response.getErrorMessage());
			}

		} catch (EncodeException e) {
			logger.error("[{}] Encode error", this.handle, e);
			throw new RecognitionException(RecognitionErrorCode.FAILURE, "Encode error", e);
		}

	}

	private void defineGrammar(LanguageModel languageModel) throws IOException, RecognitionException {
		if (languageModel == null) {
			return;
		}

		DefineGrammarMessage message = new DefineGrammarMessage();
		message.setHandle(this.handle);
		message.setLanguageModel(languageModel);

		if (languageModel.getContentType() != null) {
			message.setContentType(languageModel.getContentType());
		} else if (languageModel.getDefinition() != null) {
			message.setContentType(DefineGrammarMessage.TEXT_PLAIN);
		} else {
			message.setContentType(DefineGrammarMessage.TEXT_URI_LIST);
		}

		try {
			ResponseMessage response = client.sendMessageAndWait(message);
			if (response == null) {
				logger.error("[{}] Timeout defining grammar.", this.handle);
				throw new RecognitionException(RecognitionErrorCode.FAILURE, "Operation timeout");
			} else if (Result.SUCCESS.equals(response.getResult())) {
				logger.debug("[{}] Grammar defined ({}).", this.handle, response.getSessionStatus());
			} else {
				logger.error("[{}] Error defining grammar ({}): {}", this.handle, response.getSessionStatus(),
						response.getResult());
				throw new RecognitionException(RecognitionErrorCode.FAILURE, response.getErrorMessage());
			}
		} catch (EncodeException e) {
			logger.error("[{}] Encode error", this.handle, e);
			throw new RecognitionException(RecognitionErrorCode.FAILURE, "Encode error", e);
		}
	}

	/**
	 * Sends a message to the server to start listening for audio.
	 *
	 * @param lmList
	 *            the language model list.
	 * @param parameters
	 *            the recognition parameters.
	 * @return true if the server is listening
	 * @throws IOException
	 *             in case an I/O error occurs.
	 * @throws RecognitionException
	 *             some error in the recogniton process.
	 */
	private boolean startRecognition(LanguageModelList lmList, RecognitionConfig parameters)
			throws IOException, RecognitionException {

		this.lastResultTime = Instant.now();
		StartRecognition message = new StartRecognition();
		message.setHandle(this.handle);

		List<String> uriList = Optional.ofNullable(lmList.getUriList()).orElse(new ArrayList<>());
		// define multiplas gramaticas
		if (Optional.ofNullable(lmList.getGrammarList()).isPresent()) {
			for (String[] grammar : lmList.getGrammarList()) {
				LanguageModel lm = new LanguageModel();
				lm.setId(grammar[0]);
				lm.setDefinition(grammar[1]);
				defineGrammar(lm);
				uriList.add("session:" + grammar[0]);
			}
		}

		LanguageModel languageModel = new LanguageModel(uriList.toArray(new String[uriList.size()]));
		message.setLanguageModel(languageModel);

		// define os parametros do reconhecimento
		if (parameters != null) {
			HashMap<String, String> map = parameters.getParameterMap();
			message.setRecognitionParameters(map);
		}

		try {
			ResponseMessage response = client.sendMessageAndWait(message);
			if (response == null) {
				logger.error("[{}] Timeout starting recognition.", handle);
				throw new RecognitionException(RecognitionErrorCode.FAILURE, "Operation timeout");
			} else if (SessionStatus.LISTENING.equals(response.getSessionStatus())) {
				logger.debug("[{}] Recognition started.", handle);
				return true;
			} else {
				logger.error("[{}] Error starting recognition: {}", handle, response.getErrorMessage());
				throw new RecognitionException(RecognitionErrorCode.FAILURE, response.getErrorMessage());
			}
		} catch (EncodeException e) {
			logger.error("[{}] Encode error", this.handle, e);
			throw new RecognitionException(RecognitionErrorCode.FAILURE, "Encode error", e);
		}
	}

	/**
	 * Sends an audio packet to the server.
	 *
	 * @param audio
	 *            audio buffer
	 * @param audioLength
	 *            the number of bytes of the audio buffer to send.
	 * @param lastPacket
	 *            true if this is the last audio packet.
	 * @throws IOException
	 *             in case an I/O error occurs.
	 */
	private void sendAudio(byte[] audio, int audioLength, String contentType, boolean lastPacket) throws IOException {
		if (!client.isOpen())
			return;

		SendAudio message = new SendAudio();
		message.setHandle(this.handle);
		message.setContent(audio);
		message.setContentLength(audioLength);
		message.setContentType(contentType);
		message.setLastPacket(lastPacket);

		// SEND AUDIO nao recebe confirmacao de recebimento (exceto em caso de erro)
		try {
			client.sendMessage(message);
		} catch (EncodeException e) {
			logger.error("[{}] Encode error", this.handle, e);
		}
	}

	/**
	 * Para fins de log.
	 *
	 * @return o estado de execução da thread.
	 */
	private String getReaderTaskStatus() {
		if (readerTask == null) {
			return "null";
		} else if (readerTask.isRunning()) {
			return "running";
		} else if (readerTask.isCancelled()) {
			return "cancelled";
		} else {
			return "stopped";
		}
	}

	/**
	 * Task that reads the audio source and send audio packets to the server, in a
	 * different thread from the client application.
	 *
	 */
	private class ReaderTask implements Runnable {

		private static final int AUDIO_CHUNK = 4000;

		/** Status of the reader task. */
		private ReaderTaskStatus readerStatus;

		private AudioSource audio;

		private String threadName;

		public ReaderTask(AudioSource audio) {
			super();
			this.audio = audio;
			this.readerStatus = ReaderTaskStatus.IDLE;
		}

		public String getThreadName() {
			return this.threadName;
		}

		public boolean isCancelled() {
			return readerStatus == ReaderTaskStatus.CANCELED;
		}

		public boolean hasFinished() {
			return readerStatus != ReaderTaskStatus.IDLE && readerStatus != ReaderTaskStatus.RUNNING;
		}

		public boolean isRunning() {
			return readerStatus == ReaderTaskStatus.RUNNING;
		}

		public void cancel() {
			readerStatus = ReaderTaskStatus.CANCELED;
		}

		@Override
		public void run() {
			readerStatus = ReaderTaskStatus.RUNNING;
			this.threadName = Thread.currentThread().getName();

			int length = 0;
			int read = 0;
			byte[] buffer = new byte[AUDIO_CHUNK];
			if (logger.isDebugEnabled())
				logger.debug("[{}] sending audio with packet size = {} bytes)", handle, AUDIO_CHUNK);
			try {
				while (isListening() && read != -1 && !isCancelled()) {

					long start = System.currentTimeMillis();
					read = audio.read(buffer);

					if (read > 0) {
						length += read;
						sendAudio(buffer, read, audio.getContentType(), false);
					} else if (read < 0) {
						sendAudio(new byte[] {}, 0, audio.getContentType(), true);
					}

					logger.trace("[{}] read = {}; last = {}; sleep = {}; listening = {};", handle, read, (read <= 0),
							System.currentTimeMillis() - start, (isListening()));
				}
			} catch (Exception e) {
				logger.error("[{}] Error reading audio source", handle, e);
			} finally {
				logger.debug("[{}] {} bytes sent. Reader task finished.", handle, length);

				synchronized (audio) {
					readerStatus = ReaderTaskStatus.FINISHED;
					audio.notifyAll();
				}

				try {
					audio.close();
				} catch (Exception e) {
					logger.error("[{}] Error closing audio source", handle, e);
				}
			}
		}

		@Override
		public String toString() {
			return "SendAudioTask [threadName=" + threadName + ", status=" + readerStatus + "]";
		}
	}
}
