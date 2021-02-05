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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * AudioSource implementation for a file audio source.
 *
 */
public class FileAudioSource implements AudioSource {

	private InputStream inputStream;

	private String fileName;

	private boolean finished = false;

	private String contentType;

	/**
	 * Creates a new instance.
	 *
	 * @param file
	 *            the file.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public FileAudioSource(File file) throws IOException {
		this(file, true, "audio/raw");
	}

	/**
	 * Creates a new instance.
	 *
	 * @param file File to be read
	 * @param decodeAudio 'true' if audio must be converted to RAW before sending.
	 * @param contentType The output audio format (after decoding).
	 *
	 * @throws IOException
	 */
	public FileAudioSource(File file, boolean decodeAudio, String contentType) throws IOException {
		this.fileName = file.getName();
		this.contentType = decodeAudio ? AudioSource.AUDIO_TYPE_RAW : contentType;

		try {
			if (decodeAudio) {
				this.inputStream = AudioSystem.getAudioInputStream(file);
			} else {
				this.inputStream = new FileInputStream(file);
			}
		} catch (UnsupportedAudioFileException e) {
			throw new IOException(e);
		}
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public int read(byte[] b) throws IOException, NullPointerException {
		if (finished) return -1;
		return inputStream.read(b, 0, b.length);
	}

	@Override
	public void close() throws IOException {
		inputStream.close();
	}

	@Override
	public void finish() throws IOException {
		finished = true;
	}

	@Override
	public String toString() {
		return "FileAudioSource [" + fileName + "]";
	}

}
