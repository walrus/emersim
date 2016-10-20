package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms;

import java.util.Stack;

import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;

/**
 * Tree traverser abstract class which implements generic single-threaded tree traversal, leaving the 
 * actual node computations to the extending class.
 * @author Ben Homer, 2014.
 */
public abstract class TreeTraverser {
	
	/**
	 * Recursively traverses a tree from the specified node in a post-order
	 * fashion, performing computations at each node.
	 * @param node The node the traversal starts from.
	 * @param pv The population vector to use for node computations.
	 */
	public void recursiveTraverse(Node node, PopulationVector pv) {
		if (node.leftChild() != null) {
			recursiveTraverse(node.leftChild(), pv);
		}
		if (node.rightChild() != null) {
			recursiveTraverse(node.rightChild(), pv);
		}

		if (node.isLeaf()) {
			initLeafNode(node, pv);
		} else {
			computeSubnetNode(node, pv);
		}
	}
	
	/**
	 * Iteratively traverses a tree from the specified node in a post-order
	 * fashion, performing computations at each node.
	 * @param node The node the traversal starts from.
	 * @param pv The population vector to use for node computations.
	 */
	public void iterativeTraverse(Node root, PopulationVector pv) {
		Stack<Node> s = new Stack<Node>();
		s.push(root);
		Node prev = null;
		while (!s.empty()) {
			Node curr = s.peek();
			if (prev == null || prev.leftChild() == curr || prev.rightChild() == curr) {
				if (curr.leftChild() != null)
					s.push(curr.leftChild());
				else if (curr.rightChild() != null)
					s.push(curr.rightChild());
		    // If traversing up tree from left, traverse to right child if available.
			} else if (curr.leftChild() == prev && curr.rightChild() != null) {
				s.push(curr.rightChild());
			}
			// Otherwise traversing up tree from right, compute g arrays and pop.
			else {
				if (curr.isLeaf()) {
					initLeafNode(curr, pv);
				} else {
					computeSubnetNode(curr, pv);
				}
				s.pop();
			}
			prev = curr;
		}
	}
	
	/**
	 * Abstract method which an extending class will override to define leaf node computations.
	 * @param node The leaf node.
	 * @param pv The population vector to use for the computation.
	 */
	public abstract void initLeafNode(Node node, PopulationVector pv);
	
	/**
	 * Abstract method which an extending class will override to define subnetwork node computations.
	 * @param node The subnetwork node.
	 * @param pv The population vector to use for the computation.
	 */
	public abstract void computeSubnetNode(Node node, PopulationVector pv);
}