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

/**
 * Mensagem gerada pelo servidor, após o recebimento de uma amostra de áudio,
 * caso o servidor detecte início de fala no áudio.
 * 
 */
public class StartOfSpeechMessage extends AsrMessage {
	
	/** status da sessão de reconhecimento. */
	private SessionStatus sessionStatus;

	public StartOfSpeechMessage() {
		super();
		setmType(AsrMessageType.START_OF_SPEECH);
	}
	
	public StartOfSpeechMessage(Long handle, SessionStatus status) {
		super();
		setmType(AsrMessageType.START_OF_SPEECH);
		this.setHandle(handle);
		this.sessionStatus = status;
	}
	
	public SessionStatus getSessionStatus() {
		return sessionStatus;
	}

	public void setSessionStatus(SessionStatus status) {
		this.sessionStatus = status;
	}

	@Override
	public void populate(HashMap<String, String> headers, byte[] content) {
		for (String header : headers.keySet()) {
			try {
				if ("Handle".toLowerCase().equals(header)) {
					setHandle(Long.parseLong(headers.get(header)));
				} else if ("Session-Status".toLowerCase().equals(header)) {
					setSessionStatus(SessionStatus.valueOf(headers.get(header)));
				} else {
					logger.warn("Ignoring header: {} = {}", header, headers.get(header));
				}
			} catch (Exception e) {
				logger.error("Error parsing header [{} = {}] : {}", header, headers.get(header), e.getMessage());
			}
		}
	}

	@Override
	public HashMap<String, String> getHeaders() {
		HashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("Handle", Long.toString(this.getHandle()));
		if (this.getSessionStatus() != null) {
			map.put("Session-Status", this.getSessionStatus().name());	
		}
		
		return map;
	}
	
	@Override
	public String toString() {
		return "StartOfSpeechMessage [session-status=" + sessionStatus + ", handle=" + getHandle() + "]";
	}

}
