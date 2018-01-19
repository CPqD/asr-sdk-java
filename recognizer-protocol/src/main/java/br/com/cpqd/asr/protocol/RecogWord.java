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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A recognized word.
 *
 */
public class RecogWord {

	/** The word text. */
	private String text;

	/** The confidence score. */
	@JsonProperty("score")
	private int confidence;

	/** The start time in seconds. */
	@JsonProperty("start_time")
	private Float startTime;

	/** The end time in seconds. */
	@JsonProperty("end_time")
	private Float endTime;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getConfidence() {
		return confidence;
	}

	public void setConfidence(int confidence) {
		this.confidence = confidence;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + confidence;
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		RecogWord other = (RecogWord) obj;
		if (confidence != other.confidence)
			return false;
		if (endTime == null) {
			if (other.endTime != null)
				return false;
		} else if (!endTime.equals(other.endTime))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
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
		return "RecogWord [text=" + text + ", confidence=" + confidence + ", startTime=" + startTime + ", endTime="
				+ endTime + "]";
	}

}
