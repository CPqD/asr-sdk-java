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

import javax.websocket.DecodeException;

import br.com.cpqd.asr.exception.UnsupportedDataException;

/**
 * Message used to transfer an audio packet to the speech recognition process in
 * the server.
 *
 */
public class SendAudio extends AsrMessage {

	/** Last packet indicator. */
	private boolean lastPacket;

	public SendAudio() {
		setmType(AsrMessageType.SEND_AUDIO);
	}

	public boolean isLastPacket() {
		return lastPacket;
	}

	public void setLastPacket(boolean lastPacket) {
		this.lastPacket = lastPacket;
	}

	@Override
	public String toString() {
		String str = "SendAudio [lastPacket=" + lastPacket;
		if (this.getContent() != null) {
			str += ", content-length=" + getContentLength() + ", content-type=" + getContentType();
		}
		str += "]";
		return str;
	}

	@Override
	public void populate(HashMap<String, String> headers, byte[] content) throws DecodeException {

		for (String header : headers.keySet()) {
			try {
				if ("lastpacket".equals(header)) {
					setLastPacket(Boolean.parseBoolean(headers.get(header)));
				} else if ("content-length".equals(header)) {
					setContentLength(Integer.parseInt(headers.get(header)));
					if (content == null && this.getContentLength() > 0) {
						logger.warn("Invalid content-length header: {}. Content is null", headers.get(header));
					}
				} else if ("content-type".equals(header)) {
					setContentType(headers.get(header));
					if (content == null && this.getContentType() != null) {
						logger.warn("Invalid content-type header: {}. Content is null", headers.get(header));
					}
					if (!(APPLICATION_OCTET_STREAM.equals(headers.get(header))
							|| AUDIO_WAV.equals(headers.get(header)))) {
						// logger.warn("Unsupported content-type: {}",
						// headers.get(header));
						throw new UnsupportedDataException(headers.get(header),
								"Unsupported content-type: " + headers.get(header));
					}
				} else {
					logger.warn("Ignoring header: {} = {}", header, headers.get(header));
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
				setContentLength(content.length);
			}
		}
	}

	@Override
	public HashMap<String, String> getHeaders() {
		HashMap<String, String> map = new HashMap<String, String>();

		map.put("LastPacket", Boolean.toString(this.isLastPacket()));
		return map;
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
		result = prime * result + (lastPacket ? 1231 : 1237);
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
		SendAudio other = (SendAudio) obj;
		if (lastPacket != other.lastPacket)
			return false;
		return true;
	}

}
