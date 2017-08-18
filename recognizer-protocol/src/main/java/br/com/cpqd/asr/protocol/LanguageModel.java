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

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Representa uma modelo de língua.
 * 
 */
public class LanguageModel {

	/** Caminho para arquivo do modelo no servidor. */
	@JsonInclude(Include.NON_EMPTY)
	private String[] uri;

	/** Definição do modelo, em formado XML ou BNF. */
	@JsonInclude(Include.NON_EMPTY)
	private String definition;

	/** Indica o formato da definição. */
	@JsonIgnore
	private String contentType;

	/** Identificação do modelo. */
	@JsonInclude(Include.NON_EMPTY)
	private String id;

	public LanguageModel() {
		super();
	}

	public LanguageModel(String... uri) {
		super();
		this.uri = uri;
	}

	public String[] getUri() {
		return uri;
	}

	public void setUri(String... uri) {
		this.uri = uri;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public String toString() {
		return "LanguageModel [uri=" + Arrays.toString(uri) + ", definition=" + definition + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((definition == null) ? 0 : definition.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + Arrays.hashCode(uri);
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
		LanguageModel other = (LanguageModel) obj;
		if (definition == null) {
			if (other.definition != null)
				return false;
		} else if (!definition.equals(other.definition))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (!Arrays.equals(uri, other.uri))
			return false;
		return true;
	}

}
