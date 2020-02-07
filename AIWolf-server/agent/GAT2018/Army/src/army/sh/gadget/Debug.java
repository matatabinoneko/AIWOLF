package army.sh.gadget;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Debug {
	private static boolean isDebug = false;
	
	private static List<String> passError = new ArrayList<>();
	
	
	public static void stackError(StringWriter sw) {
		passError.add(sw.toString());
	}
}
