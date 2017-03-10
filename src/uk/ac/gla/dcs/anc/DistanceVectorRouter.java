package uk.ac.gla.dcs.anc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class CostAndNextNode {
	public int cost; 
	public Node nextNode; 
	
	public CostAndNextNode(int cost, Node nextNode) {
		this.cost = cost; 
		this.nextNode = nextNode;
	}
	
	@Override
	public String toString() {
		return "{next node: " + nextNode.name() + ", cost: " + cost + "}";
	}
}

class CostModificationSchedule {
	public Node node1;
	public Node node2;
	public int cost;
	public int iterationsLeft;
	
	public CostModificationSchedule(Node node1, Node node2, int cost, int afterIterations) {
		this.node1 = node1;
		this.node2 = node2;
		this.cost = cost;
		this.iterationsLeft = afterIterations;
	}
	
	public boolean execute(DistanceVectorRouter context) {
		if(iterationsLeft == 1) {
			System.out.println("Executing scheduled cost modification - " + node1.name() + "-" + node2.name() + " -> " + cost);
			context.addLink(node1, node2, cost);
			return true;
		}
		iterationsLeft--;
		return iterationsLeft == 0;
	}
}

public class DistanceVectorRouter { 
	private Map<Integer,Node> nodes; 
	private Set<CostModificationSchedule> schedule; 
	
	private boolean splitHorizon; 
	
	public DistanceVectorRouter() {
		nodes = new HashMap<Integer,Node>(); 
		splitHorizon = false;
		schedule = new HashSet<CostModificationSchedule>();
	}
	
	public void addNode(Node node) {
		nodes.put(node.getId(),node);
	}
	
	public void addLink(Node node1, Node node2, int cost) {
    	node1.routingTable.put(node2, new CostAndNextNode(cost,node2));
    	node2.routingTable.put(node1,  new CostAndNextNode(cost, node1));
    	
    	if(cost != Integer.MAX_VALUE) {
    		node1.neighbours.put(node2, cost);
        	node2.neighbours.put(node1, cost);	
    	} else {
    		node1.neighbours.remove(node2);
    		node2.neighbours.remove(node1);
    	}
	}
	
	public boolean iterate(Set<Node> tracedNode) {
		// System.out.println("Iterating");
		
		boolean toReturn = false;
		for(Node nodeToPublish : nodes.values()) {
			for(Node receiver : nodeToPublish.neighbours.keySet()) {
				// System.out.println("Publishing " + nodeToPublish.name() + " for "+ receiver.name() + (toReturn ? " - true" : " - false"));
				toReturn = toReturn ? true : receiver.receiveAdvertisement(nodeToPublish, nodeToPublish.getAdvertisement(this.splitHorizon, receiver), tracedNode);
			}
		}
		
		for(CostModificationSchedule scheduleItem : schedule) {
			if(scheduleItem.execute(this)) {
				schedule.remove(scheduleItem);
			}
		}
		
		return toReturn;
	}
	
	public void addCostModificationScheduleItem(Node node1, Node node2, int cost, int afterIterations) {
		schedule.add(new CostModificationSchedule(node1, node2, cost, afterIterations));
	}
	
	public HashMap<Integer,Node> getNodes() {
		return new HashMap<Integer,Node>(nodes);
	}
	
	public void setSplitHorizon(boolean s) {
		this.splitHorizon = s;
	}
	
	public boolean getSplitHorizon() {
		return this.splitHorizon;
	}
}
