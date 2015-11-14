/**
 * 
 */
package xextension.global;

import xextension.operation.Processor;

/**
 * @author QiaoMingkui
 *
 */
public class IDGenerator {
	private static int	idx	= randomInt(0x10, 0x1000);

	/**
	 * Return a random id consists of 12 or more hex numbers which include following 3 parts:
	 * a 4-digit class name hash string, a 6-digit random number, and a 2-or-more-digit index number
	 * 
	 * @param clazz
	 * @return
	 */
	public static String nextId(Class<? extends Processor> clazz) {
		// class name, 4 digits
		String clazzFlag = Integer.toString(clazz.getName().hashCode(), 16);
		clazzFlag = tailString(clazzFlag, 4, '0');

		// random part, 6 digits
		String random = Integer.toString(randomInt(0x100000, 0x1FFFFF), 16);

		// index part, 2+ digits
		String idxStr = Integer.toString(idx++, 16);

		return clazzFlag + random + idxStr;
	}

	/**
	 * 
	 * @param lower
	 * @param upper
	 *        (exclusive)
	 * @return
	 */
	private static int randomInt(int lower, int upper) {
		int n = lower + (int) (Math.random() * (upper - lower));
		return n;
	}

	private static String tailString(String s, int targetLen, char filling) {
		int sLen = s.length();
		if (sLen == targetLen) return s;
		if (sLen > targetLen) return s.substring(sLen - targetLen);

		StringBuilder sb = new StringBuilder(targetLen);
		while (sLen < targetLen) {
			sb.append(filling);
			sLen++;
		}
		sb.append(s);

		return sb.toString();
	}
}
