package br.com.cpqd.asr.recognizer.example;

import java.util.HashMap;
import java.util.Map;

public class ProgramArguments {

	private Map<String, String> arguments = new HashMap<>();

	public static ProgramArguments from(String[] args) {

		ProgramArguments pa = new ProgramArguments();

		String name = null;
		for (String arg : args) {
			if (name == null) {
				if (arg.startsWith("--")) {
					name = arg.substring(2);
				} else {
					throw new RuntimeException("Invalid argument: " + arg);
				}
			} else {
				pa.arguments.put(name, arg);
				name = null;
			}
		}
		
		return pa;
	}

	public String getArg(String name) {
		return arguments.get(name);
	}
	
	@Override
	public String toString() {
		return "Arguments[" + arguments.toString() + "]";
	}

}
