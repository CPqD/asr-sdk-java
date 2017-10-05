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
package br.com.cpqd.asr.recognizer.ws;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.cpqd.asr.protocol.CancelRecognition;
import br.com.cpqd.asr.protocol.CreateSession;
import br.com.cpqd.asr.protocol.LanguageModel;
import br.com.cpqd.asr.protocol.ReleaseSession;
import br.com.cpqd.asr.protocol.ResponseMessage;
import br.com.cpqd.asr.protocol.SendAudio;
import br.com.cpqd.asr.protocol.SessionStatus;
import br.com.cpqd.asr.protocol.SetParametersMessage;
import br.com.cpqd.asr.protocol.StartRecognition;
import br.com.cpqd.asr.protocol.ResponseMessage.Result;
import br.com.cpqd.asr.recognizer.AudioSource;
import br.com.cpqd.asr.recognizer.LanguageModelList;
import br.com.cpqd.asr.recognizer.RecognitionException;
import br.com.cpqd.asr.recognizer.RecognitionListener;
import br.com.cpqd.asr.recognizer.SpeechRecognizer;
import br.com.cpqd.asr.recognizer.model.PartialRecognitionResult;
import br.com.cpqd.asr.recognizer.model.RecognitionConfig;
import br.com.cpqd.asr.recognizer.model.RecognitionError;
import br.com.cpqd.asr.recognizer.model.RecognitionErrorCode;
import br.com.cpqd.asr.recognizer.model.RecognitionResult;

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
	private BlockingQueue<Object> sentencesQueue = new ArrayBlockingQueue<>(100);

	/** the audiosource object. */
	private AudioSource audio;

	/** the asynchronous reader task. */
	private ReaderTask readerTask;

	/** Status definition of the recognizer task. */
	private enum RecognizerStatus {
		IDLE, REQUESTED, RUNNING, FINISHED
	};

	/** Status of the recognizer task. */
	private RecognizerStatus status;

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
		this.status = RecognizerStatus.IDLE;

		client = new AsrClientEndpoint(builder.url);
		client.getListeners().add(this);
		client.getListeners().addAll(builder.listeners);
		client.setCredentials(builder.credentials);
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

		if (!client.isOpen())
			throw new IOException("Websocket session is closed");

		if (this.status == RecognizerStatus.RUNNING || this.status == RecognizerStatus.REQUESTED) {
			// encerra a reader task.
			readerTask.cancel();
			logger.trace("[{}] Reader task cancelled.", handle);
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

				// recebeu resultado final. fecha a sessao
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
		if (this.status == RecognizerStatus.RUNNING || this.status == RecognizerStatus.REQUESTED) {
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
		logger.debug("[{}] Recognize called... Reader task is {}. Client is {}. Status = {}", handle,
				getReaderTaskStatus(), client.isOpen() ? "opened" : "closed", status);

		if (this.status == RecognizerStatus.RUNNING || this.status == RecognizerStatus.REQUESTED) {
			logger.warn("[{}] Another recognition is running [{}]", this.handle,
					readerTask != null ? readerTask.getThreadName() : "null");
			throw new RecognitionException(RecognitionErrorCode.FAILURE, "Another recognition is running");
		}

		if (!client.isOpen()) {
			try {
				open();
			} catch (DeploymentException e) {
				throw new IOException(e);
			}
		}

		this.status = RecognizerStatus.REQUESTED;

		// limpa eventual lixo na fila de resposta
		sentencesQueue.clear();
		this.audio = audio;
		startRecognition(lm, recogConfig);
	}

	@Override
	public synchronized List<RecognitionResult> waitRecognitionResult() throws RecognitionException {
		logger.debug("[{}] Wait called... Reader task is {}. Client is {}. Status = {}", handle, getReaderTaskStatus(),
				client.isOpen() ? "opened" : "closed", this.status);

		if (this.status == RecognizerStatus.IDLE) {
			// nunca foi gerado reconhecimento
			return new ArrayList<>();
		}

		// se o audio esta sendo enviado, bloqueia a thread aguardando o fim do
		// processo
		long start = System.currentTimeMillis();
		while (this.status != RecognizerStatus.FINISHED) {
			try {
				synchronized (audio) {
					audio.wait(3 * 1000);
				}
			} catch (InterruptedException e) {
			}
		}
		logger.debug("[{}] Waited for {} ms", handle, (System.currentTimeMillis() - start));

		if (readerTask.isCancelled() && sentencesQueue.isEmpty()) {
			// verifica se tarefa foi cancelada
			return new ArrayList<>();
		}

		Object result = null;
		try {
			result = sentencesQueue.poll(builder.maxWaitSeconds, TimeUnit.SECONDS);
			if (result == null) {
				logger.warn("[{}] Timeout waiting for recognition result.", this.handle);
				for (RecognitionListener listener : client.getListeners()) {
					listener.onError(new RecognitionError(RecognitionErrorCode.FAILURE, "Recognition timeout"));
				}
				throw new RecognitionException(RecognitionErrorCode.FAILURE, "Recognition timeout");
			} else if (result instanceof RecognitionError) {
				throw new RecognitionException((RecognitionError) result);
			} else {
				List<RecognitionResult> resultList = new ArrayList<>();
				resultList.add((RecognitionResult) result);
				return resultList;
			}
		} catch (InterruptedException e) {
			logger.error("[{}] Interrupted while waiting for recognition result.", handle, e);
			throw new RecognitionException(RecognitionErrorCode.FAILURE,
					"Interrupted while waiting for recognition result", e);
		} finally {
			// retorna para estado original; se houver chamadas em sequencia do
			// metodo wait(), evita de ficar ocorrendo timeout
			status = RecognizerStatus.IDLE;
		}
	}

	@Override
	public void onListening() {
		// cria uma thread para ler o audio source e enviar os pacotes
		// para o servidor
		readerTask = new ReaderTask(audio);
		new Thread(readerTask).start();
		logger.debug("[{}] Server is listening.", handle);
	}

	@Override
	public void onSpeechStart(Integer time) {
		logger.trace("[{}] Speech started.", handle);
	}

	@Override
	public void onSpeechStop(Integer time) {
		logger.debug("[{}] Speech stopped.", handle);

		// o servidor nao esta mais ouvindo. Encerra o envio de audio
		try {
			this.audio.finish();
		} catch (IOException e) {
			logger.error("[{}] Error calling finish recognition.", this.handle, e);
		}
	}

	@Override
	public void onRecognitionResult(RecognitionResult result) {
		logger.info("[{}] Recognition result: {}", this.handle, result.toString());
		if (!sentencesQueue.offer(result)) {
			logger.warn("[{}] Messsage discarded, sentences queue is full: {}", this.handle, result);
		}

		// recebeu resultado final. fecha a sessao
		if (builder.autoClose) {
			try {
				close();
			} catch (IOException e) {
				logger.error("[{}] Error closing session", handle, e);
			}
		}
	}

	@Override
	public void onPartialRecognitionResult(PartialRecognitionResult result) {
		logger.trace("[{}] Partial recognition result: {}", this.handle, result.toString());
	}

	@Override
	public void onError(RecognitionError error) {
		logger.info("[{}] Recognition error: {}", this.handle, error.toString());
		if (!sentencesQueue.offer(error)) {
			logger.warn("[{}] Messsage discarded, sentences queue is full: {}", this.handle, error);
		}

		// o servidor nao esta mais ouvindo. Encerra o envio de audio
		try {
			this.audio.finish();
		} catch (IOException e) {
			logger.error("[{}] Error calling finish recognition.", this.handle, e);
		}

		// recebeu resultado final. fecha a sessao
		if (builder.autoClose) {
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
	 * Sets the recognition parameters which will be valid fotr the entire
	 * session.
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
				logger.error("[{}] Error configuring session parameters: {}", handle, response.getSessionStatus(),
						response.getErrorMessage());
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
		StartRecognition message = new StartRecognition();
		message.setHandle(this.handle);

		// define o modelo de linguagem
		if (lmList != null) {
			LanguageModel languageModel = new LanguageModel();
			if (lmList.getUriList().size() > 0) {
				languageModel.setUri(lmList.getUriList().get(0));
			} else if (lmList.getGrammarList().size() > 0) {
				String[] grammar = lmList.getGrammarList().get(0);
				languageModel.setId(grammar[0]);
				languageModel.setDefinition(grammar[1]);
			}
			message.setLanguageModel(languageModel);
		}

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
	private void sendAudio(byte[] audio, int audioLength, boolean lastPacket) throws IOException {
		if (!client.isOpen())
			throw new IOException("Websocket session is closed");

		SendAudio message = new SendAudio();
		message.setHandle(this.handle);
		message.setContent(audio);
		message.setContentLength(audioLength);
		message.setContentType(SendAudio.APPLICATION_OCTET_STREAM);
		message.setLastPacket(lastPacket);

		// SEND AUDIO nao recebe confirmacao de recebimento (exceto em caso de
		// erro)
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
	 * Task that reads the audio source and send audio packets to the server, in
	 * a different thread from the client application.
	 *
	 */
	private class ReaderTask implements Runnable {

		private AudioSource audio;

		private boolean running = false;

		private boolean cancelled = false;

		private String threadName;

		public ReaderTask(AudioSource audio) {
			super();
			this.audio = audio;
		}

		public String getThreadName() {
			return this.threadName;
		}

		public boolean isCancelled() {
			return cancelled;
		}

		public boolean isRunning() {
			return running;
		}

		public void cancel() {
			cancelled = true;
		}

		@Override
		public void run() {
			this.running = true;
			this.threadName = Thread.currentThread().getName();
			status = RecognizerStatus.RUNNING;

			final int chunkSize = calculateBufferSize(builder.chunkLength, builder.audioSampleRate,
					builder.encoding.getSampleSize());
			int length = 0;
			int read = 0;
			byte[] buffer = new byte[chunkSize];
			// atraso de envio é a duracao do pacote ajustada pelo RTF
			final int DELAY = (int) (builder.chunkLength * builder.serverRTF);
			if (logger.isDebugEnabled())
				logger.debug("[{}] sending audio with packet size = {} bytes (delay = {})", handle, chunkSize, DELAY);
			try {
				while (isListening() && read != -1 && !cancelled) {

					long start = System.currentTimeMillis();
					read = audio.read(buffer);

					if (read > 0) {
						length += read;
						sendAudio(buffer, read, false);
					} else if (read < 0) {
						sendAudio(new byte[] {}, 0, true);
					}

					Thread.sleep(DELAY);
					logger.trace("[{}] read = {}; last = {}; sleep = {}; listening = {};", handle, read, (read <= 0),
							System.currentTimeMillis() - start, (isListening()));
				}
			} catch (Exception e) {
				logger.error("[{}] Error reading audio source", handle, e);
			} finally {
				logger.debug("[{}] {} bytes sent. Reader task finished.", handle, length);

				synchronized (audio) {
					this.running = false;
					status = RecognizerStatus.FINISHED;
					audio.notifyAll();
				}

				try {
					audio.close();
				} catch (Exception e) {
					logger.error("[{}] Error closing audio source", handle, e);
				}

			}
		}

		/**
		 * Calcula o tamanho (em bytes) de um segmento de audio (WAV).
		 * 
		 * @param audioLength
		 *            duração do audio, em milis.
		 * @param sampleRate
		 *            taxa de audio, em bps (ex: 16000).
		 * @param sampleSize
		 *            tamanho da amostra em bits (ex: 16).
		 * @return tamanho do buffer (número de bytes).
		 * 
		 */
		private int calculateBufferSize(int audioLength, int sampleRate, int sampleSize) {
			float bufferSize = audioLength * (sampleRate * sampleSize) / 1000L / 8;
			return (int) bufferSize;
		}

		@Override
		public String toString() {
			return "SendAudioTask [threadName=" + threadName + ", running=" + running + ", cancelled=" + cancelled
					+ "]";
		}
	}
}
