/**
 * 
 */
package qmk;

import java.util.Scanner;

/**
 * 有任意种水果，每种水果个数也是任意的，两人轮流从中取出水果，规则如下：
 *   1）每一次应取走至少一个水果；每一次只能取走一种水果的一个或者全部
 *   2）如果谁取到最后一个水果就胜
 * 给定水果种类N和每种水果的个数M1，M2，…Mn，算出谁取胜，编程实现之。
 * （题目的隐含条件是两个人足够聪明，聪明到为了取胜尽可能利用规则）
 * 
 * @author mkqiao
 * 
 */
public class WouldIWinTheGame {
	private static final String EXIT_PROGRAM = "q";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);
		String input;
		int[] fruitNums;

		do {
			System.out.println("Input number of fruits (seperated by space)");
			System.out.println("Input q or Q to quit.");

			if (EXIT_PROGRAM.equalsIgnoreCase(input = scanner.nextLine())) {
				System.out.println("Program exits, bye");
				break;
			}

			input = input.trim();
			if (input.length() != 0) {
				fruitNums = initFruitNums(input);
				System.out.println(wouldWinTheGame(fruitNums, fruitNums.length));
			}
		} while (true);
	}

	private static int[] initFruitNums(String input) {
		String[] nums = input.split("\\s+");
		int[] fruitNums = new int[nums.length];
		int num;
		for (int i = 0; i < nums.length; i++) {
			num = Integer.parseInt(nums[i]);
			if (num <= 0) {
				throw new IllegalArgumentException("水果数量不能为0或负数：" + num);
			}

			fruitNums[i] = num;
		}

		return fruitNums;
	}

	/**
	 * 递归法
	 * 
	 * @param fruitNums
	 * @param numOfTypes
	 * @return
	 */
	private static boolean wouldWinTheGame(int[] fruitNums, int numOfTypes) {
		// only 1 fruits, we would win with no doubt
		if (numOfTypes == 1) {
			return true;
		}

		// only 2 fruits, we would win if the sum of their quantities is ODD
		if (numOfTypes == 2) {
			return sumOfTwoFruitNums(fruitNums) % 2 == 1;
		}

		// more than 3 fruits
		int num;
		for (int i = 0; i < fruitNums.length; i++) {
			num = fruitNums[i];
			if (num == 0) continue;

			// first, try to take all the fruit
			fruitNums[i] = 0;
			if (!wouldWinTheGame(fruitNums, numOfTypes - 1)) {
				fruitNums[i] = num;
				return true;
			}
			// take only one
			if (num > 1) {
				fruitNums[i] = num - 1;
				if (!wouldWinTheGame(fruitNums, numOfTypes)) {
					fruitNums[i] = num;
					return true;
				}
			}

			// if still can not win, restore the number of this fruit and continue to try
			fruitNums[i] = num;
		}

		// still can not win? we lose
		return false;
	}

	/**
	 * 直接计算法。该算法基于以下结论：
	 * 
	 * 当水果种类为奇数种时，我方必胜。当水果种类为偶数种时，如果水果总数为奇数则我方胜，否则我方败。
	 * 
	 * 证明：
	 * 
	 * m=1 必胜
	 * 
	 * m=2 因为任何人都不敢率先拿完一种水果，所以双方都只能一个一个地拿。所以总数为奇数胜
	 * 
	 * m=3 必胜。因为：
	 *     把水果按数量的奇偶分类，只有4种可能：
	 *     1) 3种都是奇数个
	 *     2) 3种都是偶数个
	 *     3) 2种奇数个1种偶数个
	 *     4) 2种偶数个1种奇数个
	 *     无论是哪种情况，我们都可以立即让对方进入与2相反的局面（必败的局面）：
	 *     a) 情况1、2，随便拿掉一种水果即可
	 *     b) 情况3、4，拿掉单独的那种，留下同为奇数或同为偶数的2种
	 *     所以，m=3时，我方必胜
	 * 
	 * m=4 谁都不敢率先拿完一种水果，所以双方都只能一个一个地拿，也就是说，“总数-4”必须是奇数，这样才会让对方进入最终的必败局面，所以总数为奇数胜
	 * 
	 * 假设k>=3且k为奇数，且m=k和m=k+1时有上面的结论成立。
	 * 
	 * 5.1 当m=k+2时（此时m为奇数），把水果按数量的奇偶分类，只有3种情况：
	 *     1) 都为奇数或都为偶数。此时只需随便拿掉1种，就会让对方剩下的水果总数量为偶数个
	 *     2) 奇数种奇数的水果、偶数种偶数的水果。此时只要拿掉一种数量为奇数的水果即可
	 *     3) 偶数种奇数的水果、奇数种偶数的水果。此时只要拿掉一种数量为偶数的水果即可
	 * 所以，当m为>3的奇数时，上述结论成立。（实际上情况1)为情况2)和3)的特例）
	 * 
	 * 5.2 当m=k+3时（此时m为偶数）
	 * 由于m=k+2时是必胜的，所以这种情况下谁都不敢率先拿掉一种水果，所以双方都只能一个一个地拿。
	 * 也就是说，“总数-m”必须是奇数，这样才能让对方进入这种局面。由于m是偶数，所以总数也必然是奇数。
	 * 
	 * 至此，用归纳法证明了上面的结论是成立的。（实际上3和4两种情况可以不要，直接由1、2来归纳出最终的结论。但加上3和4会更清晰一些）
	 * 
	 * @param fruitNums
	 * @return
	 */
	private static boolean wouldWinTheGame2(int[] fruitNums) {
		// the quantity of fruit types is odd
		if (fruitNums.length % 2 == 1) {
			return true;
		}

		// the quantity is even, check if the total quantity of fruit is odd
		return sumOfFruitNums(fruitNums) % 2 == 1;
	}

	private static int sumOfTwoFruitNums(int[] fruitNums) {
		int num1 = 0;
		int num2 = 0;

		// find two fruit quantities
		for (int num : fruitNums) {
			if (num > 0) {
				if (num1 == 0) {
					num1 = num;
				} else {
					num2 = num;
					break;
				}
			}
		}

		return num1 + num2;
	}
	
	private static int sumOfFruitNums(int[] fruitNums) {
		int sum = 0;

		for (int num : fruitNums) {
			sum += num;
		}
		
		return sum;
	}
}




