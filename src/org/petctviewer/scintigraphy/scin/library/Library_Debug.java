package org.petctviewer.scintigraphy.scin.library;

public class Library_Debug {

	public static void checkNull(String objectName, Object o) {
		System.out.println(objectName + " is " + (o == null ? "NULL" : "ok"));
	}

}
