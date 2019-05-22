package org.petctviewer.scintigraphy.scin.library;

public class Library_Debug {

	public static void checkNull(String objectName, Object o) {
		System.out.println(objectName + " is " + (o == null ? "NULL" : "ok"));
	}

	public static final String[] PATTERNS_SEPARATOR = { ".oOo" };

	public static String separator(int id, int width, int height) {
		int patternId = 0;
		switch (id) {
		default:
			patternId = 0;
		}

		String pattern = PATTERNS_SEPARATOR[patternId];
		StringBuilder result = new StringBuilder();

		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w += pattern.length()) {
				result.append(pattern);
			}
			if (h != height - 1)
				result.append('\n');
		}

		return result.toString();
	}

	public static String separator(int id) {
		switch (id) {
		default:
			return separator(id, 60, 1);
		}
	}

	public static String title(String title) {
		return "×º°”˜`”°º×  [" + title + "]  ×º°”˜`”°º×";
	}

	public static String subtitle(String subtitle) {
		return "-=> " + subtitle + " <=-";
	}

}
