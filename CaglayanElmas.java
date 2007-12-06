package org.ai;

import java.util.Iterator;
import java.util.Vector;
import java.util.Random;

/*
 * Projet d'Intelligence Artificielle (2007-2008) : Bomberman
 * Equipe : CAGLAYAN Ozan, ELMAS Can
 */

public class CaglayanElmas extends ArtificialIntelligence
{
	private static final long serialVersionUID = 1L;
	private enum searchType{HORIZONTAL,VERTICAL};
	
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
	 * 
	 * void printZoneMatrix() (Déboggage)
	 * 
	 */
	
	/*
	 * Performans icin:
	 * 
	 * 1. dimX() ve dimY() cagirmadan degerler buraya tanimlanabilir,
	 * nasilsa terrain'in boyutlari deismiyor. Gereksiz fonksiyon
	 * cagrisindan kurtuluruz, cosariz.
	 * 
	 */

	public CaglayanElmas()
	{
		// Notre IA est appelé "Smart"
		super("Smart");
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
		
		// Liste des coups possibles pour jouer
		Vector<Integer> possibleMoves = getPossibleMoves(ownPosition[0], ownPosition[1]);
		Iterator<Integer> i = possibleMoves.iterator();
		
		int[] bomb = bombCanKillMe(ownPosition[0], ownPosition[1]);
		
		while (i.hasNext())
		{
			int move = i.next();
			if (bomb[0] == -2 && (move <= AI_ACTION_GO_DOWN && move >= AI_ACTION_GO_UP)) // (up,down)
				play = move;
			else if (bomb[0] == -3 && (move <= AI_ACTION_GO_RIGHT && move >= AI_ACTION_GO_LEFT))
				play = move;
		}
		
		return play;
	}
	
	public boolean wallExists(int[][] matrix, searchType s, int fix, int start, int end)
	{
		int min = Math.min(start, end);
		int max = Math.max(start, end);
		
		if (s == searchType.HORIZONTAL)
		{
			for (int i = min; i <= max; i++)
				if (matrix[i][fix] == AI_BLOCK_WALL_SOFT || matrix[i][fix] == AI_BLOCK_WALL_HARD)
					return true;
		}
		else
		{
			for (int i = min; i <= max; i++)
				if (matrix[fix][i] == AI_BLOCK_WALL_SOFT || matrix[fix][i] == AI_BLOCK_WALL_HARD)
					return true;
		}
		return false;
	}
	
	public int[] bombCanKillMe(int x, int y)
	{
		int[] result = new int[2];
		int[][] matrix = getZoneMatrix();
		
		int bombPower = 0;
		result[0] = result[1] = -1;
		
		// FIXME : Il peut y exister plusieurs bombes!!
		
		for (int i = 1; i < getZoneMatrixDimY(); i++)
		{
			// Cherche une bombe sur la meme ligne verticale que nous..
			if ((bombPower = getBombPowerAt(x, i)) != -1)
			{
				// Une bombe existe : (x,i)
				//System.out.println("Bomb(v)["+bombPower+"] X:"+x+", Y:"+i);
				
				if ( Math.abs(y-i) <= bombPower && !wallExists(matrix, searchType.VERTICAL,x,y,i))
				{
					// Ça nous touche! Il faut changer y.
					result[0] = -3;
					result[1] = 0;
				}
			}
		}
		for (int j = 1; j < getZoneMatrixDimX(); j++)
		{
			// Cherche une bombe sur la meme ligne horizontale que nous..
			if ((bombPower = getBombPowerAt(j, y)) != -1)
			{
				// Une bombe existe : (j,y)
				//System.out.println("Bomb(h)["+bombPower+"] X:"+j+", Y:"+y);
				
				if ( Math.abs(x-j) <= bombPower && !wallExists(matrix, searchType.HORIZONTAL,y,x,j) )
				{
					// Ça nous touche! Il faut changer x.
					result[0] = -2;
					result[1] = y;
				}
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
		boolean result = false;
		
		switch(move)
		{
			case AI_ACTION_GO_UP:
				result = (y > 0) && !isObstacle(x,y-1);
				break;
				
			case AI_ACTION_GO_DOWN:
				result = (y < getZoneMatrixDimY()-1) && !isObstacle(x,y+1);
				break;
				
			case AI_ACTION_GO_LEFT:
				result = (x > 0) && !isObstacle(x-1,y);
				break;
				
			case AI_ACTION_GO_RIGHT:
				result = (x < getZoneMatrixDimX()-1) && !isObstacle(x+1,y);
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
		boolean result = false;
		int state = getZoneMatrix()[x][y];
		
		// bombes
		result = result || (state == AI_BLOCK_BOMB);
		
		// feu
		result = result || (state == AI_BLOCK_FIRE);
		
		// murs
		result = result || (state == AI_BLOCK_WALL_HARD);
		result = result || (state == AI_BLOCK_WALL_SOFT);
		
		// on ne sait pas quoi
		result = result || (state == AI_BLOCK_UNKNOWN);
		
		// shrink
		result = result || (x == getNextShrinkPosition()[0] && y == getNextShrinkPosition()[1]);
		
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

}
