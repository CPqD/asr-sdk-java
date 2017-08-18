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
package br.com.cpqd.asr.exception;

import javax.websocket.DecodeException;

/**
 * This exception indicates an unsupported Content-Type received in a WebSocket
 * message.
 */
public class UnsupportedDataException extends DecodeException {

	private static final long serialVersionUID = 1655846675921487670L;

	public UnsupportedDataException(String encodedString, String message) {
		super(encodedString, message);
	}

}
