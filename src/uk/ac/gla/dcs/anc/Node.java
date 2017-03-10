package uk.ac.gla.dcs.anc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Node {
	private int nodeId; 
	public Map<Node,CostAndNextNode> routingTable;
	public Map<Node,Integer> neighbours;
	
	public Node(int nodeId) {
		this.nodeId = nodeId;
		this.routingTable = new HashMap<Node, CostAndNextNode>();
		this.neighbours = new HashMap<Node,Integer>();
		routingTable.put(this, new CostAndNextNode(0, this));
	}
	
	public String name() {
		return "N" + nodeId; 
	}
	
	public boolean updateConnectionIfBetter(Node node, int newCost, Node nextNode, Set<Node> tracedNode) {
		int baseCost = neighbours.get(nextNode);
		// System.out.println(name() + ": Receiving " + nextNode.name() + "'s advertisement towards " + node.name() + " with baseCost " + baseCost);
		boolean toReturn = false;
		if(!this.routingTable.containsKey(node) || (baseCost + newCost) < this.routingTable.get(node).cost) {
			toReturn = true;
			if(tracedNode != null && tracedNode.contains(this)) {
				System.out.println(name() + "=>" + node.name() + ": Swapping " + (this.routingTable.containsKey(node) ? this.routingTable.get(node).cost : "null") + " for " + (baseCost + newCost));

			}
			this.routingTable.put(node, new CostAndNextNode(newCost + baseCost, nextNode));
		}
		return toReturn;
	}
	
	public Map<Node,Integer> getAdvertisement(boolean splitHorizon, Node forWhom) {
		HashMap<Node,Integer> toReturn = new HashMap<Node,Integer>();
		for(Node key : routingTable.keySet()) {
			CostAndNextNode currentCostAndNextNode = routingTable.get(key);
			
			if(!splitHorizon || (splitHorizon && !currentCostAndNextNode.nextNode.equals(forWhom))) {
				toReturn.put(key, currentCostAndNextNode.cost);
			} else {
				toReturn.put(key, Integer.MAX_VALUE);
			}
		}
		return toReturn;
	}
	
	public boolean receiveAdvertisement(Node from, Map<Node,Integer> advertisement, Set<Node> tracedNode) {		
		boolean toReturn = false; 
		for(Node node : advertisement.keySet()) {
			Integer newCost = advertisement.get(node); 
			toReturn = updateConnectionIfBetter(node, newCost, from, tracedNode);
		}
		
		return toReturn;
	}
	
	@Override
	public String toString() {
		StringBuilder a = new StringBuilder();
		a.append("\nNode: " + name());
		
		for(Node node : routingTable.keySet()) {
			CostAndNextNode costAndNextNode = routingTable.get(node);
			a.append("\n" + node.name() + ": " + costAndNextNode.cost + " via " + costAndNextNode.nextNode.name());
		}
		
		return a.toString();
	}
	
	public int getId() {
		return this.nodeId;
	}
}
