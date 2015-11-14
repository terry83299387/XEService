/**
 * 
 */
package qmk;

import java.util.Scanner;

/**
 * 根据输入初始化一颗二叉树，每个节点的值为一个整数。输入完成后，在二叉树上找到一条由根节点到叶子节点的路径，该路径上所有节点的和最小，然后输出这个最小值。
 * 每一行中的节点值用空格分隔，输入0结束输入，空节点用null表示。例如：
 * 
 * 输入：
 * 5
 * 2 3
 * 0
 * 
 * 输出：
 * 7
 * 
 * 输入：
 * 1
 * 2 18
 * 3 5 null 2
 * 100 1 null 8 null null
 * 0
 * 
 * 输出（1+2+3+1）：
 * 
 * 7
 * 
 * @author mkqiao
 * 
 */
public class BinaryTreeMinSum {
	private static final String INPUT_DONE = "0";
	private static final String EXIT_PROGRAM = "q";
	private static final String NULL_VALUE = "null";

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);
		String input;
		BNode root;
		BNode[] lastLevelNodes;

		while (true) {
			System.out.println("Input node values of binary tree (seperate by spaces), input 0 while done.");
			System.out.println("Input q or Q to quit.");

			if (EXIT_PROGRAM.equalsIgnoreCase(input = scanner.nextLine())) {
				System.out.println("Program exits, bye");
				break;
			}

			// initialize binary tree
			root = null;
			lastLevelNodes = null;
			while (!INPUT_DONE.equals(input)) {
				input = input.trim();

				if (root == null) { // root node
					root = BNode.create(input);
					lastLevelNodes = new BNode[] {root};
				} else { // other nodes
					lastLevelNodes = initNextLevelNodes(lastLevelNodes, input);
				}

				input = scanner.nextLine();
			}

			// calculate and print min sum of the tree
			System.out.println(calcMinSum(root));
		}
	}

	private static BNode[] initNextLevelNodes(BNode[] lastLevelNodes, String input) {
		BNode[] currentLevelNodes = new BNode[lastLevelNodes.length * 2];

		String[] values = input.split("\\s+");
		int i = 0;
		for (BNode node : lastLevelNodes) {
			if (node != null) {
				node.left  = BNode.create(values[i]);
				currentLevelNodes[i++] = node.left;
				node.right = BNode.create(values[i]);
				currentLevelNodes[i++] = node.right;
			}
		}

		return currentLevelNodes;
	}

	private static int calcMinSum(BNode node) {
		if (node == null) {
			return 0;
		}

		int leftSum = calcMinSum(node.left);
		int rightSum = calcMinSum(node.right);
		return node.value + (leftSum < rightSum ? leftSum : rightSum);
	}

	// node of binary tree
	private static class BNode {
		BNode left;
		BNode right;
		int value;

		BNode(int v) {
			this.value = v;
		}

		public static BNode create(String v) {
			if (NULL_VALUE.equals(v)) return null;
			return new BNode(Integer.parseInt(v));
		}
	}
}
