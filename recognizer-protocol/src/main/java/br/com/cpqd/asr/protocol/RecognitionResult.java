/*******************************************************************************
 * Copyright 2018 CPqD. All Rights Reserved.
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
import java.util.HashMap;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import com.fasterxml.jackson.annotation.JsonInclude;
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

	@JsonProperty("segment_index")
	private int segmentIndex;

	@JsonProperty("last_segment")
	private boolean lastSegment;

	@JsonProperty("final_result")
	private boolean finalResult;

	@JsonProperty("start_time")
	private Float startTime;

	@JsonProperty("end_time")
	private Float endTime;

	@JsonProperty("result_status")
	private RecognitionStatus recognitionStatus;

	@JacksonXmlElementWrapper(localName = "alternatives")
	@JacksonXmlProperty(localName = "alternative")
	private List<RecognitionAlternative> alternatives = new ArrayList<>();

	// Speech Server
	@JsonProperty("age_scores") @JsonInclude(NON_NULL)
	private HashMap<String, Object> ageScores;
	@JsonProperty("emotion_scores") @JsonInclude(NON_NULL)
	private HashMap<String, Object> emotionScores;
	@JsonProperty("gender_scores") @JsonInclude(NON_NULL)
	private HashMap<String, Object> genderScores;

	public boolean isLastSegment() {
		return lastSegment;
	}

	public void setLastSegment(boolean lastSegment) {
		this.lastSegment = lastSegment;
	}

	public int getSegmentIndex() {
		return segmentIndex;
	}

	public void setSegmentIndex(int segmentIndex) {
		this.segmentIndex = segmentIndex;
	}

	public boolean isFinalResult() {
		return finalResult;
	}

	public void setFinalResult(boolean finalResult) {
		this.finalResult = finalResult;
	}

	public Float getStartTime() {
		return startTime;
	}

	public void setStartTime(Float startTime) {
		this.startTime = startTime;
	}

	public Float getEndTime() {
		return endTime;
	}

	public void setEndTime(Float endTime) {
		this.endTime = endTime;
	}

	public List<RecognitionAlternative> getAlternatives() {
		return alternatives;
	}

	public RecognitionAlternative getAlternative(int index) {
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

	public HashMap<String, Object> getAgeScores() {
		return ageScores;
	}

	public void setAgeScores(HashMap<String, Object> ageScores) {
		this.ageScores = ageScores;
	}

	public HashMap<String, Object> getEmotionScores() {
		return emotionScores;
	}

	public void setEmotionScores(HashMap<String, Object> emotionScores) {
		this.emotionScores = emotionScores;
	}

	public HashMap<String, Object> getGenderScores() {
		return genderScores;
	}

	public void setGenderScores(HashMap<String, Object> genderScores) {
		this.genderScores = genderScores;
	}

	@Override
	public String toString() {
		return "RecognitionResult [lastSegment=" + lastSegment + ", segmentIndex=" + segmentIndex + ", finalResult="
				+ finalResult + ", alternatives=" + alternatives + ", recognitionStatus=" + recognitionStatus + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (finalResult ? 1231 : 1237);
		result = prime * result + (lastSegment ? 1231 : 1237);
		result = prime * result + ((recognitionStatus == null) ? 0 : recognitionStatus.hashCode());
		result = prime * result + segmentIndex;
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
			if (finalResult != other.finalResult)
				return false;
			if (lastSegment != other.lastSegment)
				return false;
			if (recognitionStatus != other.recognitionStatus)
				return false;
			if (segmentIndex != other.segmentIndex)
				return false;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
