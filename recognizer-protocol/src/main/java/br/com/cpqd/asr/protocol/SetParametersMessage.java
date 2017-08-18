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
 * Mensagem utilizada para definir parâmetros de uma sessão de reconhecimento.
 * 
 */
public class SetParametersMessage extends AsrMessage {

	/** headers para parametros de sessão. */
	private HashMap<String, String> parameters = new LinkedHashMap<String, String>();

	public SetParametersMessage() {
		setmType(AsrMessageType.SET_PARAMETERS);
	}

	@Override
	public void populate(HashMap<String, String> headers, byte[] content) {
		for (String header : headers.keySet()) {
			// verifica se o header é um parametro de reconhecimento valido
			Header h = Header.fromHeader(header);
			if (h != null) {
				// define o nome do parametro no mapa (evita a conversao tolowerCase())
				parameters.put(h.getHeader(), headers.get(header));
			} else {
				// adiciona o header invalido para poder retornar aviso de erro para o client
				logger.warn("Invalid header: {} = {}", header, headers.get(header));
				parameters.put(header, headers.get(header));
			}
		}
	}

	@Override
	public HashMap<String, String> getHeaders() {
		return parameters;
	}

	/**
	 * Obtém a relação de parametros para o reconhecimento.
	 * 
	 * @return mapa com os parâmetros.
	 */
	public HashMap<String, String> getRecognitionParameters() {
		return parameters;
	}

	/**
	 * Define o mapa com os parametros de reconhecimento.
	 * 
	 * @param parameters
	 *            mapa com os parâmetros.
	 */
	public void setRecognitionParameters(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}
}
