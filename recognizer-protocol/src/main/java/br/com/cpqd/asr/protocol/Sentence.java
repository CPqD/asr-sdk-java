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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Representa uma sentença gerada pelo reconhecimento.
 * 
 */
public class Sentence {
	/** indice da sentença. */
	private int index;

	/** texto reconhecido. */
	private String text;

	/** indice de confiança. */
	private int score;

	/** interpretações (em formato JSON). */
	@JsonIgnore
	private List<String> jsonInterpretations = new ArrayList<>();

	/** lista de interpretacoes. */
	@JacksonXmlElementWrapper(localName = "interpretations")
	@JacksonXmlProperty(localName = "interpretation")
	@JsonInclude(Include.NON_EMPTY)
	private List<Object> interpretations = new ArrayList<>();

	/** JSON Mapper. */
	private static final ObjectMapper jsonMapper = new ObjectMapper()
			.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public List<String> getJsonInterpretations() {
		return jsonInterpretations;
	}

	public void setJsonInterpretations(List<String> jsonInterpretations) {
		this.jsonInterpretations = jsonInterpretations;
	}

	public List<Object> getInterpretations() {
		return interpretations;
	}

	public void setInterpretations(List<Object> interpretations) {
		this.interpretations = interpretations;
	}

	/**
	 * Parse the interpretation result list into a user-defined object list.
	 * 
	 * @param clazz
	 *            the object type.
	 * @return the sentence interpretations.
	 * @throws JsonParseException
	 *             some parse error has occurred.
	 * @throws JsonMappingException
	 *             can not map JSON to object.
	 * @throws IOException
	 *             some I/O error has occurred.
	 */
	public List<?> getInterpretations(Class<?> clazz) throws JsonParseException, JsonMappingException, IOException {
		List<?> list = jsonMapper.readValue(jsonInterpretations.toString().getBytes(),
				jsonMapper.getTypeFactory().constructCollectionType(List.class, clazz));
		return list;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jsonInterpretations == null) ? 0 : jsonInterpretations.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	/**
	 * Comparacao leva apenas em consideracao o texto e o resultado de
	 * interpretacao.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sentence other = (Sentence) obj;
		if (jsonInterpretations == null) {
			if (other.jsonInterpretations != null)
				return false;
		} else if (!jsonInterpretations.equals(other.jsonInterpretations))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Sentence [index=" + index + ", text=" + text + ", score=" + score + ", interpretations="
				+ (jsonInterpretations.size() == 0 ? interpretations : jsonInterpretations) + "]";
	}

}
