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
import java.util.Map;

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
		endpointerUseToneDetectors("endpointer.useToneDetectors"),
		decoderConfidenceThreshold("decoder.confidenceThreshold"),
		decoderContinuousMode("decoder.continuousMode"),
		decoderWordDetails("decoder.wordDetails"),
		endpointerMaxSegmentDuration("endpointer.maxSegmentDuration"),
		endpointerSegmentOverlapTime("endpointer.segmentOverlapTime"),
		hintsWords("hints.words"),
		textifyEnabled("textify.enabled"),
		textifyFormattingEnabled("textify.formatting.enabled"),
		textifyFormattingRules("textify.formatting.rules"),
		loggingTag("loggingTag"),

		// versao 2.4 - speech server
		inferAgeEnabled("Infer-age-enabled"),
		inferEmotionEnabled("Infer-emotion-enabled"),
		inferGenderEnabled("Infer-gender-enabled");

		/** valor do header na comunicação websocket. */
		private String headerName;

		private Header(String header) {
			this.headerName = header;
		}

		public String getHeader() {
			return this.headerName;
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
				if (h.getHeader().equalsIgnoreCase(header))
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
	private Float endpointerLevelThreshold;
	private Integer endpointerLevelMode;
	private Integer endpointerAutoLevelLen;
	private Boolean endpointerUseToneDetectors;
	private Integer decoderConfidenceThreshold;
	private Boolean decoderContinuousMode;
	private String decoderWordDetails;
	private Integer endpointerMaxSegmentDuration;
	private Integer endpointerSegmentOverlapTime;
	private String hintsWords;
	private Boolean textifyEnabled;
	private Boolean textifyFormattingEnabled;
	private String textifyFormattingRules;
	private String loggingTag;

	private Boolean inferAgeEnabled;
	private Boolean inferEmotionEnabled;
	private Boolean inferGenderEnabled;

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
	public RecognitionParameters(Map<String, String> parameters) {

		Method[] mList = RecognitionParameters.class.getDeclaredMethods();

		for (Method m : mList) {
			if (m.getName().startsWith("set")) {

				// obtem o atributo da classe, a partir do metodo setter
				String param = m.getName().substring(3, 4).toLowerCase() + m.getName().substring(4);
				// obtem o parametro equivalente, a partir do atributo da classe
				// RecognitionParameters
				Header h = Header.fromHeader(param);
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
						logger.warn("Argument type not handled {} for parameter {}", parameterTypes[0], h);
					}
				} catch (Exception e) {
					logger.error("Error parsing attribute {}: {}", param, e.getMessage());
				}
			}
		}

	}

	@JsonIgnore
	public Map<String, String> getParameterMap() {
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
				logger.error("Error invoking method: {}(): {}", mName, e.getMessage());
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

	public Float getEndpointerLevelThreshold() {
		return endpointerLevelThreshold;
	}

	public void setEndpointerLevelThreshold(Float endpointerLevelThreshold) {
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

	public Boolean getEndpointerUseToneDetectors() {
		return endpointerUseToneDetectors;
	}

	public void setEndpointerUseToneDetectors(Boolean endpointerUseToneDetectors) {
		this.endpointerUseToneDetectors = endpointerUseToneDetectors;
	}

	public Boolean getDecoderContinuousMode() {
		return decoderContinuousMode;
	}

	public void setDecoderContinuousMode(Boolean decoderContinuousMode) {
		this.decoderContinuousMode = decoderContinuousMode;
	}

	public String getDecoderWordDetails() {
		return decoderWordDetails;
	}

	public void setDecoderWordDetails(String decoderWordDetails) {
		this.decoderWordDetails = decoderWordDetails;
	}

	public Integer getEndpointerMaxSegmentDuration() {
		return endpointerMaxSegmentDuration;
	}

	public void setEndpointerMaxSegmentDuration(Integer endpointerMaxSegmentDuration) {
		this.endpointerMaxSegmentDuration = endpointerMaxSegmentDuration;
	}

	public Integer getEndpointerSegmentOverlapTime() {
		return endpointerSegmentOverlapTime;
	}

	public void setEndpointerSegmentOverlapTime(Integer endpointerSegmentOverlapTime) {
		this.endpointerSegmentOverlapTime = endpointerSegmentOverlapTime;
	}

	public String getHintsWords() {
		return hintsWords;
	}

	public void setHintsWords(String hintsWords) {
		this.hintsWords = hintsWords;
	}

	public Boolean getTextifyEnabled() {
		return textifyEnabled;
	}

	public void setTextifyEnabled(Boolean textifyEnabled) {
		this.textifyEnabled = textifyEnabled;
	}

	public Boolean getTextifyFormattingEnabled() {
		return textifyFormattingEnabled;
	}

	public void setTextifyFormattingEnabled(Boolean textifyFormattingEnabled) {
		this.textifyFormattingEnabled = textifyFormattingEnabled;
	}

	public String getTextifyFormattingRules() {
		return textifyFormattingRules;
	}

	public void setTextifyFormattingRules(String textifyFormattingRules) {
		this.textifyFormattingRules = textifyFormattingRules;
	}

	public String getLoggingTag() {
		return loggingTag;
	}

	public void setLoggingTag(String loggingTag) {
		this.loggingTag = loggingTag;
	}

	public Boolean getInferAgeEnabled() {
		return inferAgeEnabled;
	}

	public void setInferAgeEnabled(Boolean inferAgeEnabled) {
		this.inferAgeEnabled = inferAgeEnabled;
	}

	public Boolean getInferEmotionEnabled() {
		return inferEmotionEnabled;
	}

	public void setInferEmotionEnabled(Boolean inferEmotionEnabled) {
		this.inferEmotionEnabled = inferEmotionEnabled;
	}

	public Boolean getInferGenderEnabled() {
		return inferGenderEnabled;
	}

	public void setInferGenderEnabled(Boolean inferGenderEnabled) {
		this.inferGenderEnabled = inferGenderEnabled;
	}

	@Override
	public String toString() {
		String str = this.getParameterMap().toString();
		return "RecognitionParameters [" + str.subSequence(1, str.length() - 1) + "]";
	}

	@Override
	public int hashCode() {
		return this.getParameterMap().hashCode();
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
		return other.getParameterMap().equals(this.getParameterMap());
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
