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
import java.util.LinkedHashMap;

import br.com.cpqd.asr.protocol.RecognitionParameters.Header;

/**
 * Response message returned by the server.
 * 
 */
public class ResponseMessage extends AsrMessage {

	public enum Result {
		SUCCESS, // acao foi executada
		FAILURE, // acao nao foi executada por falha no servidor
		INVALID_ACTION, // acao nao pode ser executada (engine em estado
						// improprio)
		INVALID_MESSAGE // mensagem mal formatada
	};

	/** The error code in case of server errors. */
	private String errorCode;

	/** The recognition session status. */
	private SessionStatus sessionStatus;

	/** The operation result. */
	private Result result;

	/** An optional message content. */
	private String message;

	/** Session timeout indication. */
	private String expires;

	/** Session parameters headers. */
	private HashMap<String, String> parameters = new LinkedHashMap<String, String>();

	private AsrMessageType method;

	public ResponseMessage() {
		super();
		setmType(AsrMessageType.RESPONSE);
	}

	public ResponseMessage(AsrMessageType method) {
		super();
		setmType(AsrMessageType.RESPONSE);
		this.method = method;
	}

	public ResponseMessage(AsrMessageType method, Result result, Long handle) {
		super();
		setmType(AsrMessageType.RESPONSE);
		this.result = result;
		this.method = method;
		if (handle != null)
			setHandle(handle);
	}

	public ResponseMessage(AsrMessageType method, Result result, Long handle, SessionStatus status) {
		super();
		setmType(AsrMessageType.RESPONSE);
		this.result = result;
		this.sessionStatus = status;
		this.method = method;
		if (handle != null)
			setHandle(handle);
	}

	/**
	 * Adds a list of message headers.
	 * 
	 * @param headers
	 *            the headers attribute map.
	 */
	public void putRecognitionParameters(HashMap<String, String> headers) {
		parameters.putAll(headers);
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public SessionStatus getSessionStatus() {
		return sessionStatus;
	}

	public void setSessionStatus(SessionStatus status) {
		this.sessionStatus = status;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public AsrMessageType getMethod() {
		return method;
	}

	public void setMethod(AsrMessageType method) {
		this.method = method;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getExpires() {
		return expires;
	}

	public void setExpires(String expires) {
		this.expires = expires;
	}

	@Override
	public String toString() {
		return "ResponseMessage [session-status=" + sessionStatus + ", result=" + result + ", method=" + method
				+ ", message=" + message + ", errorCode=" + errorCode + ", expires=" + expires + ", handle="
				+ getHandle() + "]";
	}

	@Override
	public void populate(HashMap<String, String> headers, byte[] content) {
		for (String header : headers.keySet()) {
			try {
				if ("Handle".toLowerCase().equals(header)) {
					setHandle(Long.parseLong(headers.get(header)));
				} else if ("Result".toLowerCase().equals(header)) {
					setResult(Result.valueOf(headers.get(header)));
				} else if ("Error-Code".toLowerCase().equals(header)) {
					setErrorCode(headers.get(header));
				} else if ("Session-Status".toLowerCase().equals(header)) {
					setSessionStatus(SessionStatus.valueOf(headers.get(header)));
				} else if ("Method".toLowerCase().equals(header)) {
					setMethod(AsrMessageType.valueOf(headers.get(header)));
				} else if ("Message".toLowerCase().equals(header)) {
					setMessage(headers.get(header));
				} else if ("Expires".toLowerCase().equals(header)) {
					setExpires(headers.get(header));
				} else {
					// verifica se o header é um parametro de reconhecimento
					// valido
					Header h = Header.fromHeader(header);
					if (h != null) {
						// define o nome do parametro no mapa (evita a conversao
						// tolowerCase())
						parameters.put(h.getHeader(), headers.get(header));
					} else {
						logger.warn("Ignoring header: {} = {}", header, headers.get(header));
					}
				}
			} catch (Exception e) {
				logger.error("Error parsing header [{} = {}]: {}", header, headers.get(header), e.getMessage());
			}
		}
	}

	@Override
	public HashMap<String, String> getHeaders() {
		HashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Handle", Long.toString(this.getHandle()));
		if (this.getMethod() != null) {
			map.put("Method", this.getMethod().name());
		}
		if (this.getResult() != null) {
			map.put("Result", this.getResult().name());
		}
		if (this.getSessionStatus() != null) {
			map.put("Session-Status", this.getSessionStatus().name());
		}
		if (this.getMessage() != null) {
			map.put("Message", this.getMessage());
		}
		if (this.getErrorCode() != null) {
			map.put("Error-Code", this.getErrorCode());
		}
		if (this.getExpires() != null) {
			map.put("Expires", this.getExpires());
		}
		// adiciona header extras (parametros de sessao)
		for (String key : parameters.keySet()) {
			map.put(key, parameters.get(key));
		}

		return map;
	}

	/**
	 * Returns a formatted error message containing the error code and message.
	 * 
	 * @return error message format: 'ERROR_CODE - ERROR_MESSAGE'.
	 */
	public String getErrorMessage() {
		if (errorCode != null) {
			return errorCode + " - " + message;
		} else {
			return message;
		}
	}

}
