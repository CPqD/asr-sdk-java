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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Audio source implementation for a Java microphone dataline input.
 *
 */
public class MicAudioSource implements AudioSource, LineListener {

	private static Logger logger = LoggerFactory.getLogger(MicAudioSource.class.getName());

	/** the audio format. */
	private AudioFormat af;

	private TargetDataLine line;

	private boolean stopped;
	private boolean started;

	public MicAudioSource() throws LineUnavailableException {
		this(new AudioFormat(8000F, 16, 1, true, false));
	}

	public MicAudioSource(AudioFormat format) throws LineUnavailableException {
		this(format, null);
	}

	public MicAudioSource(AudioFormat format, Mixer.Info info) throws LineUnavailableException {
		if (info != null) {
			line = AudioSystem.getTargetDataLine(format, info);
		} else {
			line = AudioSystem.getTargetDataLine(format);
		}
		this.af = format;
		line.addLineListener(this);
	}

	/**
	 * Obtains the size of the buffer from which data can be read. Note that the
	 * units used are bytes, but will always correspond to an integral number of
	 * sample frames of audio data.
	 * 
	 * @return the size of the buffer in bytes
	 */
	public int getBufferSize() {
		return line.getBufferSize();
	}

	/**
	 * Reads data from the audio source input buffer. This method blocks until
	 * the requested amount of data, specified by the length of the buffer
	 * array, has been read. The number of bytes to be read must represent an
	 * integral number of sample frames, such that: [ bytes read ] % [frame size
	 * in bytes ] == 0
	 */
	@Override
	public int read(byte[] b) throws IOException, NullPointerException {
		// se eh a primeira leitura, abre o microfone
		if (!started) {
			try {
				line.open(this.af);
			} catch (LineUnavailableException e) {
				throw new IOException("Failure to read audio from microphone", e);
			}
			line.start();
		}

		// se o microfone nao foi fechado, faz a leitura
		if (!stopped) {
			int nBytesToRead = b.length;
			// ajusta a quantidade de bytes da leitura, se o tamanho do buffer
			// nao for multiplo do tamanho do frame
			int excessBytesInBufferLength = b.length % af.getFrameSize();
			if (excessBytesInBufferLength != 0) {
				// subtrai a sobra do tamanho da leitura
				nBytesToRead -= excessBytesInBufferLength;
			}
			return line.read(b, 0, nBytesToRead);
		} else {
			return -1;
		}

	}

	@Override
	public void close() throws IOException {
		line.close();
	}

	@Override
	public void finish() throws IOException {
		if (line.isActive()) {
			line.stop();
			line.flush();
		}
	}

	@Override
	public void update(LineEvent event) {
		logger.debug("line event [{}]: {}", event.getType(), event.getSource());
		if (event.getType().equals(LineEvent.Type.OPEN)) {
			// when mic is opened reset control flags
			started = false;
			stopped = false;
		} else if (event.getType().equals(LineEvent.Type.START)) {
			// mic recording started
			started = true;
		} else if (event.getType().equals(LineEvent.Type.STOP)) {
			// mic recording stopped
			stopped = true;
		} else if (event.getType().equals(LineEvent.Type.CLOSE)) {
		}
	}

}
