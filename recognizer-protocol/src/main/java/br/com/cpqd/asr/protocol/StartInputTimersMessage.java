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

/**
 * Mensagem que inicia o temporizador de início de envio de áudio, da engine ASR.
 * 
 */
public class StartInputTimersMessage extends AsrMessage {

	private final HashMap<String, String> map = new HashMap<String, String>();
	
	public StartInputTimersMessage() {
		setmType(AsrMessageType.START_INPUT_TIMERS);
	}
		
	@Override
	public HashMap<String, String> getHeaders() {
		return map;
	}

	@Override
	public void populate(HashMap<String, String> headers, byte[] content) {
		for (String header : headers.keySet()) {
			logger.warn("Ignoring header: {} = {}", header, headers.get(header));
		}
	}

}
