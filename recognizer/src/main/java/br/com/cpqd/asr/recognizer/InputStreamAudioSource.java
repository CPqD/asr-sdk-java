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
package br.com.cpqd.asr.recognizer;

import java.io.IOException;
import java.io.InputStream;

/**
 * AudioSource implementation for a generic InputStream source.
 *
 */
public class InputStreamAudioSource implements AudioSource {

	private InputStream inputStream;
	private String contentType;

	public InputStreamAudioSource(InputStream is) {
		this(is, "audio/raw");
	}

	/**
	 * Creates a new instance.
	 *
	 * @param is External input stream.
	 *
	 * @param contentType the audio format. Use 'audio/raw' to indicate RAW
	 * audio already compatible to ASR (Linear PCM, Signed 16 bits, sample rate
	 * 8kHz/16kHz). Use 'application/octet-stream' to let the service detect the
	 * format.
	 */
	public InputStreamAudioSource(InputStream is, String contentType) {
		this.inputStream = is;
		this.contentType = contentType;
	}

	@Override
	public int read(byte[] b) throws IOException, NullPointerException {
		return inputStream.read(b, 0, b.length);
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public void close() throws IOException {
		inputStream.close();
	}

	@Override
	public void finish() throws IOException {
		// nothing to do
	}

}
