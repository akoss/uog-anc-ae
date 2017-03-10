package uk.ac.gla.dcs.anc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class DistanceVectorRouterApplication {

	public static DistanceVectorRouter router; 
	
	public static void main(String[] args) {
		HashMap<Integer, Node> tempNodeHashmap = new HashMap<Integer, Node>();
		
		File file =  new File(args.length > 1 ? args[0] : "input2"); 
		router = new DistanceVectorRouter();
		
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
		    for(String line; (line = br.readLine()) != null; ) {
		        String[] parts = line.split(" "); 
		        
		        if(parts[0].equals("node")) {
		        	
		        	
		        	System.out.println("Adding node (" + parts[1] + ")");
		        	Node nodeToAdd = new Node(Integer.parseInt(parts[1]));
		        	router.addNode(nodeToAdd);
		        	tempNodeHashmap.put(Integer.parseInt(parts[1]), nodeToAdd);
		        	
		        	
		        } else if(parts[0].equals("link")) {
		        	
		        	System.out.println("Adding link between " + parts[1] + " and " + parts[2] + " (cost: " + parts[3] + ")");
		        	
		        	Node node1 = tempNodeHashmap.get(Integer.parseInt(parts[1]));
		        	Node node2 = tempNodeHashmap.get(Integer.parseInt(parts[2]));
		        	
		        	int cost = Integer.parseInt(parts[3]);
		        	
		        	router.addLink(node1, node2, cost);
		        } else {
		        	System.err.println("Invalid line");
		        	return;
		        }
		        
		    }
		} catch (IOException e) {
			System.err.println("Unable to read file - " + e.getMessage());
		}
		System.out.println("");
		System.out.println("Welcome!");
		boolean quit = false;
		while(!quit) {
			Scanner s = new Scanner(System.in);
			
			boolean splitHorizon = router.getSplitHorizon();
			
			System.out.println("");
			System.out.println("Main menu");
			System.out.println("=========");
			System.out.println("");
			System.out.println("1. Compute routing tables");
			System.out.println("2. Compute routing tables and trace a set of nodes");
			System.out.println("3. Change link cost immediately");
			System.out.println("4. Change link cost after a number of exchanges");
			System.out.println("5. Turn split-horizon " + (splitHorizon ? "off" : "on"));
			System.out.println("6. View current best route between two nodes");
			System.out.println("7. View routing table of a node");
			System.out.println("8. View routing table of all nodes");
			System.out.println("");
			System.out.println("0. Quit");
			System.out.print  ("> ");
			int answer = s.nextInt();

			if(answer == 0) {
				quit = true;
			} else if(answer == 1 || answer == 2) {
				System.out.println("");
				System.out.println("How many iterations would you like to make? ");
				System.out.println("Enter -1 to iterate until stability is achieved ");
				System.out.print  ("> ");
				int answer_iter = s.nextInt();
				int iteration_counter = 0;
				HashSet<Node> tracedNodes = new HashSet<Node>();
				if(answer == 2) {
					System.out.println("");
					System.out.println("Which node(s) would you like to trace? \nEnter one per line, then 0 when finished.");
					int answer_trace = -1;
					while(answer_trace != 0) {
						System.out.print  ("> ");	
						answer_trace = s.nextInt();
						tracedNodes.add(router.getNodes().get(answer_trace));						
					}
				} else {
					tracedNodes = null;
				}
				if(answer_iter == -1) {
					while(router.iterate(tracedNodes)) {
						iteration_counter++;						
						System.out.println("Iteration - " + iteration_counter);
					}
				} else {
					while(iteration_counter < answer_iter) {
						System.out.println("Iteration - " + iteration_counter);
						router.iterate(tracedNodes);
						iteration_counter++;
					}
				}
				 
			} else if(answer == 3 || answer == 4) {
				System.out.println("");
				System.out.println("Please pick the first node: ");
				System.out.print  ("> ");
				int answer_node1 = s.nextInt();
				System.out.println("");
				System.out.println("Please pick the second node: ");
				System.out.print  ("> ");
				int answer_node2 = s.nextInt();
				Node node1 = router.getNodes().get(answer_node1);
				Node node2 = router.getNodes().get(answer_node2);
				
				if(node1.neighbours.containsKey(node2)) {
					System.out.println("The current distance of these two nodes is " + node1.neighbours.get(node2) + ".");
				} else {
					System.out.println("These two nodes aren't neighbours");
				}

				System.out.println("");
				System.out.println("Please enter new cost, or enter 0 to delete connection.");
				System.out.print  ("> ");
				int newCost; 
				
				newCost = s.nextInt();
			
				if(newCost == 0) {
					newCost = Integer.MAX_VALUE;
				}
				
				if(answer == 3) {
					router.addLink(node1, node2, newCost);
				} else {

					System.out.println("");
					System.out.println("Please enter how many iterations would you like to apply this after");
					System.out.print  ("> ");
					int afterIterations = s.nextInt();
					router.addCostModificationScheduleItem(node1, node2, newCost, afterIterations);
				}
				
			} else if(answer == 5) {
				 router.setSplitHorizon(!router.getSplitHorizon());
				 System.out.println("Split Horizon is now " + (router.getSplitHorizon() ? "on" : "off"));
			} else if(answer == 6) {
				System.out.println("");
				System.out.println("Please pick the first node: ");
				System.out.print  ("> ");
				int answer_node1 = s.nextInt();
				System.out.println("");
				System.out.println("Please pick the second node: ");
				System.out.print  ("> ");
				int answer_node2 = s.nextInt();
				
				Node node1 = router.getNodes().get(answer_node1);
				Node node2 = router.getNodes().get(answer_node2);
				
				Node currentNode = node1;
				int hops = 0;
				int totalCost = 0;
				
				if(!node1.routingTable.containsKey(node2)) {
					System.out.println("There is no connection between " + node1.name() + " and " + node2.name());
				} else {
					System.out.println("Planned cost: " + node1.routingTable.get(node2).cost);
					System.out.print(node1.name() + " (0) ");
					while(currentNode != node2) {
						CostAndNextNode currentCostAndNextNode = currentNode.routingTable.get(node2);
						int currentCost = currentNode.routingTable.get(currentCostAndNextNode.nextNode).cost;
						System.out.print(" => " + currentCostAndNextNode.nextNode.name() + " (" + currentCost + ") ");
						currentNode = currentCostAndNextNode.nextNode;
						totalCost += currentCost;
						hops++;
					}
					System.out.println("");
					System.out.println(" Actual cost: " + totalCost + ", number of hops: " + hops);
				}
				 
			} else if(answer == 7) {
				System.out.println("");
				System.out.println("Please pick a node: ");
				System.out.print  ("> ");
				int answer_node = s.nextInt();
				System.out.println(router.getNodes().get(answer_node).toString());
			} else if(answer == 8) {
				for(Node node : router.getNodes().values()) {
					System.out.println(node.toString());
				}
			}
		}
		System.out.println("Bye");
	}
	

}
