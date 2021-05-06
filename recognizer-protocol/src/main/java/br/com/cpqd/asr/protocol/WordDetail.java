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

/**
 * The word alignment detail level. 
 *
 */
public enum WordDetail {

	ALL(2), FIRST(1), NONE(0);

	private final int value;

	WordDetail(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static WordDetail from(String str) {
		if (str == null) {
			return null;
		} else if ("2".equalsIgnoreCase(str)) {
			return ALL;
		} else if ("1".equalsIgnoreCase(str)) {
			return FIRST;
		} else if ("0".equalsIgnoreCase(str)) {
			return NONE;
		} else {
			return null;
		}
	}
}
