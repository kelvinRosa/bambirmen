package org.ai;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Random;


/*
 * Projet d'Intelligence Artificielle (2007-2008) : Bomberman
 * Equipe : CAGLAYAN Ozan, ELMAS Can
 */

public class CaglayanElmas extends ArtificialIntelligence
{
	private static final long serialVersionUID = 1L;
	
	// "Pseudo-Random Number Generator"
	private Random prng;
	
	// Si n'est pas vide, il existe une destination qu'on doit arriver.
	private int[] target;
	
	/* Les méthodes disponibles depuis la classe "ArtificialIntelligence"
	 * 
	 * int[][] getZoneMatrix()
	 * int getZoneMatrixDimX()
	 * int getZoneMatrixDimY()
	 * int getBombPowerAt(int x, int y)
	 * int getPlayerCount()
	 * int getPlayerPosition(int index)
	 * int getPlayerDirection(int index)
	 * long getTimeBeforeShrink()
	 * int[] getNextShrinkPosition()
	 * int[] getOwnPosition()
	 * int getBombPosition()
	 * 
	 * void printZoneMatrix() (Déboggage)
	 * 
	 */

	public CaglayanElmas()
	{
		// Notre IA est appelé "Smart"
		super("Smart");
		
		// Init. prng
		this.prng = new Random();
	}
	
	/**
	 * Détermine la prochaine action que l'IA va effectuer
	 * (Bouger, ne rien faire, poser une bombe)
	 * 
	 * @return	AI_ACTION_XXXX
	 */
	
	public Integer call() throws Exception
	{
		// Le coup à jouer
		Integer play = AI_ACTION_DO_NOTHING;
		
		// Notre position
		int[] ownPosition = getOwnPosition();
		
		// play contient l'action qu'il faut commettre pour survivre
		play = bombCanKillMe(ownPosition[0], ownPosition[1]);
		
		return play;
	}
	
	// Pas encore utilisé
	public Integer getNextMoveToTarget(int x, int y)
	{
		Integer result = AI_ACTION_DO_NOTHING;
		
		// On a une destination à aller : target[]
		if (this.target[0] == x && this.target[1] == y)
			this.target[0] = this.target[1] = -1;
		
		else
		{
			// On n'a pas encore arrivé.
			int dx = this.target[0] - x;
			int dy = this.target[1] - y;
			
			//Liste des coups possibles pour jouer
			Vector<Integer> possibleMoves = getPossibleMoves(x, y);
			Iterator<Integer> i = possibleMoves.iterator();
			
			if (dx == 1)
				return AI_ACTION_GO_RIGHT;
			
			if (dy == 1)
				return AI_ACTION_GO_DOWN;
		}
		return result;
	}
	
	public Integer escapeFromBomb(int x, int y, int direction)
	{
		Integer result = AI_ACTION_DO_NOTHING;
		Vector<Integer> possibleMoves = getPossibleMoves(x, y);
		
		if (direction == 0)
		{
			// Deplace sur l'horizontale.
			if (possibleMoves.contains(AI_ACTION_GO_RIGHT))
				result = AI_ACTION_GO_RIGHT;
			else if(possibleMoves.contains(AI_ACTION_GO_LEFT))
				result = AI_ACTION_GO_LEFT;
			else if(possibleMoves.contains(AI_ACTION_GO_DOWN))
				result = AI_ACTION_GO_DOWN;
			else if(possibleMoves.contains(AI_ACTION_GO_UP))
				result = AI_ACTION_GO_UP;
		}
		else if (direction == 1)
		{
			// Deplace sur la verticale.
			if (possibleMoves.contains(AI_ACTION_GO_DOWN))
				result = AI_ACTION_GO_DOWN;
			else if(possibleMoves.contains(AI_ACTION_GO_UP))
				result = AI_ACTION_GO_UP;
			else if(possibleMoves.contains(AI_ACTION_GO_RIGHT))
				result = AI_ACTION_GO_RIGHT;
			else if(possibleMoves.contains(AI_ACTION_GO_LEFT))
				result = AI_ACTION_GO_LEFT;
		}
		return result;
	}

	public Integer bombCanKillMe(int x, int y)
	{
		Integer result = AI_ACTION_DO_NOTHING;
		int[][] matrix = getZoneMatrix();
		
		int bombPower = 0;
		
		int dimX = getZoneMatrixDimX();
		int dimY = getZoneMatrixDimY();
		
		// FIXME : Il peut y exister plusieurs bombes!!
		
		for (int i = 1; i < dimY; i++)
		{
			// Cherche une bombe sur la meme ligne verticale que nous..
			if ((bombPower = getBombPowerAt(x, i)) != -1)
			{
				int min = Math.min(i, y);
				int max = Math.max(i, y);
				boolean wallExists = false;
						
				// Une bombe existe : (x,i)
				//System.out.println("Bomb(v)["+bombPower+"] X:"+x+", Y:"+i);
				
				// Est-ce qu'il y a un mur entre nous?
				for (int k = min+1; k < max && !wallExists; k++)
					if (matrix[x][k] == AI_BLOCK_WALL_SOFT || matrix[x][k] == AI_BLOCK_WALL_HARD)
						wallExists = true;
				
				if ( !wallExists && Math.abs(y-i) <= bombPower)
					return escapeFromBomb(x, y, 0);
			}
		}
		
		for (int j = 1; j < dimX; j++)
		{	
			// Cherche une bombe sur la meme ligne horizontale que nous..
			if ((bombPower = getBombPowerAt(j, y)) != -1)
			{
				int min = Math.min(j, x);
				int max = Math.max(j, x);
				boolean wallExists = false;
				
				// Une bombe existe : (j,y)
				//System.out.println("Bomb(h)["+bombPower+"] X:"+j+", Y:"+y);
				
				// Est-ce qu'il y a un mur entre nous?
				for (int k = min+1; k < max && !wallExists; k++)
					if (matrix[k][x] == AI_BLOCK_WALL_SOFT || matrix[k][x] == AI_BLOCK_WALL_HARD)
						wallExists = true;
				
				if ( !wallExists && Math.abs(x-j) <= bombPower)
					return escapeFromBomb(x, y, 1);

			}
		}
		return result;
	}
	
	/**
	 * Indique si le déplacement dont le code a été passé en paramètre 
	 * est possible pour un personnage situé en (x,y).
	 * @param x	position du personnage
	 * @param y position du personnage
	 * @param move	le déplacement à étudier
	 * @return	vrai si ce déplacement est possible
	 */
	private boolean isMovePossible(int x, int y, int move)
	{	
		boolean result;
		switch(move)
		{	
			case ArtificialIntelligence.AI_ACTION_GO_UP:
				result = y>0 && !isObstacle(x,y-1);
				break;
			case ArtificialIntelligence.AI_ACTION_GO_DOWN:
				result = y<(getZoneMatrixDimY()-1) && !isObstacle(x,y+1);
				break;
			case ArtificialIntelligence.AI_ACTION_GO_LEFT:
				result = x>0 && !isObstacle(x-1,y);
				break;
			case ArtificialIntelligence.AI_ACTION_GO_RIGHT:
				result = x<(getZoneMatrixDimX()-1) && !isObstacle(x+1,y);
				break;
			default:
				result = false;
				break;
		}
		return result;
	}
	
	/**
	 * Indique si la case située à la position passée en paramètre
	 * constitue un obstacle pour un personnage : bombe, feu, mur.
	 * @param x	position à étudier
	 * @param y	position à étudier
	 * @return	vrai si la case contient un obstacle
	 */
	private boolean isObstacle(int x, int y)
	{	
		int[][] matrix = getZoneMatrix();
		boolean result = false;
		
		// bombes
		result = result || matrix[x][y] == AI_BLOCK_BOMB;
		
		// feu
		result = result || matrix[x][y] == AI_BLOCK_FIRE;
		
		// murs
		result = result || matrix[x][y] == AI_BLOCK_WALL_HARD;
		result = result || matrix[x][y] == AI_BLOCK_WALL_SOFT;
		
		// on ne sait pas quoi
		result = result || matrix[x][y] == AI_BLOCK_UNKNOWN;
		
		// shrink
		result = result || (getTimeBeforeShrink() == -1 && x == getNextShrinkPosition()[0]&& y == getNextShrinkPosition()[1]);
		return result;
	}
	
	/**
	 * Renvoie la liste de tous les déplacements possibles
	 * pour un personnage situé à la position (x,y)
	 * @param x	position du personnage
	 * @param y position du personnage
	 * @return	la liste des déplacements possibles
	 */
	private Vector<Integer> getPossibleMoves(int x, int y)
	{	
		Vector<Integer> result = new Vector<Integer>();
		
		for(int move = AI_ACTION_GO_UP; move <= AI_ACTION_GO_RIGHT; move++)
			if(isMovePossible(x, y, move))
				result.add(move);
		return result;
	}

	/**
	 * Calcule et renvoie la distance de Manhattan 
	 * entre le point de coordonnées (x1,y1) et celui de coordonnées (x2,y2). 
	 * @param x1	position du premier point
	 * @param y1	position du premier point
	 * @param x2	position du second point
	 * @param y2	position du second point
	 * @return	la distance de Manhattan entre ces deux points
	 */
	private int distance(int x1, int y1, int x2, int y2)
	{	
		int result = 0;
		result = result + Math.abs(x1-x2);
		result = result + Math.abs(y1-y2);
		return result;
	}
	
	/**
	 * Parmi les blocs dont le type correspond à la valeur blockType
	 * passée en paramètre, cette méthode cherche lequel est le plus proche
	 * du point de coordonnées (x,y) passées en paramètres. Le résultat
	 * prend la forme d'un tableau des deux coordonées du bloc le plus proche.
	 * Le tableau est contient des -1 s'il n'y a aucun bloc du bon type dans la zone de jeu.
	 * @param x	position de référence
	 * @param y	position de référence
	 * @param blockType	le type du bloc recherché
	 * @return	les coordonnées du bloc le plus proche
	 */
	private int[] getClosestBlockPosition(int x, int y, int blockType)
	{	
		int minDistance = Integer.MAX_VALUE;
		int result[] = {-1, -1}; 
		int[][] matrix = getZoneMatrix();
		int dimX = getZoneMatrixDimX();
		int dimY = getZoneMatrixDimY();
		
		for(int i = 0; i < dimX; i++)
			for(int j = 0; j < dimY; j++)
				if(matrix[i][j] == blockType)
				{	
					int tempDistance = distance(x, y, i, j); 	
					if(tempDistance < minDistance)
					{	
						minDistance = tempDistance;
						result[0] = i;
						result[1] = j;
					}
				}
		return result;
	}

}
