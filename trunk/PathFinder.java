package org.ai;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Vector;

import org.ai.SearchLink;
import org.ai.SearchNode;

public class PathFinder
{
	/*
	 * Module d'IA qui va trouver le plus court chemin
	 * entre un case initial et un case final en considérant
	 * les obstacles. On va utiliser A* avec une heuristique
	 * étant égale à la distance de Manhattan entre les deux
	 * cases.
	 */
	
	private Vector<SearchNode> nodes;
	private Vector<SearchLink> links;
	
	private int[] initialState;
	private int[] finalState;
	
	private int[][] stateSpace;
	private int dimX, dimY;
	
	public PathFinder(int[][] stateSpace)
	{
		this.stateSpace = stateSpace;
		this.dimX = stateSpace.length;
		this.dimY = stateSpace[0].length;
		
		this.nodes = new Vector<SearchNode>();
		this.links = new Vector<SearchLink>();
	}
	
	public void setStates(int[] initialState, int[] finalState)
	{
		this.initialState = initialState.clone();
		this.finalState = finalState.clone();
		
		this.nodes.add(new SearchNode(this.initialState, 0, 10.0, 
									  this.calculateHeuristic(this.initialState, this.finalState)));
	}
	
	public void markVisitedNode(SearchNode node, int iteration)
	{	
		node.markVisited(iteration);
	}
	
	public int calculateHeuristic(int[] s1, int[] s2)
	{
		return 10*(Math.abs(s1[0]-s2[0])+Math.abs(s1[1]-s2[1]));
	}

	private boolean isMovePossible(int x, int y, int move)
	{	
		boolean result;
		switch(move)
		{	
			case ArtificialIntelligence.AI_ACTION_GO_UP:
				result = y>0 && !isObstacle(x,y-1);
				break;
			case ArtificialIntelligence.AI_ACTION_GO_DOWN:
				result = y<(this.dimY-1) && !isObstacle(x,y+1);
				break;
			case ArtificialIntelligence.AI_ACTION_GO_LEFT:
				result = x>0 && !isObstacle(x-1,y);
				break;
			case ArtificialIntelligence.AI_ACTION_GO_RIGHT:
				result = x<(this.dimX-1) && !isObstacle(x+1,y);
				break;
			default:
				result = false;
				break;
		}
		return result;
	}

	private boolean isObstacle(int x, int y)
	{	
		boolean result = false;
		
		// bombes
		result = result || this.stateSpace[x][y] == ArtificialIntelligence.AI_BLOCK_BOMB;
		
		// feu
		result = result || this.stateSpace[x][y] == ArtificialIntelligence.AI_BLOCK_FIRE;
		
		// murs
		result = result || this.stateSpace[x][y] == ArtificialIntelligence.AI_BLOCK_WALL_HARD;
		result = result || this.stateSpace[x][y] == ArtificialIntelligence.AI_BLOCK_WALL_SOFT;
		
		// on ne sait pas quoi
		result = result || this.stateSpace[x][y] == ArtificialIntelligence.AI_BLOCK_UNKNOWN;
		
		// shrink

		return result;
	}

	private Vector<Integer> getPossibleMoves(int x, int y)
	{	
		Vector<Integer> result = new Vector<Integer>();
		
		for(int move = ArtificialIntelligence.AI_ACTION_GO_UP; move <= ArtificialIntelligence.AI_ACTION_GO_RIGHT; move++)
			if(isMovePossible(x, y, move))
				result.add(move);
		return result;
	}
	
	public void findShortestPath()
	{
		PriorityQueue<SearchNode> fringe = new PriorityQueue<SearchNode>(1, new SearchNodeAStarComparator());
		
		Iterator<SearchLink> sl = null;
		SearchNode sn = null;
		boolean condition = true;
		int iteration = 0;
		
		fringe.offer(this.nodes.get(0));

		while (condition && !fringe.isEmpty())
		{
			sn = fringe.poll();
			// DEBUG
			System.out.println("[PathFinder]Polled node ("+sn.getState()[0]+","+sn.getState()[1]+")");
			
			this.markVisitedNode(sn, iteration);
			
			if (!((sn.getState()[0] == this.finalState[0]) && (sn.getState()[1] == this.finalState[1])))
			{
				sl = this.developNode(sn);
				
				while (sl.hasNext())
				{
					SearchNode snt = sl.next().getTarget();
					System.out.println("\tNew Node :("+snt.getState()[0]+","+snt.getState()[1]+")");
					fringe.offer(snt);
				}
				iteration++;
			}
			
			else
				condition = false;
		}
	}
	
	public boolean containsNode(SearchNode node)
	{	
		boolean result = false;
		Iterator<SearchNode> i = nodes.iterator();
		while(i.hasNext() && !result)
		{
			SearchNode snt = i.next();
			result = (node.getState()[0] == snt.getState()[0]) &&
					 (node.getState()[1] == snt.getState()[1]);
		}
		return result;
	}
	
	public Iterator<SearchLink> developNode(SearchNode node)
	{	
		Vector<SearchLink> result = new Vector<SearchLink>();
		if(!((node.getState()[0] == this.finalState[0]) && (node.getState()[1] == this.finalState[1])))
		{	
			Iterator<Integer> i = getPossibleMoves(node.getState()[0], node.getState()[1]).iterator();
			while(i.hasNext())
			{
				Integer action = i.next();
				int[] targetState = node.getState().clone();
				
				//System.out.println("\tAction: "+action);
				switch (action)
				{
					case ArtificialIntelligence.AI_ACTION_GO_LEFT:
						targetState[0]--;
						break;
						
					case ArtificialIntelligence.AI_ACTION_GO_RIGHT:
						targetState[0]++;
						break;
						
					case ArtificialIntelligence.AI_ACTION_GO_UP:
						targetState[1]--;
						break;
						
					case ArtificialIntelligence.AI_ACTION_GO_DOWN:
						targetState[1]++;
						break;	
				}
				//System.out.println("\tTargetState: "+targetState[0]+","+targetState[1]);

				int targetDepth = node.getDepth() + 1;
				double targetCost = node.getCost() + 10;
				double targetHeuristic = this.calculateHeuristic(targetState, this.finalState);
				SearchNode target = new SearchNode(targetState, targetDepth, targetCost, targetHeuristic);
				
				if (!this.containsNode(target))
				{
					System.out.println("Adding target:("+target.getState()[0]+","+target.getState()[1]+")");
					SearchLink link = new SearchLink(node, target, action);
					result.add(link);
					this.links.add(link);
					this.nodes.add(target);
				}
			}
		}
		return result.iterator();
	}
	
	public Vector<SearchLink> getPath(SearchNode node)
	{	
		Vector<SearchLink> result;
		SearchLink parentLink = null;

		if (node != this.nodes.get(0))
		{
			Iterator<SearchLink> i = links.iterator();
			while(i.hasNext() && parentLink == null)
			{	
				SearchLink temp = i.next();
				if(temp.getTarget().equals(node))
					parentLink = temp;
			}
		}
		
		if(parentLink == null)
			result = new Vector<SearchLink>();
		else
		{	
			result = getPath(parentLink.getOrigin());
			result.add(parentLink);
		}
		
		//System.out.println(result);
		return result;
	}
	
	public void printSolution()
	{
		Iterator<SearchLink> sl = getPath(new SearchNode(this.finalState,0,0,0)).iterator();
		while (sl.hasNext())
		{
			SearchNode snt = sl.next().getTarget();
			System.out.println(snt.getState()[0]+","+snt.getState()[1]);
		}
	}
	
}
