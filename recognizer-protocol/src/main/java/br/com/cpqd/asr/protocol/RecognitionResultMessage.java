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

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * This message contains the recognition result.
 * 
 */
public class RecognitionResultMessage extends AsrMessage {

	private static Logger logger = LoggerFactory.getLogger(RecognitionResultMessage.class.getName());

	private static final ObjectMapper jsonMapper = new ObjectMapper()
			.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private static final ObjectMapper xmlMapper = new XmlMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private RecognitionStatus recognitionStatus;

	private SessionStatus sessionStatus;

	private RecognitionResult result;

	public RecognitionResultMessage() {
		setmType(AsrMessageType.RECOGNITION_RESULT);
	}

	public RecognitionStatus getRecognitionStatus() {
		return recognitionStatus;
	}

	public void setRecognitionStatus(RecognitionStatus status) {
		this.recognitionStatus = status;
	}

	public RecognitionResult getRecognitionResult() {
		return this.result;
	}

	public void setRecognitionResult(RecognitionResult result) {
		this.result = result;
	}

	public void setSessionStatus(SessionStatus sessionStatus) {
		this.sessionStatus = sessionStatus;
	}

	public SessionStatus getSessionStatus() {
		return sessionStatus;
	}

	@Override
	public String toString() {
		return "RecognitionResult [result-status=" + recognitionStatus + ", session-status=" + sessionStatus
				+ ", result=" + result + ", handle=" + getHandle() + "]";
	}

	@Override
	public void populate(HashMap<String, String> headers, byte[] content) {
		// define o conteudo raw
		setContent(content);

		for (String header : headers.keySet()) {
			try {
				if ("Handle".toLowerCase().equals(header)) {
					setHandle(Long.parseLong(headers.get(header)));
				} else if ("Result-Status".toLowerCase().equals(header)) {
					setRecognitionStatus(RecognitionStatus.valueOf(headers.get(header)));
				} else if ("Session-Status".toLowerCase().equals(header)) {
					setSessionStatus(SessionStatus.valueOf(headers.get(header)));
				} else if ("Content-Length".toLowerCase().equals(header)) {
					setContentLength(Integer.parseInt(headers.get(header)));
					if (content == null && this.getContentLength() > 0) {
						logger.warn("Invalid content-length header: {}. Content is null", headers.get(header));
					}
				} else if ("Content-Type".toLowerCase().equals(header)) {
					String contentType = headers.get(header);
					setContentType(contentType);
					if (content == null) {
						logger.warn("Invalid content-type header: {}. Content is null", contentType);
					}

					// parse the content to RecognitionResult object
					try {
						if (APPLICATION_XML.equals(contentType)) {
							this.result = xmlMapper.readValue(content, RecognitionResult.class);
						} else if (APPLICATION_JSON.equals(contentType)) {
							this.result = jsonMapper.readValue(content, RecognitionResult.class);
						} else {
							logger.warn("Unsupported content-type: {}", contentType);
						}
					} catch (IOException e) {
						logger.error("Invalid content: \n" + new String(content));
						logger.error("Error parsing recognition result", e);
					}

				}
			} catch (Exception e) {
				logger.error("Error parsing header [{} = {}] : {}", header, headers.get(header), e.getMessage());
			}
		}
	}

	@Override
	public byte[] getContent() {

		// Content was not created yet
		if (super.getContent() == null) {

			try {
				String resultBody = null;
				if (APPLICATION_XML.equals(super.getContentType())) {
					resultBody = xmlMapper.writeValueAsString(this.result);
					setContentType(APPLICATION_XML);
				} else {
					resultBody = jsonMapper.writeValueAsString(this.result);
					setContentType(APPLICATION_JSON);
				}
				byte[] data = resultBody.getBytes(ENCODING);
				setContent(data);
				setContentLength(data.length);

			} catch (IOException e) {
				logger.error("Error serializing Recognition Result", e);
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
	public HashMap<String, String> getHeaders() {
		HashMap<String, String> map = new LinkedHashMap<String, String>();

		map.put("Handle", Long.toString(this.getHandle()));

		if (this.getRecognitionStatus() != null) {
			map.put("Result-Status", this.getRecognitionStatus().name());
		}

		if (this.getSessionStatus() != null) {
			map.put("Session-Status", this.getSessionStatus().name());
		}

		return map;
	}

	public boolean isFinalResult() {
		return result != null && result.isFinalResult();
	}

}
