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

import java.lang.reflect.Method;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

/**
 * Contains the recognition parameters definition.
 * 
 */
public class RecognitionParameters {

	@JsonIgnore
	private static Logger logger = LoggerFactory.getLogger(RecognitionParameters.class.getName());

	/**
	 * The recognition parameter header enumeration.
	 * 
	 */
	@JsonIgnoreType
	public enum Header {

		noInputTimeoutEnabled("noInputTimeout.enabled"), 
		noInputTimeout("noInputTimeout.value"), 
		recognitionTimeoutEnabled("recognitionTimeout.enabled"), 
		recognitionTimeout("recognitionTimeout.value"), 
		decoderStartInputTimers("decoder.startInputTimers"), 
		decoderMaxSentences("decoder.maxSentences"), 
		endpointerHeadMargin("endpointer.headMargin"), 
		endpointerTailMargin("endpointer.tailMargin"), 
		endpointerWaitEnd("endpointer.waitEnd"), 
		endpointerLevelThreshold("endpointer.levelThreshold"), 
		endpointerLevelMode("endpointer.levelMode"), 
		endpointerAutoLevelLen("endpointer.autoLevelLen"), 
		decoderConfidenceThreshold("decoder.confidenceThreshold");

		/** valor do header na comunicação websocket. */
		private String header;

		private Header(String header) {
			this.header = header;
		}

		public String getHeader() {
			return this.header;
		}

		/**
		 * Obtem o enum com base no header informado pelo cliente.
		 * 
		 * @param header
		 *            nome do header.
		 * @return Enum que representa o Header.
		 */
		public static Header fromHeader(String header) {
			for (Header h : Header.values()) {
				// comparar o header convertendo para minusculo para evitar
				// erros
				if (h.getHeader().toLowerCase().equals(header.toLowerCase()))
					return h;
			}
			return null;
		}

	}

	/* atributos de configuração. */
	private Boolean noInputTimeoutEnabled;
	private Integer noInputTimeout;
	private Boolean recognitionTimeoutEnabled;
	private Integer recognitionTimeout;
	private Boolean decoderStartInputTimers;
	private Integer decoderMaxSentences;
	private Integer endpointerHeadMargin;
	private Integer endpointerTailMargin;
	private Integer endpointerWaitEnd;
	private Integer endpointerLevelThreshold;
	private Integer endpointerLevelMode;
	private Integer endpointerAutoLevelLen;
	private Integer decoderConfidenceThreshold;

	/**
	 * Obtém uma lista de todos os parametros de configuração.
	 * 
	 * @return lista com os parametros.
	 */
	public static String[] getParameters() {
		String[] params = new String[Header.values().length];
		int i = 0;
		for (Header h : Header.values()) {
			params[i++] = h.getHeader();
		}

		return params;
	}

	public RecognitionParameters() {
		super();
	}

	/**
	 * Constroi o objeto populando os atributos com base no mapa de parametros.
	 * 
	 * @param parameters
	 *            a key do mapa deve ser o atributo conforme definido no enum Header
	 */
	public RecognitionParameters(HashMap<String, String> parameters) {

		Method[] mList = RecognitionParameters.class.getDeclaredMethods();

		for (Method m : mList) {
			if (m.getName().startsWith("set")) {

				// obtem o atributo da classe, a partir do metodo setter
				String param = m.getName().substring(3, 4).toLowerCase() + m.getName().substring(4);
				// obtem o parametro equivalente, a partir do atributo da classe
				// RecognitionParameters
				Header h = Header.valueOf(param);
				if (h == null) {
					logger.warn("Parameter not mapped in Header enum: {}", param);
					continue;
				}
				// obtem o valor do parametro passado no mapa
				String stringValue = parameters.get(h.getHeader());
				if (stringValue == null)
					continue;

				try {
					Class<?>[] parameterTypes = m.getParameterTypes();
					if (parameterTypes[0].equals(Float.class)) {
						Float value = Float.valueOf(stringValue);
						m.invoke(this, value);
					} else if (parameterTypes[0].equals(Integer.class)) {
						Integer value = Integer.valueOf(stringValue);
						m.invoke(this, value);
					} else if (parameterTypes[0].equals(String.class)) {
						String value = stringValue;
						m.invoke(this, value);
					} else if (parameterTypes[0].equals(Boolean.class)) {
						Boolean value = convertToBoolean(stringValue);
						m.invoke(this, value);
					} else {
						logger.warn("Argument type not handled {} for parameter {}", parameterTypes[0].toString(),
								h.toString());
					}
				} catch (Exception e) {
					logger.error("Error parsing attribute " + param + ": " + e.getMessage());
				}
			}
		}

	}

	@JsonIgnore
	public HashMap<String, String> getParameterMap() {
		@SuppressWarnings("rawtypes")
		Class noparams[] = {};
		HashMap<String, String> paramMap = new HashMap<>();
		for (Header h : Header.values()) {
			String mName = "get" + h.name().substring(0, 1).toUpperCase() + h.name().substring(1);
			try {
				// obtem referencia ao metodo get()
				Method method = this.getClass().getDeclaredMethod(mName, noparams);
				Object obj = method.invoke(this);
				if (obj != null) {
					paramMap.put(h.getHeader(), obj.toString());
				}
			} catch (Exception e) {
				logger.error("Error invoking method: " + mName + "(): " + e.getMessage());
			}
		}
		return paramMap;
	}

	public Boolean getNoInputTimeoutEnabled() {
		return noInputTimeoutEnabled;
	}

	public void setNoInputTimeoutEnabled(Boolean noInputTimeoutEnabled) {
		this.noInputTimeoutEnabled = noInputTimeoutEnabled;
	}

	public Integer getNoInputTimeout() {
		return noInputTimeout;
	}

	public void setNoInputTimeout(Integer noInputTimeout) {
		this.noInputTimeout = noInputTimeout;
	}

	public Boolean getRecognitionTimeoutEnabled() {
		return recognitionTimeoutEnabled;
	}

	public void setRecognitionTimeoutEnabled(Boolean recognitionTimeoutEnabled) {
		this.recognitionTimeoutEnabled = recognitionTimeoutEnabled;
	}

	public Integer getRecognitionTimeout() {
		return recognitionTimeout;
	}

	public void setRecognitionTimeout(Integer recognitionTimeout) {
		this.recognitionTimeout = recognitionTimeout;
	}

	public Boolean getDecoderStartInputTimers() {
		return decoderStartInputTimers;
	}

	public void setDecoderStartInputTimers(Boolean decoderStartInputTimers) {
		this.decoderStartInputTimers = decoderStartInputTimers;
	}

	public Integer getDecoderMaxSentences() {
		return decoderMaxSentences;
	}

	public void setDecoderMaxSentences(Integer decoderMaxSentences) {
		this.decoderMaxSentences = decoderMaxSentences;
	}

	public Integer getEndpointerHeadMargin() {
		return endpointerHeadMargin;
	}

	public void setEndpointerHeadMargin(Integer endpointerHeadMargin) {
		this.endpointerHeadMargin = endpointerHeadMargin;
	}

	public Integer getEndpointerTailMargin() {
		return endpointerTailMargin;
	}

	public void setEndpointerTailMargin(Integer endpointerTailMargin) {
		this.endpointerTailMargin = endpointerTailMargin;
	}

	public Integer getEndpointerWaitEnd() {
		return endpointerWaitEnd;
	}

	public void setEndpointerWaitEnd(Integer endpointerWaitEnd) {
		this.endpointerWaitEnd = endpointerWaitEnd;
	}

	public Integer getEndpointerLevelThreshold() {
		return endpointerLevelThreshold;
	}

	public void setEndpointerLevelThreshold(Integer endpointerLevelThreshold) {
		this.endpointerLevelThreshold = endpointerLevelThreshold;
	}

	public Integer getEndpointerLevelMode() {
		return endpointerLevelMode;
	}

	public void setEndpointerLevelMode(Integer endpointerLevelMode) {
		this.endpointerLevelMode = endpointerLevelMode;
	}

	public Integer getEndpointerAutoLevelLen() {
		return endpointerAutoLevelLen;
	}

	public void setEndpointerAutoLevelLen(Integer endpointerAutoLevelLen) {
		this.endpointerAutoLevelLen = endpointerAutoLevelLen;
	}

	public Integer getDecoderConfidenceThreshold() {
		return decoderConfidenceThreshold;
	}

	public void setDecoderConfidenceThreshold(Integer decoderConfidenceThreshold) {
		this.decoderConfidenceThreshold = decoderConfidenceThreshold;
	}

	@Override
	public String toString() {
		return "RecognitionParameters [noInputTimeoutEnabled=" + noInputTimeoutEnabled + ", noInputTimeout="
				+ noInputTimeout + ", recognitionTimeoutEnabled=" + recognitionTimeoutEnabled + ", recognitionTimeout="
				+ recognitionTimeout + ", decoderStartInputTimers=" + decoderStartInputTimers + ", decoderMaxSentences="
				+ decoderMaxSentences + ", endpointerHeadMargin=" + endpointerHeadMargin + ", endpointerTailMargin="
				+ endpointerTailMargin + ", endpointerWaitEnd=" + endpointerWaitEnd + ", endpointerLevelThreshold="
				+ endpointerLevelThreshold + ", endpointerLevelMode=" + endpointerLevelMode
				+ ", endpointerAutoLevelLen=" + endpointerAutoLevelLen + ", decoderConfidenceThreshold="
				+ decoderConfidenceThreshold + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((decoderConfidenceThreshold == null) ? 0 : decoderConfidenceThreshold.hashCode());
		result = prime * result + ((decoderMaxSentences == null) ? 0 : decoderMaxSentences.hashCode());
		result = prime * result + ((decoderStartInputTimers == null) ? 0 : decoderStartInputTimers.hashCode());
		result = prime * result + ((endpointerAutoLevelLen == null) ? 0 : endpointerAutoLevelLen.hashCode());
		result = prime * result + ((endpointerHeadMargin == null) ? 0 : endpointerHeadMargin.hashCode());
		result = prime * result + ((endpointerLevelMode == null) ? 0 : endpointerLevelMode.hashCode());
		result = prime * result + ((endpointerLevelThreshold == null) ? 0 : endpointerLevelThreshold.hashCode());
		result = prime * result + ((endpointerTailMargin == null) ? 0 : endpointerTailMargin.hashCode());
		result = prime * result + ((endpointerWaitEnd == null) ? 0 : endpointerWaitEnd.hashCode());
		result = prime * result + ((noInputTimeout == null) ? 0 : noInputTimeout.hashCode());
		result = prime * result + ((noInputTimeoutEnabled == null) ? 0 : noInputTimeoutEnabled.hashCode());
		result = prime * result + ((recognitionTimeout == null) ? 0 : recognitionTimeout.hashCode());
		result = prime * result + ((recognitionTimeoutEnabled == null) ? 0 : recognitionTimeoutEnabled.hashCode());
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
		RecognitionParameters other = (RecognitionParameters) obj;
		if (decoderConfidenceThreshold == null) {
			if (other.decoderConfidenceThreshold != null)
				return false;
		} else if (!decoderConfidenceThreshold.equals(other.decoderConfidenceThreshold))
			return false;
		if (decoderMaxSentences == null) {
			if (other.decoderMaxSentences != null)
				return false;
		} else if (!decoderMaxSentences.equals(other.decoderMaxSentences))
			return false;
		if (decoderStartInputTimers == null) {
			if (other.decoderStartInputTimers != null)
				return false;
		} else if (!decoderStartInputTimers.equals(other.decoderStartInputTimers))
			return false;
		if (endpointerAutoLevelLen == null) {
			if (other.endpointerAutoLevelLen != null)
				return false;
		} else if (!endpointerAutoLevelLen.equals(other.endpointerAutoLevelLen))
			return false;
		if (endpointerHeadMargin == null) {
			if (other.endpointerHeadMargin != null)
				return false;
		} else if (!endpointerHeadMargin.equals(other.endpointerHeadMargin))
			return false;
		if (endpointerLevelMode == null) {
			if (other.endpointerLevelMode != null)
				return false;
		} else if (!endpointerLevelMode.equals(other.endpointerLevelMode))
			return false;
		if (endpointerLevelThreshold == null) {
			if (other.endpointerLevelThreshold != null)
				return false;
		} else if (!endpointerLevelThreshold.equals(other.endpointerLevelThreshold))
			return false;
		if (endpointerTailMargin == null) {
			if (other.endpointerTailMargin != null)
				return false;
		} else if (!endpointerTailMargin.equals(other.endpointerTailMargin))
			return false;
		if (endpointerWaitEnd == null) {
			if (other.endpointerWaitEnd != null)
				return false;
		} else if (!endpointerWaitEnd.equals(other.endpointerWaitEnd))
			return false;
		if (noInputTimeout == null) {
			if (other.noInputTimeout != null)
				return false;
		} else if (!noInputTimeout.equals(other.noInputTimeout))
			return false;
		if (noInputTimeoutEnabled == null) {
			if (other.noInputTimeoutEnabled != null)
				return false;
		} else if (!noInputTimeoutEnabled.equals(other.noInputTimeoutEnabled))
			return false;
		if (recognitionTimeout == null) {
			if (other.recognitionTimeout != null)
				return false;
		} else if (!recognitionTimeout.equals(other.recognitionTimeout))
			return false;
		if (recognitionTimeoutEnabled == null) {
			if (other.recognitionTimeoutEnabled != null)
				return false;
		} else if (!recognitionTimeoutEnabled.equals(other.recognitionTimeoutEnabled))
			return false;
		return true;
	}

	private boolean convertToBoolean(String value) {
		boolean returnValue = false;
		if (value != null)
			value = value.trim();
		if ("1".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)
				|| "on".equalsIgnoreCase(value))
			returnValue = true;
		return returnValue;
	}
}
