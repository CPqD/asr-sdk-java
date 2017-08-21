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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Represents the recognition result. 
 * 
 */
@JacksonXmlRootElement(localName = "recognition_result")
public class RecognitionResult {

	@JacksonXmlElementWrapper(localName = "alternatives")
	@JacksonXmlProperty(localName = "alternative")
	private List<Sentence> alternatives = new ArrayList<>();

	@JsonProperty("result_status")
	private RecognitionStatus recognitionStatus;

	public List<Sentence> getAlternatives() {
		return alternatives;
	}

	public void setAlternatives(List<Sentence> sentences) {
		this.alternatives = sentences;
	}

	public Sentence getAlternative(int index) {
		if (alternatives != null && !alternatives.isEmpty() && index < alternatives.size() && index >= 0) {
			return alternatives.get(index);
		} else {
			return null;
		}
	}

	public RecognitionStatus getRecognitionStatus() {
		return recognitionStatus;
	}

	public void setRecognitionStatus(RecognitionStatus status) {
		this.recognitionStatus = status;
	}

	@Override
	public String toString() {
		return "RecognitionResult [recognition-status=" + recognitionStatus + ", alternatives=" + alternatives + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alternatives == null) ? 0 : alternatives.hashCode());
		result = prime * result + ((recognitionStatus == null) ? 0 : recognitionStatus.hashCode());
		return result;
	}

	/**
	 * Comparacao leva em consideracao apenas as alternativas.
	 */
	@Override
	public boolean equals(Object obj) {
		try {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RecognitionResult other = (RecognitionResult) obj;
			if (alternatives == null) {
				if (other.alternatives != null)
					return false;
			} else if (!alternatives.get(0).getText().equals(other.alternatives.get(0).getText()))
				return false;
			if (recognitionStatus != other.recognitionStatus)
				return false;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
