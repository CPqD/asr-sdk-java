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
package br.com.cpqd.asr.recognizer.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to read the 'recognizer.properties' configuration file. The
 * file path can be defined as the environment variable
 * '-Dasr.config.file=&lt;filepath&gt;',
 *
 */
public class Config {

	private static Logger logger = LoggerFactory.getLogger(Config.class.getName());

	private static final Properties config = new Properties();

	static {
		try {
			if (System.getenv("asr.config.file") != null) {
				config.load(new FileInputStream(System.getenv("asr.config.file")));
			} else {
				config.load(Config.class.getClassLoader().getResourceAsStream("recognizer.properties"));
			}
		} catch (FileNotFoundException e) {
			logger.error("File asr-client.properties not found: {}", e.getMessage());
		} catch (IOException e) {
			logger.error("Error loading asr-client.properties: {}", e.getMessage());
		}

	}

	public static String getDebugLevel() {
		return config.getProperty("javax.net.debug");
	}

	public static int getIncomingBufferSize() {
		return Integer.parseInt(config.getProperty("incoming.buffer.size"));
	}

	public static String getKeystore() {
		return resolveEnvVars(config.getProperty("keystore.file.path"));
	}

	public static String getTruststore() {
		return resolveEnvVars(config.getProperty("truststore.file.path"));
	}

	public static String getKeystorePasswd() {
		return config.getProperty("keystore.passwd");
	}

	public static String getTruststorePasswd() {
		return config.getProperty("truststore.passwd");
	}

	public static Integer getWorkerThreads() {
		try {
			return Integer.parseInt(config.getProperty("worker.threads"));
		} catch (Exception e) {
			return null;
		}
	}

	public static Integer getSelectorThreads() {
		try {
			return Integer.parseInt(config.getProperty("selector.threads"));
		} catch (Exception e) {
			return null;
		}
	}

	public static Integer getExecutorTimeout() {
		try {
			return Integer.parseInt(config.getProperty("executor.timeout"));
		} catch (Exception e) {
			return null;
		}
	}

	private static String resolveEnvVars(String input) {
		if (null == input) {
			return null;
		}
		// match ${ENV_VAR_NAME} or $ENV_VAR_NAME
		Pattern p = Pattern.compile("\\$\\{(\\w+)\\}|\\$(\\w+)");
		Matcher m = p.matcher(input); // get a matcher object
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String envVarName = null == m.group(1) ? m.group(2) : m.group(1);
			String envVarValue = System.getenv(envVarName);
			m.appendReplacement(sb, null == envVarValue ? "" : Matcher.quoteReplacement(envVarValue));
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
