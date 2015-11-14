/**
 * 
 */
package qmk;

/**
 * @author mkqiao
 * 
 */
public class MontyHallProblem {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		double ratio1 = montyHall();
		double ratio2 = montyHall2();
		double ratio3 = montyHall3();
		double ratio4 = montyHall4();

		System.out.println("以下是基于三种不同的前提，并且都选择改变答案的结果。");
		System.out.println("主持人帮忙排除一个山羊：" + ratio1 + "%");
		System.out.println("主持人随机做出选择，且选择的是山羊：" + ratio2 + "%");
		System.out.println("主持人随机做出选择，然后告诉你他选择的是什么：" + ratio3 + "%");
		System.out.println("主持人随机做出选择，但并不告诉你他选择的是什么：" + ratio4 + "%");
	}

	/*
	 * 主持人帮忙排除一个不是汽车的选项
	 */
	private static double montyHall() {
		int totalTimes = 1000000;
		int bingo = 0;
		boolean[] boxes;
		for (int i = 0; i < totalTimes; i++) { // loop 1 million times
			boxes = new boolean[] {false, false, false};
			boxes[random(3)] = true;

			int myChoice = random(3);
			int hostChoice = openNoCarOne(boxes, myChoice);

			if (!boxes[myChoice]) {
				bingo++;
			}
		}
		return (double) bingo * 100 / totalTimes;
	}

	/*
	 * 主持人事先并不知道答案，随机排除一个，并且碰巧这是一只山羊。
	 */
	private static double montyHall2() {
		int totalTimes = 1000000;
		int bingo = 0;
		boolean[] boxes;
		int actuallyTotalTimes = 0;
		for (int i = 0; i < totalTimes; i++) { // loop 1 million times
			boxes = new boolean[] {false, false, false};
			boxes[random(3)] = true;

			int myChoice = random(3);
			int hostChoice = random(3, myChoice);
			if (boxes[hostChoice]) {
				continue;
			}

			actuallyTotalTimes++;
			if (!boxes[myChoice]) {
				bingo++;
			}
		}
		return (double) bingo * 100 / actuallyTotalTimes;
	}

	/*
	 * 主持人事先并不知道答案，随机排除一个，并且告诉你这一个是什么。
	 */
	private static double montyHall3() {
		int totalTimes = 1000000;
		int bingo = 0;
		boolean[] boxes;
		for (int i = 0; i < totalTimes; i++) { // loop 1 million times
			boxes = new boolean[] {false, false, false};
			boxes[random(3)] = true;

			int myChoice = random(3);
			int hostChoice = random(3, myChoice);

			if (!boxes[myChoice]&& !boxes[hostChoice]) {
				bingo++;
			}
		}
		return (double) bingo * 100 / totalTimes;
	}

	/*
	 * 主持人随机排除一个选项，但不告诉你是不是汽车。
	 */
	private static double montyHall4() {
		int totalTimes = 1000000;
		int bingo = 0;
		boolean[] boxes;
		for (int i = 0; i < totalTimes; i++) { // loop 1 million times
			boxes = new boolean[] {false, false, false};
			boxes[random(3)] = true;

			int myChoice = random(3);
			int hostChoice = random(3, myChoice);

			if (!boxes[myChoice] && !boxes[hostChoice]) {
				bingo++;
			}
		}
		return (double) bingo * 100 / totalTimes;
	}

	private static int openNoCarOne(boolean[] boxes, int myChoice) {
		for (int i = 0; i < boxes.length; i++) {
			if (!boxes[i] && i != myChoice) return i;
		}

		return -1;
	}

	private static int random(int i) {
		return (int) (Math.random() * i);
	}

	private static int random(int i, int exclusion) {
		int randomOne = -1;
		while ((randomOne = random(i)) == exclusion) {
		}

		return randomOne;
	}

	private static void testRandom() {
		int[] results = new int[10];
		for (int i = 0; i < 1000000; i++) {
			results[random(10)]++;
		}

		for (int i = 0; i < 10; i++) {
			System.out.println(results[i]);
		}
	}

	private static void testRandom2() {
		int exclusion;
		int randomOne;
		boolean allPass = true;
		for (int i = 0; i < 1000000; i++) {
			exclusion = random(10);
			randomOne = random(10, exclusion);
			if (randomOne == exclusion || randomOne >= 10 || randomOne < 0) {
				System.out.println("Error " + i + " " + randomOne);
				allPass = false;
			}
		}
		if (allPass) {
			System.out.println("all pass");
		}
	}

	private static void testOpenNoCarOne() {
		boolean[] boxes;
		int myChoice;
		int hostChoice;
		boolean allPass = true;
		for (int i = 0; i < 10000; i++) {
			boxes = new boolean[] {false, false, false};
			boxes[random(3)] = true;
			myChoice = random(3);
			hostChoice = openNoCarOne(boxes, myChoice);
			if (hostChoice == -1 || boxes[hostChoice] || hostChoice == myChoice) {
				System.out.println("Error " + i + " " + myChoice + " " + hostChoice);
				allPass = false;
			}
		}
		if (allPass) {
			System.out.println("all pass");
		}
	}
}
