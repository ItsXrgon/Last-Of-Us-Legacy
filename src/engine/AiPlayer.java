package engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.awt.Point;

import exceptions.InvalidTargetException;
import exceptions.MovementException;
import exceptions.NoAvailableResourcesException;
import exceptions.NotEnoughActionsException;
import model.world.CollectibleCell;
import views.GameMap;
import views.Main;
import model.characters.Direction;
import model.characters.Explorer;
import model.characters.Fighter;
import model.characters.Hero;
import model.characters.Medic;
import model.characters.Zombie;
import model.collectibles.Supply;
import model.collectibles.Vaccine;
import model.world.CharacterCell;

public class AiPlayer {

    public static boolean checkValidMove(int x, int y) {

        if(!(x >= 0 && x <= 14 && y >=0 && y<= 14) || 
        Game.map[x][y] instanceof CharacterCell && ((CharacterCell) Game.map[x][y]).getCharacter() != null) {
            return false;
        }
        
        return true;
    }

    public static ArrayList<Point> findOptimalPath(Hero h, int targetX, int targetY) {
        int currX = (int) h.getLocation().getX();
        int currY = (int) h.getLocation().getY();

        boolean[][] visited = new boolean[15][15]; // Track visited cells
        int[][] parentX = new int[15][15]; // Track parent X-coordinates for each cell
        int[][] parentY = new int[15][15]; // Track parent Y-coordinates for each cell
        Queue<Point> queue = new LinkedList<Point>(); // Queue for BFS traversal
        queue.add(new Point(currX, currY)); // Add starting point to the queue
        visited[currX][currY] = true; // Mark starting point as visited

        int[] dx = {0, 0, 1, -1}; // Possible x-direction movements
        int[] dy = {1, -1, 0, 0}; // Possible y-direction movements

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            int cx = current.x;
            int cy = current.y;

            if (cx == targetX && cy == targetY) {
                // Build path using parent coordinates
                ArrayList<Point> path = new ArrayList<Point>();
                path.add(new Point(cx,cy));
                while (cx != currX || cy != currY) {
                    int px = parentX[cx][cy];
                    int py = parentY[cx][cy];
                    path.add(new Point(cx,cy));
                    cx = px;
                    cy = py;
                }
                
                Collections.reverse(path); // Reverse path to get correct order (from start to end
                return path;
            }

            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];

                if (nx >= 0 && nx < 15 && ny >= 0 && ny < 15 && !visited[nx][ny] && checkValidMove(nx, ny)) {
                    queue.add(new Point(nx, ny)); // Add valid neighbor to the queue
                    visited[nx][ny] = true; // Mark neighbor as visited
                    parentX[nx][ny] = cx; // Track parent X-coordinate
                    parentY[nx][ny] = cy; // Track parent Y-coordinate
                }
            }
        }

        ArrayList<Point> path = new ArrayList<Point>();
        for(int i=0; i<30; i++) {
            path.add(new Point(0, 0));
        }

        return path; // Target not reachable
    }


    public static void moveTo(Hero h, int x, int y) throws MovementException,  NotEnoughActionsException {

        ArrayList<Point> path = findOptimalPath(h, x, y);
        int indent = 1;
        
        if(Game.map[x][y] instanceof CharacterCell && ((CharacterCell) Game.map[x][y]).getCharacter() != null) {
            indent = 2;
        }
        
        if (path != null) {
            for (int i = 0; i < path.size() - indent; i++) {
                Point cell = path.get(i);
                if(h.getCurrentHp() == 0 || h.getActionsAvailable() == 0) {
                    return;
                }
                if(cell.x > h.getLocation().x) {
                    System.out.println("up");
                    h.move(Direction.UP);
                } else if(cell.x < h.getLocation().x) {
                    System.out.println("down");
                    h.move(Direction.DOWN);
                } else if(cell.y > h.getLocation().y) {
                    System.out.println("right");
                    h.move(Direction.RIGHT);
                } else if(cell.y < h.getLocation().y) {
                    System.out.println("left");
                    h.move(Direction.LEFT);
                }
            }
        }
        
    }

    public static int[] findShortestPath(ArrayList<Point> targets, Hero h) {
        int targetX = 99;
        int targetY = 99;
        int pathLength = 99;

        // Find shortest path to a target from a list of targets
        for(Point z : targets) {
            int currTargetX = z.x;
            int currTargetY = z.y;
            int currPathToTarget = findOptimalPath(h, currTargetX, currTargetY).size();
            if(currPathToTarget < pathLength) {
                pathLength = currPathToTarget;
                targetX = currTargetX;
                targetY = currTargetY;
            }
        }

        //System.out.println("t x: " + targetX + " t y:" + targetY + " path:" + pathLength);
        int[] output = {targetX, targetY, pathLength};

        return output;
    }


    public static void moveToSafeCell(Hero h) throws MovementException, NotEnoughActionsException {

        
        if(Game.map[h.getLocation().x][h.getLocation().y] instanceof CharacterCell && ((CharacterCell) Game.map[h.getLocation().x][h.getLocation().y]).isSafe() == true) {
           return;
        }

        if (h.getActionsAvailable() == 0) {
            return;
        }

        for(int i=h.getLocation().x-1; i<h.getLocation().x+2; i++) {
            for(int j=h.getLocation().x-1; j<h.getLocation().y+2; j++) {
                if(i != h.getLocation().x && j != h.getLocation().y) {
                    if(!(Game.map[i][j] instanceof CharacterCell && ((CharacterCell) Game.map[i][j]).isSafe() == true)) {
                        moveTo(h, i, j);
                    }
                }
            }
        }
    }


    public static boolean decideBestMove(Hero h) throws MovementException, NotEnoughActionsException, NoAvailableResourcesException, InvalidTargetException {
        
    	
    	
        // if hero has no actions left, return
        if(h.getActionsAvailable() == 0) {
            return false;
        }
		System.out.println(h.getName());
        System.out.println(0);
       
        
        // Get all visible zombies
        ArrayList<Point> visibleZombies = new ArrayList<Point>();
        for(int i=0; i<15; i++) {
            for(int j=0; j<15; j++) {
                if(Game.map[i][j].isVisible() && Game.map[i][j] instanceof CharacterCell && ((CharacterCell) Game.map[i][j]).getCharacter() instanceof Zombie) {
                    visibleZombies.add(new Point(i, j));
                }
            }
        }

        // If hero has a vaccine and there are visible zombies, move towards the closest zombie and cure it
        if(!h.getVaccineInventory().isEmpty() && !visibleZombies.isEmpty()) {
            
            int[] zombieLocation = findShortestPath(visibleZombies, h);

            int zombieX = zombieLocation[0];
            int zombieY = zombieLocation[1];
            int pathLength = zombieLocation[2];
            
        	Zombie z = (Zombie) ((CharacterCell) Game.map[zombieX][zombieY]).getCharacter(); 
            if(!h.adjacent(z)) {
            	moveTo(h, zombieX, zombieY);
            	System.out.println(00);
            	return true;
            }
            // cure zombie if path is short enough
            if (h.adjacent(z) && h.getActionsAvailable() != 0) {
            	h.setTarget(z);
            	System.out.println("curing");
	            h.cure();
            	System.out.println(000);
            	return true;
            }
       
        }

        System.out.println(1);
        // get locations of all Ai heroes
        ArrayList<Point> AllyHeroes = new ArrayList<Point>();
        for(Hero hero : Game.heroes) {
            AllyHeroes.add(new Point(hero.getLocation().x, hero.getLocation().y));
        }

        // if hero is a medic and has a supply, move towards the closest Ai Hero with less than 50% health and heal it
        if(h instanceof Medic && !h.getSupplyInventory().isEmpty()) {

            int[] heroLocation = findShortestPath(AllyHeroes, h);

            int heroX = heroLocation[0];
            int heroY = heroLocation[1];

            // Heal hero if path is short enough
            moveTo(h, heroX, heroY);
            if (h.adjacent(((CharacterCell) Game.map[heroX][heroY]).getCharacter())) {
            	h.setTarget(((CharacterCell) Game.map[heroX][heroY]).getCharacter());
            	h.useSpecial();
            }
            return true;
        }

        System.out.println(2);
        // Get all visible vaccine cells
        ArrayList<Point> vaccineCells = new ArrayList<Point>();
        for(int i=0; i<15; i++) {
            for(int j=0; j<15; j++) {
                if(Game.map[i][j].isVisible() && Game.map[i][j] instanceof CollectibleCell && ((CollectibleCell) Game.map[i][j]).getCollectible() instanceof Vaccine) {
                    vaccineCells.add(new Point(i,j));
                }
            }
        }

        // If there are visible vaccine cells, move to the closest one
        if(!vaccineCells.isEmpty()) {

            int[] vaccineLocation = findShortestPath(visibleZombies, h);

            int cellX = vaccineLocation[0];
            int cellY = vaccineLocation[1];
            int pathLength = vaccineLocation[2];

            // Move to vaccine cell if path is available
            if(pathLength <= h.getActionsAvailable()) {
                moveTo(h, cellX, cellY);
                return true;
            }
        }
        System.out.println(3);

        // Get all visible supply cells
        ArrayList<Point> supplyCells = new ArrayList<Point>();
        for(int i=0; i<15; i++) {
            for(int j=0; j<15; j++) {
                if(Game.map[i][j].isVisible() && Game.map[i][j] instanceof CollectibleCell && ((CollectibleCell) Game.map[i][j]).getCollectible() instanceof Supply) {
                    supplyCells.add(new Point(i,j));
                }
            }
        }

        // If there are visible supply cells, move to the closest one
        if(!supplyCells.isEmpty()) {

            int[] supplyLocation = findShortestPath(supplyCells, h);

            int cellX = supplyLocation[0];
            int cellY = supplyLocation[1];
            int pathLength = supplyLocation[2];

            // Move to supply cell if path is short enough
            if(pathLength <= h.getActionsAvailable()) {
                moveTo(h, cellX, cellY);
                supplyCells.remove(new Point(cellX, cellY));
                return true;
            }
        }
        System.out.println(4);

        // if hero has hp > 15, move towards the closest visible zombie and attack it
        if(h.getCurrentHp() > 15) {

            int[] zombieLocation = findShortestPath(visibleZombies, h);

            int zombieX = zombieLocation[0];
            int zombieY = zombieLocation[1];
            int pathLength = zombieLocation[2];

            // Attack zombie normally if path is short enough
            if(pathLength < h.getActionsAvailable()-1) {
                Zombie z = (Zombie) ((CharacterCell) Game.map[zombieX][zombieY]).getCharacter();
                moveTo(h, zombieX, zombieY);
                h.setTarget(z);
                while(h.getCurrentHp() > 15 && h.getTarget().getCurrentHp() > 0 && h.getActionsAvailable() != 0) {
                    h.attack();
                }
                return true;
            }
        }

        System.out.println(5);

        // if hero is a figher, has hp > 15, and has a supply, move towards the closest visible zombie and attack it with special 
        if(h instanceof Fighter && !h.getSupplyInventory().isEmpty() && h.getCurrentHp() > 15) {

            int[] zombieLocation = findShortestPath(visibleZombies, h);

            int zombieX = zombieLocation[0];
            int zombieY = zombieLocation[1];
            int pathLength = zombieLocation[2];

            // Attack zombie with special if path is short enough
            if(pathLength <= h.getActionsAvailable()) {
                Zombie z = (Zombie) ((CharacterCell) Game.map[zombieX][zombieY]).getCharacter();
                h.setTarget(z);
                moveTo(h, zombieX, zombieY);
                ((Fighter) h).useSpecial();
                while(h.getCurrentHp() > 15 && h.getTarget().getCurrentHp() > 0) {
                    h.attack();
                }
                return true;
            }
        }

        System.out.println(6);
        // if hero is an explorer and has a supply, use special to make map visibile for all heroes
        if(h instanceof Explorer && !h.getSupplyInventory().isEmpty() && !h.isSpecialAction()) {
            h.useSpecial();
            return true;
        }


        System.out.println(7);
        // if no action has been done yet, Make 1 move to nearest vaccine cell if path is safe
        if(!vaccineCells.isEmpty()) {
            
            int[] vaccineLocation = findShortestPath(vaccineCells, h);

            int cellX = vaccineLocation[0];
            int cellY = vaccineLocation[1];

            int heroX = h.getLocation().x;
            int heroY = h.getLocation().y;
            
            if(cellX > h.getLocation().x  && checkValidMove(heroX+1, heroY)) {
                h.move(Direction.UP);
                return true;
            } else if(cellX < h.getLocation().x && checkValidMove(heroX-1, heroY)) {
                h.move(Direction.DOWN);
                return true;
            } else if(cellY > h.getLocation().y && checkValidMove(heroX, heroY + 1)) {
                h.move(Direction.RIGHT);
                return true;
            } else if(cellY < h.getLocation().y && checkValidMove(heroX, heroY - 1)) {
                h.move(Direction.LEFT);
                return true;
            }	
            
        }

        System.out.println(8);
        // if no action has been done yet, Make 1 move to nearest supply cell if path is safe
        if(!supplyCells.isEmpty()) {

            int[] supplyLocation = findShortestPath(supplyCells, h);

            int cellX = supplyLocation[0];
            int cellY = supplyLocation[1];
            
            int heroX = h.getLocation().x;
            int heroY = h.getLocation().y;
            
            // Make 1 move to nearest supply cell if path is safe
            if(cellX > h.getLocation().x  && checkValidMove(heroX+1, heroY)) {
                h.move(Direction.UP);
                return true;
            } else if(cellX < h.getLocation().x && checkValidMove(heroX-1, heroY)) {
                h.move(Direction.DOWN);
                return true;
            } else if(cellY > h.getLocation().y && checkValidMove(heroX, heroY + 1)) {
                h.move(Direction.RIGHT);
                return true;
            } else if(cellY < h.getLocation().y && checkValidMove(heroX, heroY - 1)) {
                h.move(Direction.LEFT);
                return true;
            }	
        }
        System.out.println(9);

        // if no action has been done yet, Make 1 move to a different location
       
        int heroX = h.getLocation().x;
        int heroY = h.getLocation().y;

        
        
        if((heroX <= 10 && heroX >= 5) && (heroY <= 10 && heroY >= 5)) {
        	return moveRandomDirection(h, Direction.values()); 
        	
        } else if((heroX <= 5 && heroX >= 0) && (heroY <= 5 && heroY >= 0)) {
        	
        	Direction Directions[] = {Direction.UP, Direction.RIGHT};
        	return moveRandomDirection(h, Directions); 
        	
        } else if((heroX <= 15 && heroX >= 10) && (heroY <= 5 && heroY >= 0)) {
        	
        	Direction Directions[] = {Direction.DOWN, Direction.RIGHT};
        	return moveRandomDirection(h, Directions); 
        	
        } else if((heroX <= 5 && heroX >= 0) && (heroY <= 15 && heroY >= 10)) {
        	
        	Direction Directions[] = {Direction.UP, Direction.LEFT};
        	return moveRandomDirection(h, Directions); 
        	
        } else if((heroX <= 15 && heroX >= 10) && (heroY <= 15 && heroY >= 10)) {
        	
        	Direction Directions[] = {Direction.DOWN, Direction.LEFT};
        	return moveRandomDirection(h, Directions); 
        	
        } 
        System.out.println(10);
        return moveRandomDirection(h, Direction.values());
        //return false;

    }

    public static boolean moveRandomDirection(Hero h, Direction[] directions) {
    	
    	int heroX = h.getLocation().x;
        int heroY = h.getLocation().y;
		Random rand = new Random();
		int result;
		int numTries = 0;
		
    	do {
    		result = rand.nextInt(directions.length);
    		
            try {
            	numTries++;
            	h.move(directions[result]);
            } catch (Exception e) {
            	
            }
    		
    	} while(heroX == h.getLocation().x && heroY == h.getLocation().y && numTries < 10);
    	
    	return numTries < 10;
    }

    /**
     * Method to handle Ai turn
     */
    public static void AIturn() throws MovementException, NotEnoughActionsException, NoAvailableResourcesException, InvalidTargetException {

		boolean actionDone = false;
		int failCount = 0;
		
		while (!actionDone) {
			for (Hero h : Game.heroes) {
				try {
					actionDone = decideBestMove(h) || actionDone;
				} catch (Exception e) {
					
				}
				
			}
			failCount++;
			if(failCount > Game.heroes.size()) {
				Game.endTurn();
				return;
			}
		}

	}

}



