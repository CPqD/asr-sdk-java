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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.websocket.DecodeException;

import br.com.cpqd.asr.exception.UnsupportedDataException;
import br.com.cpqd.asr.protocol.RecognitionParameters.Header;

/**
 * This message starts the speech recognition process.
 * 
 */
public class StartRecognition extends AsrMessage {
	/** The Language Model. */
	private LanguageModel lm;

	/**
	 * Defines the format (content type) of the response message containing the
	 * recognition result message.
	 */
	private String accept;

	/** Additional headers containing the recognition parameters. */
	private HashMap<String, String> parameters = new LinkedHashMap<>();

	private String mediaType;
	private Boolean verifyBuffer;

	public StartRecognition() {
		super();
		this.lm = new LanguageModel();
		setmType(AsrMessageType.START_RECOGNITION);
	}

	public LanguageModel getLanguageModel() {
		return lm;
	}

	public void setLanguageModel(LanguageModel lm) {
		this.lm = lm;
	}

	public String getAccept() {
		return accept;
	}

	public void setAccept(String contentType) {
		this.accept = contentType;
	}

	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public Boolean getVerifyBuffer() {
		return verifyBuffer;
	}

	public void setVerifyBuffer(Boolean verifyBuffer) {
		this.verifyBuffer = verifyBuffer;
	}

	/**
	 * Obtém a relação de parametros para o reconhecimento.
	 * 
	 * @return mapa com os parâmetros.
	 */
	public HashMap<String, String> getRecognitionParameters() {
		return parameters;
	}

	/**
	 * Sets the recognition parameters.
	 * 
	 * @param parameters
	 *            header map.
	 */
	public void setRecognitionParameters(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}

	@Override
	public void populate(HashMap<String, String> headers, byte[] content) throws DecodeException {
		for (String header : headers.keySet()) {
			try {
				if ("content-length".equals(header)) {
					setContentLength(Integer.parseInt(headers.get(header)));
					if (content == null && this.getContentLength() > 0) {
						logger.warn("Invalid content-length header: {}. Content is null", headers.get(header));
					}
				} else if ("content-type".equals(header)) {
					setContentType(headers.get(header));
					if (content == null && this.getContentType() != null) {
						logger.warn("Invalid content-type header: {}. Content is null", headers.get(header));
					}
					if (!(APPLICATION_GRAMMAR_XML.equals(headers.get(header))
							|| APPLICATION_SRGS_XML.equals(headers.get(header))
							|| APPLICATION_SRGS.equals(headers.get(header))
							|| APPLICATION_XML.equals(headers.get(header)) || TEXT_PLAIN.equals(headers.get(header))
							|| TEXT_URI_LIST.equals(headers.get(header)) || TEXT_XML.equals(headers.get(header)))) {

						throw new UnsupportedDataException(headers.get(header),
								"Unsupported content-type: " + headers.get(header));
					}
				} else if ("accept".equals(header)) {
					this.accept = headers.get(header);
				} else if ("content-id".equals(header)) {
					this.lm.setId(headers.get(header));
				} else if ("Media-Type".equalsIgnoreCase(header)) {
					this.mediaType = headers.get(header);
				} else if ("Verify-Buffer-Utterance".equalsIgnoreCase(header)) {
					this.verifyBuffer = Boolean.getBoolean(headers.get(header));
				} else {
					// verifica se o header é um parametro de reconhecimento valido
					Header h = Header.fromHeader(header);
					if (h != null) {
						// define o nome do parametro no mapa (evita a conversao tolowerCase())
						parameters.put(h.getHeader(), headers.get(header));
					} else {
						// adiciona o header invalido para poder retornar aviso de erro para o client
						logger.warn("Invalid header: {} = {}", header, headers.get(header));
						parameters.put(header, headers.get(header));
					}
				}
			} catch (Exception e) {
				if (e instanceof DecodeException || e instanceof UnsupportedDataException)
					throw e;
				logger.error("Error parsing header [{} = {}] : {}", header, headers.get(header), e.getMessage());
				throw new DecodeException(header + ": " + headers.get(header),
						"Error parsing header [" + header + ": " + headers.get(header) + "]");
			}
		}

		if (content != null) {
			setContent(content);
			if (content.length != this.getContentLength()) {
				logger.warn("Actual content length [{}] differs from header: {}", content.length,
						this.getContentLength());
			}

			if (getContentType().equals(TEXT_URI_LIST)) {
				// modelo ligua definido como lista URI
				ArrayList<String> uriList = new ArrayList<>();
				String[] uris = new String(content).split("\\r?\\n");
				for (int i = 0; i < uris.length; i++) {
					String str = uris[i].trim();
					if (str.length() > 0) {
						try {
							URI uri = new URI(str);
							String scheme = uri.getScheme().toLowerCase();
							if (scheme.startsWith("file") || scheme.startsWith("http") || scheme.startsWith("builtin")
									|| scheme.startsWith("session")) {
								uriList.add(str);
							} else {
								logger.warn("Ignoring unhandled URI: " + str);
							}
						} catch (URISyntaxException e) {
							logger.warn("Ignoring invalid URI [{}]: {} ", str, e.getMessage());
						}
					}
				}
				String[] array = new String[uriList.size()];
				array = uriList.toArray(array);
				this.lm.setUri(array);
			} else if (getContentType().equals(TEXT_PLAIN) || getContentType().equals(TEXT_XML)
					|| getContentType().equals(APPLICATION_GRAMMAR_XML) || getContentType().equals(APPLICATION_SRGS_XML)
					|| getContentType().equals(APPLICATION_SRGS) || getContentType().equals(APPLICATION_XML)) {
				this.lm.setDefinition(new String(content));
			}
		}
	}

	@Override
	public HashMap<String, String> getHeaders() {
		HashMap<String, String> map = new HashMap<>();
		if (this.lm.getId() != null) {
			map.put("Content-ID", this.lm.getId());
		}
		if (this.accept != null) {
			map.put("Accept", this.accept);
		}
		if (this.mediaType != null) {
			map.put("Media-Type", this.mediaType);
		}
		if (this.verifyBuffer != null) {
			map.put("Verify-Buffer-Utterance", this.verifyBuffer.toString());
		}
		// adiciona header extras (parametros para o reconhecimento)
		for (String key : parameters.keySet()) {
			map.put(key, parameters.get(key));
		}
		return map;
	}

	@Override
	public byte[] getContent() {
		if (this.lm.getDefinition() != null) {
			try {
				byte[] data = this.lm.getDefinition().getBytes(ENCODING);
				setContent(data);
				setContentLength(data.length);
				if (super.getContentType() == null) {
					setContentType(APPLICATION_SRGS);
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Error encoding language model file", e);
			}

		} else if (this.lm.getUri() != null && this.lm.getUri().length > 0) {
			StringBuffer strBuff = new StringBuffer();
			for (String uri : this.lm.getUri()) {
				if (uri != null) {
					strBuff.append(uri.trim() + "\n");
				}
			}
			try {
				byte[] data = strBuff.toString().getBytes(ENCODING);
				setContent(data);
				setContentLength(data.length);
				setContentType(TEXT_URI_LIST);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Error encoding language model URI", e);
			}
		}
		return super.getContent();
	}

	@Override
	public int getContentLength() {
		if (getContent() == null) {
			return 0;
		} else {
			return super.getContentLength();
		}
	}

	@Override
	public String getContentType() {
		if (getContent() == null) {
			return null;
		} else {
			return super.getContentType();
		}
	}

}
