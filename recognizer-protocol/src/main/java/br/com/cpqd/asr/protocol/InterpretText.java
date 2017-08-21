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
import java.util.ArrayList;
import java.util.HashMap;

import javax.websocket.DecodeException;

import br.com.cpqd.asr.exception.UnsupportedDataException;

/**
 * This message sends a text phrase to be interpreted by a grammar.
 *
 */
public class InterpretText extends AsrMessage {

	/** The Language Model. */
	private LanguageModel lm;

	/** Text content for interpretation. */
	private String text;

	/** Defines the response format (content-type) of the interpretation result.  */
	private String accept;

	public InterpretText() {
		this.lm = new LanguageModel();
		setmType(AsrMessageType.INTERPRET_TEXT);
	}

	public LanguageModel getLanguageModel() {
		return lm;
	}

	public void setLanguageModel(LanguageModel lm) {
		this.lm = lm;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getAccept() {
		return accept;
	}

	public void setAccept(String accept) {
		this.accept = accept;
	}

	@Override
	public HashMap<String, String> getHeaders() {
		HashMap<String, String> map = new HashMap<String, String>();
		if (this.lm.getId() != null) {
			map.put("Content-ID", this.lm.getId());
		}

		if (this.text != null) {
			map.put("Text", this.text);
		}

		if (this.accept != null) {
			map.put("Accept", this.accept);
		}

		return map;
	}

	@Override
	public void populate(HashMap<String, String> headers, byte[] content) throws DecodeException {

		for (String header : headers.keySet()) {
			try {
				if ("content-length".equals(header)) {
					setContentLength(Integer.parseInt(headers.get(header)));
					if (content == null && this.getContentLength() > 0) {
						logger.warn("Invalid content-length header: {}. Actual content is null", headers.get(header));
					}
				} else if ("content-type".equals(header)) {
					setContentType(headers.get(header));
					if (content == null && this.getContentType() != null) {
						logger.warn("Invalid content-type header: {}. Actual content is null", headers.get(header));
					}
					if (!(APPLICATION_GRAMMAR_XML.equals(headers.get(header))
							|| APPLICATION_SRGS_XML.equals(headers.get(header))
							|| APPLICATION_SRGS.equals(headers.get(header))
							|| APPLICATION_XML.equals(headers.get(header)) || TEXT_PLAIN.equals(headers.get(header))
							|| TEXT_URI_LIST.equals(headers.get(header)) || TEXT_XML.equals(headers.get(header)))) {

						throw new UnsupportedDataException(headers.get(header),
								"Unsupported content-type: " + headers.get(header));
					}
				} else if ("text".equals(header)) {
					this.text = headers.get(header);
				} else if ("accept".equals(header)) {
					this.accept = headers.get(header);
				} else if ("content-id".equals(header)) {
					this.lm.setId(headers.get(header));
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

			if (getContentType().equals(TEXT_PLAIN) || getContentType().equals(TEXT_URI_LIST)) {
				// modelo ligua definido como lista URI
				ArrayList<String> uriList = new ArrayList<String>();
				String[] uris = new String(content).split("\\r?\\n");
				for (int i = 0; i < uris.length; i++) {
					String str = uris[i].trim();
					if (str.length() > 0)
						uriList.add(str);
				}
				String[] array = new String[uriList.size()];
				array = uriList.toArray(array);
				this.lm.setUri(array);
			} else if (getContentType().equals(TEXT_XML) || getContentType().equals(APPLICATION_GRAMMAR_XML)
					|| getContentType().equals(APPLICATION_SRGS_XML) || getContentType().equals(APPLICATION_SRGS)
					|| getContentType().equals(APPLICATION_XML)) {
				this.lm.setDefinition(new String(content));
			}
		}
	}

	@Override
	public byte[] getContent() {
		if (this.lm.getDefinition() != null) {
			try {
				byte[] data = this.lm.getDefinition().getBytes(ENCODING);
				setContent(data);
				setContentLength(data.length);
				setContentType(APPLICATION_SRGS);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((accept == null) ? 0 : accept.hashCode());
		result = prime * result + ((lm == null) ? 0 : lm.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		InterpretText other = (InterpretText) obj;
		if (accept == null) {
			if (other.accept != null)
				return false;
		} else if (!accept.equals(other.accept))
			return false;
		if (lm == null) {
			if (other.lm != null)
				return false;
		} else if (!lm.equals(other.lm))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

}
