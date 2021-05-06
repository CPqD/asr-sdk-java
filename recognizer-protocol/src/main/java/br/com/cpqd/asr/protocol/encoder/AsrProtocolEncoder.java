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
package br.com.cpqd.asr.protocol.encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.cpqd.asr.protocol.AsrMessage;
import br.com.cpqd.asr.protocol.AsrMessage.AsrMessageType;

import static br.com.cpqd.asr.protocol.AsrMessage.AsrMessageType.SEND_AUDIO;

/**
 * ASR Server WebSocket Messages Encoder.
 * 
 */
public class AsrProtocolEncoder implements Encoder.BinaryStream<AsrMessage>, Decoder.BinaryStream<AsrMessage> {

	private static Logger logger = LoggerFactory.getLogger(AsrProtocolEncoder.class.getName());

	private static final String PROTOCOL_NAME = "ASR";
	private static final String PROTOCOL_MAJOR_VERSION = "2";
	private static final String PROTOCOL_MINOR_VERSION = "3";

	public static final Charset UTF_8 = StandardCharsets.UTF_8;
	private static final byte[] b = { 13, 10 };
	private static final String CRLF = new String(b);

	@Override
	public void init(EndpointConfig config) {
		//
	}

	@Override
	public void destroy() {
		//
	}

	@Override
	public AsrMessage decode(InputStream is) throws DecodeException, IOException {

		StringBuffer stb = new StringBuffer();
		AsrMessageType messageType = null;
		byte[] content = null;
		byte[] buf = getBytesFromInputStream(is);
		int index = 0;

		// imprime a mensagem de entrada, para log de debug
		logMessage(buf);

		// le a primeira linha do protocolo para obter a messageType
		index = readLine(buf, index, stb);
		String line = stb.toString();
		stb.delete(0, line.length()); // limpa o stb

		String[] firstLine = line.toString().split(" ");
		// ignora a minor versions
		if (!line.startsWith(PROTOCOL_NAME + " " + PROTOCOL_MAJOR_VERSION)) {
			throw new RuntimeException("Error decoding message. Protocol not supported: " + line);
		} else {
			try {
				messageType = AsrMessageType.valueOf(firstLine[2].toUpperCase());
			} catch (Exception e) {
			}

			if (messageType == null) {
				throw new DecodeException(line, "Invalid message type: " + firstLine[2]);
			}
		}

		// mapa com os headers e conteudo da mensagem
		HashMap<String, String> headerMap = new HashMap<>();

		try {
			// comeca a ler os header e conteudo até o final
			int contentLength = 0; // armazena o content-length
			while (index != -1) {
				// le linha por linha, qdo nao tiver mais CRLF retorna -1
				index = readLine(buf, index, stb);
				line = stb.toString();
				stb.delete(0, line.length());

				String[] header = line.split(":", 2); // evita quebra da mensagem se valor possuir ':'
				if (header.length > 1) {
					if (header[0].toLowerCase().equals("content-length")) {
						// armazena o content-length
						contentLength = Integer.parseInt(header[1].trim());
						headerMap.put(header[0].toLowerCase(), header[1].trim());

					} else if (header[0].toLowerCase().equals("content-type")) {
						// formata o content-type em minusculas (para padronizar
						// a leitura posterior)
						headerMap.put(header[0].toLowerCase(), header[1].trim().toLowerCase());

					} else {
						// adiciona o header no mapa
						headerMap.put(header[0].toLowerCase(), header[1].trim());
					}
				} else {
					// nao conseguiu encontrar um header
					if (line.trim().equals("")) {
						// linha em branco separadora: verifica se existe
						// conteudo no corpo da mensagem
						if (index > 0 && contentLength > 0) {
							// le o conteudo (quantidade de bytes indicada)
							content = Arrays.copyOfRange(buf, index, index + contentLength);
							logger.trace("Read " + contentLength + " bytes from content");

							// acabou headers
							index = -1;
						}
					} else {
						// adiciona o header no mapa, sem valor associado
						headerMap.put(header[0].toLowerCase(), "");
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error parsing line: " + line);
			throw new DecodeException(line, "Error parsing line");
		}

		// monta a mensagem com header e conteudo
		AsrMessage message = AsrMessage.createMessage(messageType, firstLine[1].trim(), headerMap, content);
		if (message == null) {
			throw new DecodeException(messageType.toString(), "Invalid message type");
		}
		return message;
	}

	@Override
	public void encode(AsrMessage message, OutputStream os) throws IOException {

		// primeira linha do protocolo
		String protocol = new String(PROTOCOL_NAME + " " +
				Optional.ofNullable(message.getProtocolVersion()).orElse(PROTOCOL_MAJOR_VERSION + "." + PROTOCOL_MINOR_VERSION) +
				" " + message.getmType() + CRLF);

		// headers
		HashMap<String, String> map = message.getHeaders();
		for (String key : map.keySet()) {
			if (key.trim().toLowerCase().equals("content-length") || key.trim().toLowerCase().equals("content-type")) {
				continue;
			}
			protocol += key.trim() + ": " + (map.get(key) != null ? map.get(key) : "") + CRLF;
		}

		if (message.getContent() != null) {
			protocol += "Content-Length: " + message.getContentLength() + CRLF;
			protocol += "Content-Type: " + message.getContentType() + CRLF;
		}

		// linha em branco para finalizar a parte dos headers
		protocol += CRLF;

		try {
			os.write(protocol.getBytes());

			// escreve o body content
			if (message.getContent() != null) {
				os.write(message.getContent(), 0, message.getContentLength());
			}
			os.flush();
		} finally {
			os.close();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Message sent: " + protocol +
					(message.getmType() != SEND_AUDIO && message.getContent() != null ?
							new String(message.getContent()) : ""));
		}
	}

	/**
	 * Read bytes from an InputStream to a byte array.
	 * 
	 * @param is
	 *            data inputstream from the WebSocket channel.
	 * @return the bytes read from the input stream.
	 * 
	 * @throws IOException
	 *             in case any I/O error occurs.
	 */
	public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
			byte[] buffer = new byte[0xFFFF];
			int bytesRead = 0;
			for (int len; (len = is.read(buffer)) != -1;) {
				os.write(buffer, 0, len);
				bytesRead += len;
			}

			logger.trace("Bytes read: " + bytesRead);
			os.flush();
			return os.toByteArray();
		}
	}

	/**
	 * Read a line of the ASR message from a byte array. Each byte is read until a
	 * CRLF or LF sequence is found. The data is copied to the StringBuffer
	 * reference.
	 * 
	 * @param buf
	 *            input byte array.
	 * @param start
	 *            initial read position.
	 * @param line
	 *            the protocol line, which is data read until a CRLF or LF is found.
	 * @return last byte read position, or -1 if the end of the array is reached.
	 */
	private static int readLine(byte[] buf, int start, StringBuffer line) {
		if (0 <= start && start < buf.length - 1) {
			for (int index = start; index < buf.length; index++) {
				// look for CRLF or LF
				if (buf[index] == 10) { // New Line
					if (index > 1 && buf[index - 1] == 13) { // Carriage return
						line.append(new String(Arrays.copyOfRange(buf, start, index - 1)));
					} else {
						line.append(new String(Arrays.copyOfRange(buf, start, index)));
					}
					return index + 1;
				}
			}
			// se nao encontrou o CRLF, devolve a linha final
			line.append(new String(Arrays.copyOfRange(buf, start, buf.length)));
		}
		return -1;
	}

	private static void logMessage(byte[] buf) {
		if (logger.isDebugEnabled()) {

			int index = 0;
			StringBuffer stb = new StringBuffer();
			StringBuffer logmessage = new StringBuffer();

			// le a primeira linha do protocolo para obter a messageType
			index = readLine(buf, index, stb);
			String line = stb.toString();
			stb.delete(0, line.length()); // limpa o stb

			String[] firstLine = line.toString().split(" ");
			// verifica se é mensagem de audio
			if (!AsrMessageType.valueOf(firstLine[2].toUpperCase()).equals(SEND_AUDIO)) {
				// se nao é audio, copia o buffer todo para o log
				logmessage.append(new String(buf));
			} else {
				logmessage.append(line + "\n");
				// filtra o conteudo
				while (index != -1) {
					// le linha por linha, qdo nao tiver mais CRLF retorna -1
					index = readLine(buf, index, stb);
					line = stb.toString();
					stb.delete(0, line.length());
					if (line.trim().equals("")) {
						// nao conseguiu encontrar um header, encerra
						logmessage.append("-- audio content supressed --\n");
						break;
					} else {
						logmessage.append(line + "\n");
					}
				}
			}

			logger.debug("Received message: \n" + logmessage.toString());
		}
	}

}
