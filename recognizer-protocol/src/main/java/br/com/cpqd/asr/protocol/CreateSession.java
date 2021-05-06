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

import java.util.HashMap;
import br.com.cpqd.asr.protocol.RecognitionParameters.Header;

/**
 * This message creates a new recognition session in the server.
 * 
 */
public class CreateSession extends AsrMessage {

	/** Client user agent identification (optional). */
	private String userAgent;

	private String loggingTag;

	/** Identifier for sharing resources through different sessions. (speech server optional)*/
	private String channelIdentifier;

	public CreateSession() {
		setmType(AsrMessageType.CREATE_SESSION);
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getLoggingTag() {
		return loggingTag;
	}

	public void setLoggingTag(String loggingTag) {
		this.loggingTag = loggingTag;
	}

	public String getChannelIdentifier() {
		return channelIdentifier;
	}

	public void setChannelIdentifier(String channelIdentifier) {
		this.channelIdentifier = channelIdentifier;
	}

	@Override
	public String toString() {
		return "CreateSession [userAgent=" + userAgent + "]";
	}

	@Override
	public void populate(HashMap<String, String> headers, byte[] content) {

		for (String header : headers.keySet()) {
			try {
				if ("User-Agent".equalsIgnoreCase(header)) {
					this.userAgent = headers.get(header);
				} else if ("Channel-Identifier".equalsIgnoreCase(header)) {
					this.channelIdentifier = headers.get(header);
				} else if (Header.loggingTag.getHeader().equalsIgnoreCase(header)) {
					this.loggingTag = headers.get(header);
				} else {
					logger.warn("Ignoring header: {} = {}", header, headers.get(header));
				}
			} catch (Exception e) {
				logger.error("Error parsing header [{} = {}] : {}", header, headers.get(header), e.getMessage());
			}
		}
	}

	@Override
	public HashMap<String, String> getHeaders() {
		HashMap<String, String> map = new HashMap<>();
		if (this.userAgent != null) {
			map.put("User-Agent", this.userAgent);
		}
		if (this.loggingTag != null) {
			map.put(Header.loggingTag.getHeader(), this.loggingTag);
		}
		if (this.channelIdentifier != null) {
			map.put("Channel-Identifier", this.channelIdentifier);
		}
		return map;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result;
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
		return true;
	}

}
