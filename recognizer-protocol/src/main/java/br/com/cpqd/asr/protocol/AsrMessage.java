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
package br.com.cpqd.asr.protocol;

import java.util.HashMap;

import javax.websocket.DecodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ASR Protocol Message base class.
 *
 */
public abstract class AsrMessage {

	protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	public static final String ENCODING = "UTF-8";

	public static final String AUDIO_WAV = "audio/wav";
	public static final String APPLICATION_GRAMMAR_XML = "application/grammar+xml";
	public static final String AUDIO_RAW = "audio/raw";
	public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
	public static final String APPLICATION_JSON = "application/json";
	public static final String APPLICATION_SRGS_XML = "application/srgs+xml";
	public static final String APPLICATION_SRGS = "application/srgs";
	public static final String APPLICATION_XML = "application/xml";
	public static final String TEXT_PLAIN = "text/plain";
	public static final String TEXT_URI_LIST = "text/uri-list";
	public static final String TEXT_XML = "text/xml";

	public enum AsrMessageType {
		CREATE_SESSION, START_RECOGNITION, CANCEL_RECOGNITION, SEND_AUDIO, RELEASE_SESSION,
		START_INPUT_TIMERS, SET_PARAMETERS, GET_PARAMETERS, DEFINE_GRAMMAR, START_OF_SPEECH,
		END_OF_SPEECH, RECOGNITION_RESULT, INTERPRET_TEXT, RESPONSE

	}

	/** Defines the message type. */
	private AsrMessageType mType;

	/** The ASR session identification number. */
	private long handle;

	/** Message body content. */
	private byte[] content;

	/** The body length in bytes. */
	private int contentLength;

	/** The message body content-type. */
	private String contentType;

	/** The message protocol version. */
	private String protocolVersion;

	public AsrMessageType getmType() {
		return mType;
	}

	public void setmType(AsrMessageType mType) {
		this.mType = mType;
	}

	public long getHandle() {
		return handle;
	}

	public void setHandle(long handle) {
		this.handle = handle;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public int getContentLength() {
		return contentLength;
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (handle ^ (handle >>> 32));
		result = prime * result + ((mType == null) ? 0 : mType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AsrMessage other = (AsrMessage) obj;
		if (handle != other.handle)
			return false;
		if (mType != other.mType)
			return false;
		return true;
	}

	/**
	 * Retorna um mapa com os headers que devem ser enviados na mensagem pelo
	 * websocket.
	 *
	 * @return mapa de headers da mensagem.
	 */
	public abstract HashMap<String, String> getHeaders();

	/**
	 * Cria uma mensagem.
	 *
	 * @param type
	 *            tipo da mensagem.
	 * @param headers
	 *            headers recebidos pelo websocket.
	 * @param content
	 *            conteúdo recebido na mensagem.
	 * @return mensagem do protocolo ASR.
	 * @throws DecodeException
	 *             em caso de erro de decodificação.
	 */
	public static AsrMessage createMessage(AsrMessageType type, String version, HashMap<String, String> headers, byte[] content)
			throws DecodeException {
		AsrMessage message = null;

		switch (type) {
		case CREATE_SESSION:
			message = new CreateSession();
			break;
		case START_RECOGNITION:
			message = new StartRecognition();
			break;
		case CANCEL_RECOGNITION:
			message = new CancelRecognition();
			break;
		case SEND_AUDIO:
			message = new SendAudio();
			break;
		case RECOGNITION_RESULT:
			message = new RecognitionResultMessage();
			break;
		case RELEASE_SESSION:
			message = new ReleaseSession();
			break;
		case RESPONSE:
			message = new ResponseMessage();
			break;
		case END_OF_SPEECH:
			message = new EndOfSpeechMessage();
			break;
		case START_OF_SPEECH:
			message = new StartOfSpeechMessage();
			break;
		case START_INPUT_TIMERS:
			message = new StartInputTimersMessage();
			break;
		case SET_PARAMETERS:
			message = new SetParametersMessage();
			break;
		case GET_PARAMETERS:
			message = new GetParametersMessage();
			break;
		case DEFINE_GRAMMAR:
			message = new DefineGrammarMessage();
			break;
		case INTERPRET_TEXT:
			message = new InterpretText();
			break;
		default:
			break;
		}

		if (message != null) {
			message.populate(headers, content);
			message.setProtocolVersion(version);
		}
		return message;
	}

	/**
	 * Método utilizado para popular os campos da mensagem, com base nos headers e
	 * conteúdo recebido.
	 *
	 * @param headers
	 *            mapa com os headers recebidos.
	 * @param content
	 *            conteúdo binário.
	 * @throws DecodeException
	 *             em caso de erro na decodificação.
	 */
	public abstract void populate(HashMap<String, String> headers, byte[] content) throws DecodeException;

	@Override
	public String toString() {
		String str = mType + " [Handle=" + handle;
		if (content != null) {
			str += ", Content-Length=" + contentLength + ", Content-Type=" + contentType;
		}
		str += "]";
		return str;
	}

}
