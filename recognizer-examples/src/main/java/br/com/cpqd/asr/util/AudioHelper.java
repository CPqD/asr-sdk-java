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
package br.com.cpqd.asr.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioHelper {
	
	private static Logger logger = LoggerFactory.getLogger(AudioHelper.class.getName());

	public static AudioFormat getAudioFormat(byte[] content) throws UnsupportedAudioFileException, IOException {
		AudioInputStream stream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(content));
		AudioFormat format = stream.getFormat();
		return format;
	}
	
	public static AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
		AudioFileFormat aff = AudioSystem.getAudioFileFormat(file);
		return aff;
	}
	
	public static float calculateAudioLength(File file) throws UnsupportedAudioFileException, IOException {
		AudioFileFormat aff = getAudioFileFormat(file);
		return calculateAudioLength(getContent(file).length, aff.getFormat().getFrameSize(), aff.getFormat().getFrameRate());
	}
			
	/**
	 * Calcula a duração (em segundos) do arquivo de audio (WAV).
	 *  
	 * @param bufferSize tamanho do buffer (número de bytes)
	 * @param frameSize tamanho do frame (em bytes).
	 * @param frameRate taxa do frame (em segundos).
	 * @return duração em segundos do buffer
	 */
	public static float calculateAudioLength(int bufferSize, int frameSize, float frameRate) {
		float audioLength = 0;
		
		// Method 1
		audioLength = bufferSize / (frameSize * frameRate);
		
		// Method 2
//		int fileSize = content.length;
//		int sampleRate = (int) format.getSampleRate();
//		int channels = format.getChannels();
//		int bitPerSample = format.getSampleSizeInBits();
//		audioLength = fileSize / (sampleRate * channels * bitPerSample / 8);
		
		return audioLength;
	}
	
	/**
	 * Calcula o tamanho (em bytes) de um segmento de audio (WAV).
	 *  
	 * @param audioLength duração do audio, em milis. 
	 * @param audioRate taxa de audio, em bps (ex: 256000).
	 * @return tamanho do buffer (número de bytes).
	 * 
	 */
	public static int calculateBufferSize(int audioLength, int audioRate) {
		float bufferSize = audioLength * (audioRate) / 1000L / 8;
		return (int) bufferSize;
	}
	
	/**
	 * Read the audio file (with header) and return its binary content. Raw files does not work here.
	 * 
	 * @param file Audio file
	 * 
	 * @return binary content of audio file.
	 * 
	 * @throws Exception
	 */
	public static byte[] getContent(File file) {
		try {
			AudioInputStream stream = AudioSystem.getAudioInputStream(file);
			long sizeInBytes = stream.getFormat().getFrameSize() * stream.getFrameLength();
			if (sizeInBytes > Integer.MAX_VALUE) {
				throw new RuntimeException("Audio file size exceeds the maximum: " + sizeInBytes);
			}
			byte[] buffer = new byte[(int) sizeInBytes];
			stream.read(buffer, 0, (int)sizeInBytes);
			return buffer;
		} catch (Exception e) {
			throw new RuntimeException("Failure to read audio file: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Extrai um framento do audio (inclui cabeçalho) em memoria.
	 * 
	 * @param content conteúdo em bytes
	 * @param startTime tempo (em seg) de início do fragmento
	 * @param endTime tempo (em seg) de fim do fragmento
	 * @param noHeader indica se o segmento extraido deve conter o cabeçalho WAV ou não
	 * @throws Exception
	 */
	public static ByteArrayOutputStream getSegment(File audio, float startTime, float endTime, boolean noHeader) throws Exception {
		if (startTime > endTime) {
			throw new Exception("Start Time should be less than end time" + " for fragmenting audio");
		}

		AudioInputStream inAudio = AudioSystem.getAudioInputStream(audio);

		float sampleRate = inAudio.getFormat().getSampleRate();
		int sampleSize = inAudio.getFormat().getSampleSizeInBits();
		float numOfBitsBeforeStart = startTime * sampleRate * sampleSize;
		float numOfBits = (endTime - startTime) * sampleRate * sampleSize;
		int numberOfSamples = (int) numOfBits / sampleSize;

		inAudio.skip((int) numOfBitsBeforeStart / 8);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream((int) numOfBits / 8);
		
		if (noHeader) {
			byte[] buffer = new byte[(int) numOfBits / 8];
			int bytes = inAudio.read(buffer, 0, (int) numOfBits / 8); // calcular len em bytes
			logger.debug(String.format("Reading %s bytes", bytes));
			baos.write(buffer);
		} else {
			logger.debug(String.format("Reading %s samples", numberOfSamples));
			AudioSystem.write(new AudioInputStream(inAudio, inAudio.getFormat(), numberOfSamples), 
					AudioFileFormat.Type.WAVE,
					baos);
		}
				
		return baos;
	}
		
}
