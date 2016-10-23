package jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;
import jmt.engine.random.engine.RandomEngine;

import jmt.jmva.analytical.solvers.DataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.DataStructures.QNModel;
import jmt.jmva.analytical.solvers.QueuingNet.TreeAlgorithms.ClassCoverageUtils.Covered;
import jmt.jmva.analytical.solvers.Utilities.Timer;

/**
 * Class responsible for the tree planting phase of the Tree Convolution algorithm.
 * @author Ben Homer, 2014.
 */
public class TreePlanter {

	/** Enum for different tree planting approaches. */
	public enum PlantMode { HEURISTIC, SIMPLE, SEQUENTIAL };
	
	/** The timer for the tree planting phase. */
	public Timer treePlantTimer = new Timer();
	
	/** The planted tree's root node. */
	private Node treeRoot;
	
	/** The plant mode used to plant the current tree. */
	private PlantMode plantModeUsed;
	
	/** The number of stations which require planting at leaf nodes. */
	private int stations;
	
	/** The population vector of the network. */
	private PopulationVector N;
	
	/** The class coverage utilities helper. */
	private ClassCoverageUtils ccu;
	
	/** The tree printer. */
	private TreePrinter treePrinter;
	
	/** The complexity evaluator. */
	private ComplexityEvaluator ce;
        
        private RandomEngine engine = RandomEngine.makeDefault();

	/** Constructor.
	 * @param qnm The queueing network model to solve.
	 * @param ccu The class coverage utilities helper.
	 * @param ce The complexity evaluator.
	 * @param treePrinter The tree printer.
	 */
	public TreePlanter(QNModel qnm, ClassCoverageUtils ccu, ComplexityEvaluator ce, TreePrinter treePrinter) {
		this.ccu = ccu;
		this.ce = ce;
		this.treePrinter = treePrinter;
		this.stations = qnm.M;
		this.N = qnm.N;
	}
	
	/**
	 * Gets the planted tree's root node.
	 * @return The root node.
	 */
	public Node treeRoot() {
		return treeRoot;
	}
	
	/**
	 * Gets the plant mode used to plant the current tree.
	 * @return The plant mode as a string.
	 */
	public String getPlantModeUsed() {
		return plantModeUsed.toString();
	}
	
	/**
	 * Creates and returns a clone of the planted tree's root node.
	 * @return The cloned root node.
	 */
	public Node rootClone() {
		return treeRoot.clone();
	}
	
	/**
	 * Prints the current tree.
	 */
	public void printCurrentTree() {
		this.treePrinter.printNode(treeRoot);
	}
	
	/** Prints a tree from specific root node.
	 * @param root The tree root to print from.
	 */
	public void printTree(Node root) {
		this.treePrinter.printNode(root);
	}

	/**
	 * Comparator for comparing two nodes based on their relative weights.
	 */
	private class WeightComparator implements Comparator<Node> {
		@Override
		public int compare(Node o1, Node o2) {
			o1.recalculateWeight();
			o2.recalculateWeight();
			if (o1.weight == o2.weight) return 0;
			return o1.weight > o2.weight ? -1 : 1;
		}
	}

	/**
	 * Runs the main tree planting phase.
	 * Consists of two steps:
	 * 1. Plant the tree.
	 * 2. Check this tree has acceptable complexity characteristics, if not go back to step 1.
	 * @return A boolean indicating whether an adequate tree was found.
	 */
	public boolean runTreePlantingPhase() {
		Printer.out.println("Running tree planting phase.");
		treePlantTimer.start();
		boolean suitableTreeFound = false;
		int i = 0;
		while (!suitableTreeFound) {
			if (i >= Config.MAX_TREE_PLANT_ATTEMPTS) {
				treePlantTimer.pause();
				return false;
			}
			
			if (Config.COMPARE_ALL_HEURISTICS) {
				Node[] trees = new Node[2];
				Printer.out.println("HEURISTIC");
				trees[0] = plantTreeHeuristicApproach();
				this.treePrinter.printNode(trees[0]);
				Printer.out.println("SIMPLE");
				trees[1] = plantSimpleTree();
				this.treePrinter.printNode(trees[1]);
				/*Printer.out.println("SEQUENTIAL");
				trees[2] = plantSequentialTree();
				this.treePrinter.printNode(trees[2]);*/
				int treeIdx = ce.getTreeWithBestComplexity(trees, N);
				treeRoot = trees[treeIdx];
				plantModeUsed = PlantMode.values()[treeIdx];
			} else {
				switch (Config.TREE_PLANT_MODE) {
					case HEURISTIC:				
						treeRoot = plantTreeHeuristicApproach();
						break;
					case SIMPLE:
						treeRoot = plantSimpleTree();
						break;
					case SEQUENTIAL:
						treeRoot = plantSequentialTree();
						break;
				}
				plantModeUsed = Config.TREE_PLANT_MODE;
			}
			
			suitableTreeFound = ce.treeHasAcceptableComplexity(treeRoot, N);
			i++;
		}
		
		Printer.out.println("\nFinal tree after tree planting:");
		this.treePrinter.printNode(treeRoot);
		treePlantTimer.pause();
		return true;
	}
	
	/**
	 * Runs the heuristic tree planting approach, as outlined in the LamLien83 paper.
	 */
	private Node plantTreeHeuristicApproach() {
		int levels = (int) Math.ceil((Math.log(stations)/Math.log(2))) + 1;
		int stationsAtLowestLevel = 2*(stations - (int)Math.pow(2, Math.floor((Math.log(stations)/Math.log(2)))));
		boolean unbalancedLeafPlantingStage = stationsAtLowestLevel != stations;

		// Initially each subnet contains a single station.
		ArrayList<Node> currentNodes = new ArrayList<Node>();
		ArrayList<Node> levelAbove = new ArrayList<Node>();
		for (int k = 0; k < stations; k++) {
			Node node = new Node();
			node.stations.add(k);
			node.recalculateCoveredClasses();
			node.recalculateWeight();
			levelAbove.add(node);
		}

		for (int lvl = levels-1; lvl > 0; lvl--) {
			// Copy nodes at next level into current nodes, and clear nodes at next level.
			currentNodes.clear();
			currentNodes.addAll(levelAbove);
			levelAbove.clear();

			// Sort subnets by weight in decreasing order.
			Collections.sort(currentNodes, new WeightComparator());

			// Mark all subnets.
			for (Node s : currentNodes) {
				s.marked = true;
			}

			boolean someSubnetsMarked = true;
			int mergeCount = 0;
			while (someSubnetsMarked) {
				Node candidate1 = null; 
				Node candidate2 = null;
				int minCost = Integer.MAX_VALUE;
				int markedCount = 0;
				for (Node s : currentNodes) {
					if (s.marked) {
						markedCount++;
						// 1st candidate = heaviest marked.
						if (candidate1 == null) { // heaviest marked
							candidate1 = s;
							continue;
						}

						// 2nd candidate = one of remaining marked subnets s.t. cost(1st, 2nd) is minimized.
						int cost = cost(candidate1, s);
						if (cost < minCost) {
							minCost = cost;
							candidate2 = s;
						}
						// Ties broken first by weight, second by random selection.
						else if (cost == minCost) {
							if (s.weight > candidate2.weight) {
								candidate2 = s;
							}
							else if (candidate2.weight == s.weight) {
                                                                // Replaced Math.random
								candidate2 = (engine.raw2() < 0.5) ? candidate2 : s;
							}
						}
					}
				}

				// Unmark the 2 candidates and merge them into unmarked subnet at next level.
				candidate1.marked = false;
				candidate2.marked = false;
				Node merged = candidate1.merge(candidate2);
				Printer.out.print("\nmerge: " + Arrays.toString(candidate1.stations.toArray()) + " + " +
						Arrays.toString(candidate2.stations.toArray()) + " = " + Arrays.toString(merged.stations.toArray()));
				candidate1.setParent(merged);
				candidate2.setParent(merged);
				merged.setLeftChild(candidate1);
				merged.setRightChild(candidate2);
				merged.recalculateCoveredClasses();
				merged.recalculateWeight();
				levelAbove.add(merged);
				mergeCount += 2;
				
				if (unbalancedLeafPlantingStage && mergeCount == stationsAtLowestLevel) {
					for (Node s : currentNodes) {
						if (s.marked) {
							levelAbove.add(s);
						}
					}
					unbalancedLeafPlantingStage = false;
					someSubnetsMarked = false;
				} else {
					// If any of subnets are still marked continue looping.
					someSubnetsMarked = markedCount > 2;
				}
			}
		}

		if (levelAbove.size() != 1) {
			Printer.out.println("Error in tree planting phase.");
		}

		return levelAbove.get(0);
	}

	/** Determines the cost of merging two nodes, using the approach outlined in the LamLien83 paper.
	 * Smaller costs are better.
	 * @param node1 The first candidate node.
	 * @param node2 The second candidate node.
	 * @return The cost of merging the nodes.
	 */
	private int cost(Node node1, Node node2) {
		TreeSet<Integer> subnet1 = new TreeSet<Integer>(node1.stations);
		TreeSet<Integer> subnet2 =  new TreeSet<Integer>(node2.stations);
		int cost = 0;
		TreeSet<Integer> S1unionS2 = new TreeSet<Integer>(subnet1);
		S1unionS2.addAll(subnet2);

		for (int c : node2.pcs) {
			Covered coverS1 = ccu.getClassCoverage(c, subnet1);
			
			if (coverS1 == Covered.NON) {
				cost++;
				continue;
			}
			
			Covered coverS1unionS2 = ccu.getClassCoverage(c, S1unionS2);
			if (coverS1 == Covered.PARTIALLY && coverS1unionS2 != Covered.FULLY) cost--;
			else if (coverS1 == Covered.PARTIALLY && coverS1unionS2 == Covered.FULLY) cost -= 2;
		}

		return cost;
	}
	
	/**
	 * Creates a copy of the current tree with station m cloned as an additional leaf node.
	 * In the Lam/Lien paper this is referred to as the Gm+ tree.
	 * @param m The station/queue to clone in the tree.
	 * @return The parent node of the two cloned nodes.
	 */
	public Node createTreeWithClonedStation(int m) {
		Node rootCopy = treeRoot.clone();
		Node leafNode = findLeafNodeForStation(rootCopy, m);
		Node clone = leafNode.clone();

		// If a class was previously fully covered by the station we are cloning, then the cloning act
		// causes that class to become only partially covered by each clone.
		
		// First mark the node so we know if its g-array needs to be recomputed.
		if (clone.fcs.size() != 0) {
			clone.marked = true;
		}
		clone.pcs.addAll(clone.fcs);
		clone.fcs.clear();

		leafNode.setLeftChild(clone);
		leafNode.setRightChild(clone);
		treePrinter.printNode(rootCopy);
		return leafNode;
	}
	
	/**
	 * Creates a copy of the current tree with the node for station m removed.
	 * In the Lam/Lien paper this is referred to as the Gm- tree.
	 * @param m The station/queue to remove from the tree.
	 * @return The parent node of the removed node.
	 */
	public Node createTreeWithRemovedStation(int m) {
		Node rootCopy = treeRoot.clone();
		Node leafNode = findLeafNodeForStation(rootCopy, m);
		Node leafParent = leafNode.parent();
		Node parent = leafParent.parent();
		Node otherLeaf = null;
		
		if (leafParent.leftChild().stations.contains(m)) {
			leafParent.setLeftChild(null);
			otherLeaf = leafParent.rightChild();
		} else if (leafParent.rightChild().stations.contains(m)) {
			leafParent.setRightChild(null);
			otherLeaf = leafParent.leftChild();
		}
		
		if (parent.leftChild().stations.contains(m)) {
			parent.setLeftChild(otherLeaf);
		} else if (parent.rightChild().stations.contains(m)) {
			parent.setRightChild(otherLeaf);
		}
		
		// Since station m is deleted, the chains partially covered
		// by m remain partially covered at the root.
		// TODO: recurse up tree and add pcs to node.pcs.

		treePrinter.printNode(rootCopy);
		return parent;
	}
	
	/** 
	 * Finds the leaf node corresponding to the specified station ID k.
	 * @param node The node to search from.
	 * @param k The station ID to search for.
	 * @return The leaf node representing the specified station (or null if not found).
	 */
	private Node findLeafNodeForStation(Node node, int k) {
		if (node.leftChild() != null) {
			Node n = findLeafNodeForStation(node.leftChild(), k);
			if (n != null) return n;
		}

		if (node.rightChild() != null) {
			Node n = findLeafNodeForStation(node.rightChild(), k);
			if (n != null) return n;
		}

		if (node.isLeaf() && node.stations.contains(k)) {
			return node;
		}

		return null;
	}

	/** Plants tree with stations in given order at leaf nodes. */
	private Node plantSimpleTree() {
		int levels = (int) Math.ceil((Math.log(stations)/Math.log(2))) + 1;
		int stationsAtLowestLevel = 2*(stations - (int)Math.pow(2, Math.floor((Math.log(stations)/Math.log(2)))));
		boolean unbalancedLeafPlantingStage = stationsAtLowestLevel != stations;

		// Initially each subnet contains a single station.
		ArrayList<Node> currentNodes = new ArrayList<Node>();
		ArrayList<Node> levelAbove = new ArrayList<Node>();
		for (int k = 0; k < stations; k++) {
			Node node = new Node();
			node.stations.add(k);
			node.recalculateCoveredClasses();
			node.recalculateWeight();
			levelAbove.add(node);
		}

		for (int lvl = levels-1; lvl > 0; lvl--) {
			// Copy nodes at next level into current nodes, and clear nodes at next level.
			currentNodes.clear();
			currentNodes.addAll(levelAbove);
			levelAbove.clear();

			for (Node s : currentNodes) {
				s.marked = true;
			}

			boolean someSubnetsMarked = true;
			int mergeCount = 0;
			while (someSubnetsMarked) {
				Node candidate1 = null; 
				Node candidate2 = null;
				int markedCount = 0;
				for (Node s : currentNodes) {
					if (s.marked) {
						markedCount++;
						// 1st candidate
						if (candidate1 == null) {
							candidate1 = s;
							continue;
						}
						
						// 2st candidate
						if (candidate2 == null) {
							candidate2 = s;
							continue;
						}
					}
				}

				// Unmark the 2 candidates and merge them into unmarked subnet at next level.
				candidate1.marked = false;
				candidate2.marked = false;
				Node merged = candidate1.merge(candidate2);
				Printer.out.print("\nmerge: " + Arrays.toString(candidate1.stations.toArray()) + " + " +
						Arrays.toString(candidate2.stations.toArray()) + " = " + Arrays.toString(merged.stations.toArray()));
				candidate1.setParent(merged);
				candidate2.setParent(merged);
				merged.setLeftChild(candidate1);
				merged.setRightChild(candidate2);
				merged.recalculateCoveredClasses();
				merged.recalculateWeight();
				levelAbove.add(merged);
				mergeCount += 2;
				
				if (unbalancedLeafPlantingStage && mergeCount == stationsAtLowestLevel) {
					for (Node s : currentNodes) {
						if (s.marked) {
							levelAbove.add(s);
						}
					}
					unbalancedLeafPlantingStage = false;
					someSubnetsMarked = false;
				} else {
					// If any of subnets are still marked continue looping.
					someSubnetsMarked = markedCount > 2;
				}
			}
		}

		if (levelAbove.size() != 1) {
			Printer.out.println("Error in tree planting phase.");
		}

		return levelAbove.get(0);
	}

	/** Plants tree such that operations are done in same order as normal convolution. */
	private Node plantSequentialTree() {
		ArrayList<Node> leafNodes = new ArrayList<Node>();
		for (int k = 0; k < stations; k++) {
			Node node = new Node();
			node.stations.add(k);
			node.recalculateCoveredClasses();
			leafNodes.add(node);
		}

		Node currNode = leafNodes.get(0);
		for (int i = 1; i < stations; i++) {
			Node parent = new Node();
			parent.setLeftChild(currNode);
			parent.setRightChild(leafNodes.get(i));
			parent.stations.addAll(parent.leftChild().stations);
			parent.stations.addAll(parent.rightChild().stations);
			parent.recalculateCoveredClasses();
			currNode = parent;
		}
		
		return currNode;
	}
}