package br.com.cpqd.asr.recognizer.example;

import java.util.Properties;

public class ProgramArguments {

	public static Properties parseFrom(String[] args) {

		Properties arguments = new Properties();

		String name = null;
		for (String arg : args) {
			if (name == null) {
				if (arg.startsWith("--")) {
					name = arg.substring(2);
				} else {
					throw new RuntimeException("Invalid argument: " + arg);
				}
			} else {
				arguments.put(name, arg);
				name = null;
			}
		}
		
		return arguments;
	}

}
