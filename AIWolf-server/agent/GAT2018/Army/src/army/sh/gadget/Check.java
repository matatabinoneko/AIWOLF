package army.sh.gadget;

public class Check {
	// 引数がnullならtrue, 違うならfalse
	public static boolean isNull(Object obj) {
		return obj == null ? true : false;
	}

	// 引数がnullではないならtrue, 違うならfalse
	public static boolean isNotNull(Object obj) {
		return obj != null ? true : false;
	}

	// // test
	// public static boolean test(Object obj) {
	// return obj == null ? true : false;
	// }

}
