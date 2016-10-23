package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Class for printing trees to console.
 * @author Ben Homer, 2014.
 */
public class TreePrinter {

	/** Prints the specified node and then its children recursively. 
	 * @param root The root node to print from.
	 */
	public <T extends Comparable<?>> void printNode(Node root) {
		System.out.println();
		int maxLevel = this.maxLevel(root);
		printNode(Collections.singletonList(root), 1, maxLevel);
	}

	/** Prints the specified list of nodes.
	 * @param nodes The nodes to print.
	 * @param level The level these nodes are at.
	 * @param maxLevel The maximum level in the tree.
	 */
	private <T extends Comparable<?>> void printNode(List<Node> nodes, int level, int maxLevel) {
		if (nodes.isEmpty() || allElementsAreNull(nodes))
			return;

		int floor = maxLevel - level;
		int endLines = (int) Math.pow(2, (Math.max(floor - 1, 0)));
		int firstSpaces = (int) Math.pow(2, (floor)) - 1;
		int betweenSpaces = (int) Math.pow(2, (floor + 1)) - 1;

		this.printSpaces(firstSpaces);

		List<Node> newNodes = new ArrayList<Node>();
		for (Node node : nodes) {
			if (node != null) {
				node.print();
				newNodes.add(node.leftChild());
				newNodes.add(node.rightChild());
			} else {
				newNodes.add(null);
				newNodes.add(null);
				Printer.out.print(" ");
			}

			this.printSpaces(betweenSpaces);
		}
		Printer.out.println("");

		for (int i = 1; i <= endLines; i++) {
			for (int j = 0; j < nodes.size(); j++) {
				this.printSpaces(firstSpaces - i);
				if (nodes.get(j) == null) {
					this.printSpaces(endLines*2 + i + 1);
					continue;
				}

				if (nodes.get(j).leftChild() == null)
					this.printSpaces(1);

				this.printSpaces(i + i - 1);

				if (nodes.get(j).rightChild() == null)
					this.printSpaces(1);

				this.printSpaces(endLines*2 - i);
			}

			Printer.out.println("");
		}

		printNode(newNodes, level + 1, maxLevel);
	}

	/** Prints the specified number of spaces.
	 * @param count The number of spaces to print.
	 */
	private void printSpaces(int count) {
		for (int i = 0; i < count; i++)
			Printer.out.print(" ");
	}

	/** Gets the maximum level of the specified node.
	 * @param node The node to get the max level for.
	 * @return The maximum level of the node.
	 */
	private <T extends Comparable<?>> int maxLevel(Node node) {
		if (node == null) {
			return 0;
		}

		return Math.max(this.maxLevel(node.leftChild()), this.maxLevel(node.rightChild())) + 1;
	}
	
	/** Returns true if all nodes are null.
	 * @param list The node list to check.
	 * @return A boolean indicating whether the node's in the node list are null.
	 */
	private boolean allElementsAreNull(List<Node> list) {
		for (Object object : list) {
			if (object != null)
				return false;
 		}
 
		return true;
	}
}