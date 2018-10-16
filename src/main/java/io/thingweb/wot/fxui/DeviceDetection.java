package io.thingweb.wot.fxui;

public class DeviceDetection {
	
	public static boolean isAndroid() {
		String javaRuntimeName = System.getProperty("java.runtime.name");
		return (javaRuntimeName != null && javaRuntimeName.toLowerCase().contains("android"));
	}

}
