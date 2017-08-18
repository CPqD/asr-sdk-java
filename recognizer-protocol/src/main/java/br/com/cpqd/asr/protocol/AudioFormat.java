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

/**
 * Representa o formato de audio.
 * 
 */
public class AudioFormat {

	public enum Encoding {
		ALAW, PCM_FLOAT, PCM_SIGNED, PCM_UNSIGNED, ULAW
	};

	/** Número de canais. */
	private int channels;
	
	/** Formato da codificação. */
	private Encoding encoding;
	
	/** Taxa de amostragem. */
	private float sampleRate;
	
	/** Número de bits de uma amostra. */
	private int sampleSizeInBits;

	public AudioFormat() {
		super();
	}

	public AudioFormat(int channels, Encoding encoding, float sampleRate, int sampleSizeInBits) {
		super();
		this.channels = channels;
		this.encoding = encoding;
		this.sampleRate = sampleRate;
		this.sampleSizeInBits = sampleSizeInBits;
	}

	public int getChannels() {
		return channels;
	}

	public void setChannels(int channels) {
		this.channels = channels;
	}

	public Encoding getEncoding() {
		return encoding;
	}

	public void setEncoding(Encoding encoding) {
		this.encoding = encoding;
	}

	public float getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int getSampleSizeInBits() {
		return sampleSizeInBits;
	}

	public void setSampleSizeInBits(int sampleSizeInBits) {
		this.sampleSizeInBits = sampleSizeInBits;
	}

	@Override
	public String toString() {
		return "AudioFormat [channels=" + channels + ", encoding=" + encoding
				+ ", sampleRate=" + sampleRate + ", sampleSizeInBits=" + sampleSizeInBits + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + channels;
		result = prime * result + ((encoding == null) ? 0 : encoding.hashCode());
		result = prime * result + Float.floatToIntBits(sampleRate);
		result = prime * result + sampleSizeInBits;
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
		AudioFormat other = (AudioFormat) obj;
		if (channels != other.channels)
			return false;
		if (encoding != other.encoding)
			return false;
		if (Float.floatToIntBits(sampleRate) != Float.floatToIntBits(other.sampleRate))
			return false;
		if (sampleSizeInBits != other.sampleSizeInBits)
			return false;
		return true;
	}

}
