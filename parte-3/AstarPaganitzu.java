import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class AstarPaganitzu {

	// Lists and global variables
	
		static List open = new List();  // All node waiting list
		static List closed = new List();  // Expanded node list
		static int heightT = 0;
		static int widthT = 0;
		static int heuristicMode = 0;
		
	// Create char map
		
		static char[][] countFile(String file) throws FileNotFoundException, IOException, InterruptedException {
		      String cadena;
		      int width = 0, height = 0;
		      FileReader f = new FileReader(file);
		      BufferedReader b = new BufferedReader(f);
		      while((cadena = b.readLine())!=null) {
		          width = cadena.length();
		          height++;
		      }
		      b.close();
		      heightT = height;
		      widthT = width;
		      return new char [height][width];
		}
		
	// Filling map	
		
		static char[][] fileToMatrix(String file) throws FileNotFoundException, IOException, InterruptedException {
		      String cadena;
		      char[][] array = countFile(file);
		      FileReader f = new FileReader(file);
		      BufferedReader b = new BufferedReader(f);
		      for(int i = 0; (cadena = b.readLine())!=null; i++)
		    	  array [i] = cadena.toCharArray();
		      b.close();
		      return array;
		}
		
	// Print map	
		
		static void printMap(char[][] map) {
			for(int i = 0; i < map.length; i++) {
				for(int j = 0; j < map[i].length; j++) {
					System.out.print(map[i][j]);
				}
				System.out.println();
			}
		}
		
	//Abre el archivo, y lo crea si no existe, en el que se escribe el laberinto con Al y las serpientes
        public static void printOutputMap(String file, String path, char[][] map) throws IOException {
            File archivo = new File(file + ".output");
            BufferedWriter bw;
            bw = new BufferedWriter(new FileWriter(archivo));
            for(int i = 0; i < map.length; i++) {
                for(int j = 0; j < map[i].length; j++) {
                    bw.write(map[i][j]);
                }
                bw.write("\n");
            }
            bw.write("\n");
            bw.write("Path: " + path + "Finish");
            bw.close();
        }
        
    //Print A* statistics
        public static void printStatistics(String file, long time, int cost, int distance, int nodes) throws IOException {
            File archivo = new File(file + ".statistics");
            BufferedWriter bw;
            bw = new BufferedWriter(new FileWriter(archivo));
            bw.write("Time: " + (time/ 1_000_000_000.0) + " seconds" + "\n");
            bw.write("Total cost: " + cost + "\n");
            bw.write("Path distance: " + distance + "\n");
            bw.write("Expanded nodes: " + nodes + "\n");
            bw.close();
        }	
		
	// Count Keys
		
		static int countKeys(char[][] map){
			int keys = 0;
			
			for(int i = 0; i < map.length; i++) {
				for(int j = 0; j < map[i].length; j++) {
					if(map[i][j] == 'K'){keys++;}
				}
			}
			return keys;
		}
		
		
	// Where is Al?
		
		static int Ax(char[][] map){
			for(int i = 0; i < map.length; i++) {
				for(int j = 0; j < map[i].length; j++) {
					if(map[i][j] == 'A'){return i;}
				}
			}
			return -1;
		}
		
		static int Ay(char[][] map){
			for(int i = 0; i < map.length; i++) {
				for(int j = 0; j < map[i].length; j++) {
					if(map[i][j] == 'A'){return j;}
				}
			}
			return -1;
		}
		
	// Where are the keys?
		
		static int [] Kx(int keys, char[][] map){
			int [] xArray = new int [keys];
			int keysEncountered = 0;
			
			for(int i = 0; i < map.length && keysEncountered != keys; i++) {
				for(int j = 0; j < map[i].length && keysEncountered != keys; j++) {
					if(map[i][j] == 'K'){
						xArray[keysEncountered] = i;
						keysEncountered++;
					}
				}
			}
			return xArray;
		}
		
		static int [] Ky(int keys, char[][] map){
			int [] yArray = new int [keys];
			int keysEncountered = 0;
			
			for(int i = 0; i < map.length && keysEncountered != keys; i++) {
				for(int j = 0; j < map[i].length && keysEncountered != keys; j++) {
					if(map[i][j] == 'K'){
						yArray[keysEncountered] = j;
						keysEncountered++;
					}
				}
			}
			return yArray;
		}
		
	// Where are the snakes?
		
		static int [] Sx(int keys, char[][] map){
			int [] xArray = new int [keys];
			int keysEncountered = 0;
			
			for(int i = 0; i < map.length && keysEncountered != keys; i++) {
				for(int j = 0; j < map[i].length && keysEncountered != keys; j++) {
					if(map[i][j] == 'S'){
						xArray[keysEncountered] = i;
						keysEncountered++;
					}
				}
			}
			return xArray;
		}
		
		static int [] Sy(int keys, char[][] map){
			int [] yArray = new int [keys];
			int keysEncountered = 0;
			
			for(int i = 0; i < map.length && keysEncountered != keys; i++) {
				for(int j = 0; j < map[i].length && keysEncountered != keys; j++) {
					if(map[i][j] == 'S'){
						yArray[keysEncountered] = j;
						keysEncountered++;
					}
				}
			}
			return yArray;
		}
		
	// Where is the exit?
		
		static int Ex(char[][] map){
			for(int i = 0; i < map.length; i++) {
				for(int j = 0; j < map[i].length; j++) {
					if(map[i][j] == 'E'){return i;}
				}
			}
			return -1;
		}
		
		static int Ey(char[][] map){
			for(int i = 0; i < map.length; i++) {
				for(int j = 0; j < map[i].length; j++) {
					if(map[i][j] == 'E'){return j;}
				}
			}
			return -1;
		}
		
		
	// Operators
		
	static void moveDown(int keys, int end, char [][] mapReceived, int g, String path){
		char [][] map = new char [heightT][widthT];
		for(int i = 0; i < mapReceived.length; i++) {
			for(int j = 0; j < mapReceived[i].length; j++) {
				map[i][j] = mapReceived[i][j];
			}
		}
		
		int Ay = Ay(map);
		int Ax = Ax(map);
		
		if(Ax > -1 && Ax+1 < heightT && map[Ax+1][Ay] != '%')
		{
			
			if(map[Ax+1][Ay] == ' ' || map[Ax+1][Ay] == 'K') // Next square = ' ' or 'K'
			{
				
				// Check snakes
				
				int s = 0;
				for(int i=Ay-1; i>-1; i--){ // Check left side
					if(map[Ax+1][i] != ' ' || map[Ax+1][i] != 'K'){
						if(map[Ax+1][i] == 'S'){
							s++;
						}
						else if(map[Ax+1][i] == '%' || map[Ax+1][i] == 'O'){
							i = -1;
						}
					}
				}
				for(int i=Ay+1; i<widthT; i++){ // Check right side
					if(map[Ax+1][i] != ' ' || map[Ax+1][i] != 'K'){
						if(map[Ax+1][i] == 'S'){
							s++;
						}
						else if(map[Ax+1][i] == '%' || map[Ax+1][i] == 'O'){
							i = widthT;
						}
					}
				}
				
				if(s == 0){
					if(map[Ax+1][Ay] == 'K'){keys--;}
					map[Ax+1][Ay] = 'A';
					map[Ax][Ay] = ' ';
					g = g + 2;
				}
			}
			
			else if(map[Ax+1][Ay] == 'O') // Next square = 'O'
			{
				if(Ax+2 < map.length){
					if(map[Ax+2][Ay] == ' '){
						
						// Check snakes
						
						int s = 0;
						for(int i=Ay-1; i>-1; i--){ // Check left side
							if(map[Ax+1][i] != ' ' || map[Ax+1][i] != 'K'){
								if(map[Ax+1][i] == 'S'){
									s++;
								}
								else if(map[Ax+1][i] == '%' || map[Ax+1][i] == 'O'){
									i = -1;
								}
							}
						}
						for(int i=Ay+1; i<widthT; i++){ // Check right side
							if(map[Ax+1][i] != ' ' || map[Ax+1][i] != 'K'){
								if(map[Ax+1][i] == 'S'){
									s++;
								}
								else if(map[Ax+1][i] == '%' || map[Ax+1][i] == 'O'){
									i = widthT;
								}
							}
						}
						
						if(s == 0){
							map[Ax+2][Ay] = 'O';
							map[Ax+1][Ay] = 'A';
							map[Ax][Ay] = ' ';
							g = g + 4;
						}
					}
				}
			}
			
			else if(map[Ax+1][Ay] == 'E' && keys == 0) // Next square = 'E' ande keys == 0
			{
				end = 1;
				map[Ax+1][Ay] = 'A';
				map[Ax][Ay] = ' ';
				g = g + 2;
			}
			
			String pathNew = path + "("+Ax(map)+","+Ay(map)+") -> ";
			DNode nodeAux = new DNode(keys, end, map, g, heuristic(keys, map), pathNew);
			int openContains = open.containsState(nodeAux);
			int closedContains = closed.containsState(nodeAux);
			
			if(openContains == -1 && closedContains == -1){ // If contains == false, add node
				open.add(nodeAux); // Adding sucesor to open list
			}
			
			else if(openContains > -1){
				DNode nodeContains = (DNode) open.get(openContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					open.remove(nodeContains);
					open.add(nodeAux);
				}
			}
			
			else if(closedContains > -1){ // If node is already in closed list
				DNode nodeContains = (DNode) closed.get(closedContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					closed.remove(nodeContains);
					open.add(nodeAux);
				}
			}
		}
	}
	
	static void moveUp(int keys, int end, char [][] mapReceived, int g, String path){
		char [][] map = new char [heightT][widthT];
		for(int i = 0; i < mapReceived.length; i++) {
			for(int j = 0; j < mapReceived[i].length; j++) {
				map[i][j] = mapReceived[i][j];
			}
		}
		
		int Ay = Ay(map);
		int Ax = Ax(map);
		
		if(Ax-1 > -1 && Ax < heightT && map[Ax-1][Ay] != '%')
		{
			
			if(map[Ax-1][Ay] == ' ' || map[Ax-1][Ay] == 'K') // Next square = ' ' or 'K'
			{
				
				// Check snakes
				
				int s = 0;
				for(int i=Ay-1; i>-1; i--){ // Check left side
					if(map[Ax-1][i] != ' ' || map[Ax-1][i] != 'K'){
						if(map[Ax-1][i] == 'S'){
							s++;
						}
						else if(map[Ax-1][i] == '%' || map[Ax-1][i] == 'O'){
							i = -1;
						}
					}
				}
				for(int i=Ay+1; i<widthT; i++){ // Check right side
					if(map[Ax-1][i] != ' ' || map[Ax-1][i] != 'K'){
						if(map[Ax-1][i] == 'S'){
							s++;
						}
						else if(map[Ax-1][i] == '%' || map[Ax-1][i] == 'O'){
							i = widthT;
						}
					}
				}
				
				if(s == 0){
					if(map[Ax-1][Ay] == 'K'){keys--;}
					map[Ax-1][Ay] = 'A';
					map[Ax][Ay] = ' ';
					g = g + 2;
				}
			}
			
			else if(map[Ax-1][Ay] == 'O') // Next square = 'O'
			{
				if(Ax-2 < map.length){
					if(map[Ax-2][Ay] == ' '){

						// Check snakes
						
						int s = 0;
						for(int i=Ay-1; i>-1; i--){ // Check left side
							if(map[Ax-1][i] != ' ' || map[Ax-1][i] != 'K'){
								if(map[Ax-1][i] == 'S'){
									s++;
								}
								else if(map[Ax-1][i] == '%' || map[Ax-1][i] == 'O'){
									i = -1;
								}
							}
						}
						for(int i=Ay+1; i<widthT; i++){ // Check right side
							if(map[Ax-1][i] != ' ' || map[Ax-1][i] != 'K'){
								if(map[Ax-1][i] == 'S'){
									s++;
								}
								else if(map[Ax-1][i] == '%' || map[Ax-1][i] == 'O'){
									i = widthT;
								}
							}
						}
						
						if(s == 0){map[Ax-2][Ay] = 'O';
							map[Ax-1][Ay] = 'A';
							map[Ax][Ay] = ' ';
							g = g + 4;
						}
						
					}
				}
			}
			
			else if(map[Ax-1][Ay] == 'E' && keys == 0) // Next square = 'E' and keys == 0
			{
				end = 1;
				map[Ax-1][Ay] = 'A';
				map[Ax][Ay] = ' ';
				g = g + 2;
			}
			
			String pathNew = path + "("+Ax(map)+","+Ay(map)+") -> ";
			DNode nodeAux = new DNode(keys, end, map, g, heuristic(keys, map), pathNew);
			int openContains = open.containsState(nodeAux);
			int closedContains = closed.containsState(nodeAux);
			
			if(openContains == -1 && closedContains == -1){ // If contains == false, add node
				open.add(nodeAux); // Adding sucesor to open list
			}
			
			else if(openContains > -1){
				DNode nodeContains = (DNode) open.get(openContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					open.remove(nodeContains);
					open.add(nodeAux);
				}
			}
			
			else if(closedContains > -1){ // If node is already in closed list
				DNode nodeContains = (DNode) closed.get(closedContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					closed.remove(nodeContains);
					open.add(nodeAux);
				}
			}
		}
	}

	static void moveLeft(int keys, int end, char [][] mapReceived, int g, String path){
		char [][] map = new char [heightT][widthT];
		for(int i = 0; i < mapReceived.length; i++) {
			for(int j = 0; j < mapReceived[i].length; j++) {
				map[i][j] = mapReceived[i][j];
			}
		}
		
		int Ay = Ay(map);
		int Ax = Ax(map);
		
		if(Ay-1 > -1 && Ay < widthT && map[Ax][Ay-1] != '%')
		{

			if(map[Ax][Ay-1] == ' ' || map[Ax][Ay-1] == 'K') // Next square = ' ' or 'K'
			{
				if(map[Ax][Ay-1] == 'K'){keys--;}	// we don't need to check about snakes
				map[Ax][Ay-1] = 'A';
				map[Ax][Ay] = ' ';
				g = g + 2;
			}
			
			else if(map[Ax][Ay-1] == 'O') // Next square = 'O'
			{
				if(Ay-2 < map[0].length){
					if(map[Ax][Ay-2] == ' '){
						map[Ax][Ay-2] = 'O';
						map[Ax][Ay-1] = 'A';
						map[Ax][Ay] = ' ';
						g = g + 4;
					}
				}
			}
			
			else if(map[Ax][Ay-1] == 'E' && keys == 0) // Next square = 'E' and keys == 0
			{
				end = 1;
				map[Ax][Ay-1] = 'A';
				map[Ax][Ay] = ' ';
				g = g + 2;
			}
			
			String pathNew = path + "("+Ax(map)+","+Ay(map)+") -> ";
			DNode nodeAux = new DNode(keys, end, map, g, heuristic(keys, map), pathNew);
			int openContains = open.containsState(nodeAux);
			int closedContains = closed.containsState(nodeAux);
			
			if(openContains == -1 && closedContains == -1){ // If contains == false, add node
				open.add(nodeAux); // Adding sucesor to open list
			}
			
			else if(openContains > -1){
				DNode nodeContains = (DNode) open.get(openContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					open.remove(nodeContains);
					open.add(nodeAux);
				}
			}
			
			else if(closedContains > -1){ // If node is already in closed list
				DNode nodeContains = (DNode) closed.get(closedContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					closed.remove(nodeContains);
					open.add(nodeAux);
				}
			}
		}
	}
	
	static void moveRight(int keys, int end, char [][] mapReceived, int g, String path){
		char [][] map = new char [heightT][widthT];
		for(int i = 0; i < mapReceived.length; i++) {
			for(int j = 0; j < mapReceived[i].length; j++) {
				map[i][j] = mapReceived[i][j];
			}
		}
		
		int Ay = Ay(map);
		int Ax = Ax(map);
		
		if(Ay > -1 && Ay+1 < widthT && map[Ax][Ay+1] != '%')
		{
			
			if(map[Ax][Ay+1] == ' ' || map[Ax][Ay+1] == 'K') // Next square = ' ' or 'K'
			{
				if(map[Ax][Ay+1] == 'K'){keys--;}	// we don't need to check about snakes
				map[Ax][Ay+1] = 'A';
				map[Ax][Ay] = ' ';
				g = g + 2;
			}
			
			else if(map[Ax][Ay+1] == 'O') // Next square = 'O'
			{
				if(Ay+2 < map[0].length){
					if(map[Ax][Ay+2] == ' '){
						map[Ax][Ay+2] = 'O';
						map[Ax][Ay+1] = 'A';
						map[Ax][Ay] = ' ';
						g = g + 4;
					}
				}
			}
			
			else if(map[Ax][Ay+1] == 'E' && keys == 0) // Next square = 'E' and keys == 0
			{
				end = 1;
				map[Ax][Ay+1] = 'A';
				map[Ax][Ay] = ' ';
				g = g + 2;
			}

			String pathNew = path + "("+Ax(map)+","+Ay(map)+") -> ";
			DNode nodeAux = new DNode(keys, end, map, g, heuristic(keys, map), pathNew);
			int openContains = open.containsState(nodeAux);
			int closedContains = closed.containsState(nodeAux);
			
			if(openContains == -1 && closedContains == -1){ // If contains == false, add node
				open.add(nodeAux); // Adding sucesor to open list
			}
			
			else if(openContains > -1){ // If node is already in open list
				DNode nodeContains = (DNode) open.get(openContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					open.remove(nodeContains);
					open.add(nodeAux);
				}
			}
			
			else if(closedContains > -1){ // If node is already in closed list
				DNode nodeContains = (DNode) closed.get(closedContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					closed.remove(nodeContains);
					open.add(nodeAux);
				}
			}
		}
	}
	
	// Diagonal operators
	
	static void moveDownRight(int keys, int end, char [][] mapReceived, int g, String path){
		char [][] map = new char [heightT][widthT];
		for(int i = 0; i < mapReceived.length; i++) {
			for(int j = 0; j < mapReceived[i].length; j++) {
				map[i][j] = mapReceived[i][j];
			}
		}
		
		int Ay = Ay(map);
		int Ax = Ax(map);
		
		if((Ax > -1 && Ax+1 < heightT && map[Ax+1][Ay+1] != '%') && (Ay > -1 && Ay+1 < widthT && map[Ax+1][Ay+1] != '%'))
		{
			
			if(map[Ax+1][Ay+1] == ' ' || map[Ax+1][Ay+1] == 'K') // Next square = ' ' or 'K'
			{
				
				// Check snakes
				
				int s = 0;
				for(int i=Ay; i>-1; i--){ // Check left side
					if(map[Ax+1][i] != ' ' || map[Ax+1][i] != 'K'){
						if(map[Ax+1][i] == 'S'){
							s++;
						}
						else if(map[Ax+1][i] == '%' || map[Ax+1][i] == 'O'){
							i = -1;
						}
					}
				}
				for(int i=Ay+1; i<widthT; i++){ // Check right side
					if(map[Ax+1][i] != ' ' || map[Ax+1][i] != 'K'){
						if(map[Ax+1][i] == 'S'){
							s++;
						}
						else if(map[Ax+1][i] == '%' || map[Ax+1][i] == 'O'){
							i = widthT;
						}
					}
				}
				
				if(s == 0){
					if(map[Ax+1][Ay+1] == 'K'){keys--;}
					map[Ax+1][Ay+1] = 'A';
					map[Ax][Ay] = ' ';
					g = g + 2;
				}
			}
			
			else if(map[Ax+1][Ay+1] == 'O') // Next square = 'O'
			{
				if(Ax+2 < map.length){
					if(map[Ax+2][Ay+2] == ' '){
						
						// Check snakes
						
						int s = 0;
						for(int i=Ay; i>-1; i--){ // Check left side
							if(map[Ax+1][i] != ' ' || map[Ax+1][i] != 'K'){
								if(map[Ax+1][i] == 'S'){
									s++;
								}
								else if(map[Ax+1][i] == '%' || map[Ax+1][i] == 'O'){
									i = -1;
								}
							}
						}
						for(int i=Ay+1; i<widthT; i++){ // Check right side
							if(map[Ax+1][i] != ' ' || map[Ax+1][i] != 'K'){
								if(map[Ax+1][i] == 'S'){
									s++;
								}
								else if(map[Ax+1][i] == '%' || map[Ax+1][i] == 'O'){
									i = widthT;
								}
							}
						}
						
						if(s == 0){
							map[Ax+2][Ay+2] = 'O';
							map[Ax+1][Ay+1] = 'A';
							map[Ax][Ay] = ' ';
							g = g + 4;
						}
					}
				}
			}
			
			else if(map[Ax+1][Ay+1] == 'E' && keys == 0) // Next square = 'E' and keys == 0
			{
				end = 1;
				map[Ax+1][Ay+1] = 'A';
				map[Ax][Ay] = ' ';
				g = g + 2;
			}
			
			String pathNew = path + "("+Ax(map)+","+Ay(map)+") -> ";
			DNode nodeAux = new DNode(keys, end, map, g, heuristic(keys, map), pathNew);
			int openContains = open.containsState(nodeAux);
			int closedContains = closed.containsState(nodeAux);
			
			if(openContains == -1 && closedContains == -1){ // If contains == false, add node
				open.add(nodeAux); // Adding sucesor to open list
			}
			
			else if(openContains > -1){
				DNode nodeContains = (DNode) open.get(openContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					open.remove(nodeContains);
					open.add(nodeAux);
				}
			}
			
			else if(closedContains > -1){ // If node is already in closed list
				DNode nodeContains = (DNode) closed.get(closedContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					closed.remove(nodeContains);
					open.add(nodeAux);
				}
			}
		}
	}
	
	static void moveUpRight(int keys, int end, char [][] mapReceived, int g, String path){
		char [][] map = new char [heightT][widthT];
		for(int i = 0; i < mapReceived.length; i++) {
			for(int j = 0; j < mapReceived[i].length; j++) {
				map[i][j] = mapReceived[i][j];
			}
		}
		
		int Ay = Ay(map);
		int Ax = Ax(map);

		if((Ax-1 > -1 && Ax < heightT && map[Ax+1][Ay+1] != '%') && (Ay > -1 && Ay+1 < widthT && map[Ax+1][Ay+1] != '%'))
		{
			
			if(map[Ax-1][Ay+1] == ' ' || map[Ax-1][Ay+1] == 'K') // Next square = ' ' or 'K'
			{
				
				// Check snakes
				
				int s = 0;
				for(int i=Ay; i>-1; i--){ // Check left side
					if(map[Ax-1][i] != ' ' || map[Ax-1][i] != 'K'){
						if(map[Ax-1][i] == 'S'){
							s++;
						}
						else if(map[Ax-1][i] == '%' || map[Ax-1][i] == 'O'){
							i = -1;
						}
					}
				}
				for(int i=Ay+1; i<widthT; i++){ // Check right side
					if(map[Ax-1][i] != ' ' || map[Ax-1][i] != 'K'){
						if(map[Ax-1][i] == 'S'){
							s++;
						}
						else if(map[Ax-1][i] == '%' || map[Ax-1][i] == 'O'){
							i = widthT;
						}
					}
				}
				
				if(s == 0){
					if(map[Ax-1][Ay+1] == 'K'){keys--;}
					map[Ax-1][Ay+1] = 'A';
					map[Ax][Ay] = ' ';
					g = g + 2;
				}
			}
			
			else if(map[Ax-1][Ay+1] == 'O') // Next square = 'O'
			{
				if(Ax-2 < map.length){
					if(map[Ax-2][Ay+2] == ' '){

						// Check snakes
						
						int s = 0;
						for(int i=Ay; i>-1; i--){ // Check left side
							if(map[Ax-1][i] != ' ' || map[Ax-1][i] != 'K'){
								if(map[Ax-1][i] == 'S'){
									s++;
								}
								else if(map[Ax-1][i] == '%' || map[Ax-1][i] == 'O'){
									i = -1;
								}
							}
						}
						for(int i=Ay+1; i<widthT; i++){ // Check right side
							if(map[Ax-1][i] != ' ' || map[Ax-1][i] != 'K'){
								if(map[Ax-1][i] == 'S'){
									s++;
								}
								else if(map[Ax-1][i] == '%' || map[Ax-1][i] == 'O'){
									i = widthT;
								}
							}
						}
						
						if(s == 0){map[Ax-2][Ay+2] = 'O';
							map[Ax-1][Ay+1] = 'A';
							map[Ax][Ay] = ' ';
							g = g + 4;
						}
						
					}
				}
			}
			
			else if(map[Ax-1][Ay+1] == 'E' && keys == 0) // Next square = 'E' and keys == 0
			{
				end = 1;
				map[Ax-1][Ay+1] = 'A';
				map[Ax][Ay] = ' ';
				g = g + 2;
			}
			
			String pathNew = path + "("+Ax(map)+","+Ay(map)+") -> ";
			DNode nodeAux = new DNode(keys, end, map, g, heuristic(keys, map), pathNew);
			int openContains = open.containsState(nodeAux);
			int closedContains = closed.containsState(nodeAux);
			
			if(openContains == -1 && closedContains == -1){ // If contains == false, add node
				open.add(nodeAux); // Adding sucesor to open list
			}
			
			else if(openContains > -1){
				DNode nodeContains = (DNode) open.get(openContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					open.remove(nodeContains);
					open.add(nodeAux);
				}
			}
			
			else if(closedContains > -1){ // If node is already in closed list
				DNode nodeContains = (DNode) closed.get(closedContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					closed.remove(nodeContains);
					open.add(nodeAux);
				}
			}
		}
	}
	static void moveDownLeft(int keys, int end, char [][] mapReceived, int g, String path){
		char [][] map = new char [heightT][widthT];
		for(int i = 0; i < mapReceived.length; i++) {
			for(int j = 0; j < mapReceived[i].length; j++) {
				map[i][j] = mapReceived[i][j];
			}
		}
		
		int Ay = Ay(map);
		int Ax = Ax(map);

		if((Ax > -1 && Ax+1 < heightT && map[Ax+1][Ay+1] != '%') && (Ay-1 > -1 && Ay < widthT && map[Ax+1][Ay+1] != '%'))
		{
			
			if(map[Ax+1][Ay-1] == ' ' || map[Ax+1][Ay-1] == 'K') // Next square = ' ' or 'K'
			{
				
				// Check snakes
				
				int s = 0;
				for(int i=Ay; i>-1; i--){ // Check left side
					if(map[Ax+1][i] != ' ' || map[Ax+1][i] != 'K'){
						if(map[Ax+1][i] == 'S'){
							s++;
						}
						else if(map[Ax+1][i] == '%' || map[Ax+1][i] == 'O'){
							i = -1;
						}
					}
				}
				for(int i=Ay+1; i<widthT; i++){ // Check right side
					if(map[Ax+1][i] != ' ' || map[Ax+1][i] != 'K'){
						if(map[Ax+1][i] == 'S'){
							s++;
						}
						else if(map[Ax+1][i] == '%' || map[Ax+1][i] == 'O'){
							i = widthT;
						}
					}
				}
				
				if(s == 0){
					if(map[Ax+1][Ay-1] == 'K'){keys--;}
					map[Ax+1][Ay-1] = 'A';
					map[Ax][Ay] = ' ';
					g = g + 2;
				}
			}
			
			else if(map[Ax+1][Ay-1] == 'O') // Next square = 'O'
			{
				if(Ax+2 < map.length){
					if(map[Ax+2][Ay-2] == ' '){
						
						// Check snakes
						
						int s = 0;
						for(int i=Ay; i>-1; i--){ // Check left side
							if(map[Ax+1][i] != ' ' || map[Ax+1][i] != 'K'){
								if(map[Ax+1][i] == 'S'){
									s++;
								}
								else if(map[Ax+1][i] == '%' || map[Ax+1][i] == 'O'){
									i = -1;
								}
							}
						}
						for(int i=Ay+1; i<widthT; i++){ // Check right side
							if(map[Ax+1][i] != ' ' || map[Ax+1][i] != 'K'){
								if(map[Ax+1][i] == 'S'){
									s++;
								}
								else if(map[Ax+1][i] == '%' || map[Ax+1][i] == 'O'){
									i = widthT;
								}
							}
						}
						
						if(s == 0){
							map[Ax+2][Ay-2] = 'O';
							map[Ax+1][Ay-1] = 'A';
							map[Ax][Ay] = ' ';
							g = g + 4;
						}
					}
				}
			}
			
			else if(map[Ax+1][Ay-1] == 'E' && keys == 0) // Next square = 'E' ande keys == 0
			{
				end = 1;
				map[Ax+1][Ay-1] = 'A';
				map[Ax][Ay] = ' ';
				g = g + 2;
			}
			
			String pathNew = path + "("+Ax(map)+","+Ay(map)+") -> ";
			DNode nodeAux = new DNode(keys, end, map, g, heuristic(keys, map), pathNew);
			int openContains = open.containsState(nodeAux);
			int closedContains = closed.containsState(nodeAux);
			
			if(openContains == -1 && closedContains == -1){ // If contains == false, add node
				open.add(nodeAux); // Adding sucesor to open list
			}
			
			else if(openContains > -1){
				DNode nodeContains = (DNode) open.get(openContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					open.remove(nodeContains);
					open.add(nodeAux);
				}
			}
			
			else if(closedContains > -1){ // If node is already in closed list
				DNode nodeContains = (DNode) closed.get(closedContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					closed.remove(nodeContains);
					open.add(nodeAux);
				}
			}
		}
	}
	
	static void moveUpLeft(int keys, int end, char [][] mapReceived, int g, String path){
		char [][] map = new char [heightT][widthT];
		for(int i = 0; i < mapReceived.length; i++) {
			for(int j = 0; j < mapReceived[i].length; j++) {
				map[i][j] = mapReceived[i][j];
			}
		}
		
		int Ay = Ay(map);
		int Ax = Ax(map);

		if((Ax-1 > -1 && Ax < heightT && map[Ax+1][Ay+1] != '%') && (Ay-1 > -1 && Ay < widthT && map[Ax+1][Ay+1] != '%'))
		{
			
			if(map[Ax-1][Ay-1] == ' ' || map[Ax-1][Ay-1] == 'K') // Next square = ' ' or 'K'
			{
				
				// Check snakes
				
				int s = 0;
				for(int i=Ay; i>-1; i--){ // Check left side
					if(map[Ax-1][i] != ' ' || map[Ax-1][i] != 'K'){
						if(map[Ax-1][i] == 'S'){
							s++;
						}
						else if(map[Ax-1][i] == '%' || map[Ax-1][i] == 'O'){
							i = -1;
						}
					}
				}
				for(int i=Ay+1; i<widthT; i++){ // Check right side
					if(map[Ax-1][i] != ' ' || map[Ax-1][i] != 'K'){
						if(map[Ax-1][i] == 'S'){
							s++;
						}
						else if(map[Ax-1][i] == '%' || map[Ax-1][i] == 'O'){
							i = widthT;
						}
					}
				}
				
				if(s == 0){
					if(map[Ax-1][Ay-1] == 'K'){keys--;}
					map[Ax-1][Ay-1] = 'A';
					map[Ax][Ay] = ' ';
					g = g + 2;
				}
			}
			
			else if(map[Ax-1][Ay-1] == 'O') // Next square = 'O'
			{
				if(Ax-2 < map.length){
					if(map[Ax-2][Ay-2] == ' '){

						// Check snakes
						
						int s = 0;
						for(int i=Ay; i>-1; i--){ // Check left side
							if(map[Ax-1][i] != ' ' || map[Ax-1][i] != 'K'){
								if(map[Ax-1][i] == 'S'){
									s++;
								}
								else if(map[Ax-1][i] == '%' || map[Ax-1][i] == 'O'){
									i = -1;
								}
							}
						}
						for(int i=Ay+1; i<widthT; i++){ // Check right side
							if(map[Ax-1][i] != ' ' || map[Ax-1][i] != 'K'){
								if(map[Ax-1][i] == 'S'){
									s++;
								}
								else if(map[Ax-1][i] == '%' || map[Ax-1][i] == 'O'){
									i = widthT;
								}
							}
						}
						
						if(s == 0){map[Ax-2][Ay-2] = 'O';
							map[Ax-1][Ay-1] = 'A';
							map[Ax][Ay] = ' ';
							g = g + 4;
						}
						
					}
				}
			}
			
			else if(map[Ax-1][Ay-1] == 'E' && keys == 0) // Next square = 'E' and keys == 0
			{
				end = 1;
				map[Ax-1][Ay-1] = 'A';
				map[Ax][Ay] = ' ';
				g = g + 2;
			}
			
			String pathNew = path + "("+Ax(map)+","+Ay(map)+") -> ";
			DNode nodeAux = new DNode(keys, end, map, g, heuristic(keys, map), pathNew);
			int openContains = open.containsState(nodeAux);
			int closedContains = closed.containsState(nodeAux);
			
			if(openContains == -1 && closedContains == -1){ // If contains == false, add node
				open.add(nodeAux); // Adding sucesor to open list
			}
			
			else if(openContains > -1){
				DNode nodeContains = (DNode) open.get(openContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					open.remove(nodeContains);
					open.add(nodeAux);
				}
			}
			
			else if(closedContains > -1){ // If node is already in closed list
				DNode nodeContains = (DNode) closed.get(closedContains);
				
				if(nodeContains.getG() + nodeContains.getH() > nodeAux.getG() + nodeAux.getH()){
					closed.remove(nodeContains);
					open.add(nodeAux);
				}
			}
		}
	}
	
		
	// Heuristics
	
	static int heuristic(int keys, char [][] map){
		int value = 0;
		
		if(heuristicMode == 3){
			
			// Position of keys
			
			int [] xArray = Kx(keys, map);
			int [] yArray = Ky(keys, map);
			
			// Position of exit
			
			int Ex = Ex(map);
			int Ey = Ey(map);
			
			// Position of A
			
			int Ax = Ax(map);
			int Ay = Ay(map);
			
			int distTotalKeys = -1, distMinExit = -1;
			
			for(int i=0; i<keys; i++){
				
				// Distance key-A
				
				int distActualKey = Math.abs(xArray[i]-Ax)+Math.abs(yArray[i]-Ay);
				if((distActualKey < distTotalKeys) || (distTotalKeys == -1)){
					distTotalKeys += distActualKey;
				}
				
				// Distance key-E
				
				int distActualExit = Math.abs(Ex-xArray[i])+Math.abs(Ey-yArray[i]);
				if((distActualExit < distMinExit) || (distMinExit == -1)){
					distMinExit += distActualExit;
				}
			}
			return (distTotalKeys+distMinExit);
		}
		
		return value;
		
	}
	
	// Path distance

	public static int pathDistance(String path){
		int pathDistance = 0;
		
		for(int i=0; i<path.length(); i++){
			if(path.charAt(i) == ','){pathDistance++;}
		}
		
		return pathDistance;
	}
	
	// ** MAIN ** //

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		
		// Charge map
		
		if(args.length != 2) {
			System.out.println("Error: parametros introducidos no son correctos");
		}
		char[][] map = fileToMatrix(args[0]);
		
		// Some variables
		
		heuristicMode = Integer.parseInt(args[1]);
		boolean success = false;
		long startTime = System.nanoTime(); // Time

		open.clear();
		closed.clear();
		
		// First add
		
		String pathIni = "("+Ax(map)+","+Ay(map)+") -> ";
		DNode nodeIni = new DNode(countKeys(map), 0, map, 0, heuristic(countKeys(map), map), pathIni); // node(Keys, End, Map, Cost, Heuristic, Path)
		open.add(nodeIni);
		DNode nodeAux = new DNode();

		// Iterations
		
		do{
			nodeAux = (DNode) open.first();
			if(nodeAux.getEnd() == 0){
			
				// Node values
				
				int keysFirst = nodeAux.getKeys();
				int endFirst = nodeAux.getEnd();
				char [][] mapFirst = nodeAux.getMap();
				int gFirst = nodeAux.getG();
				String pathFirst = nodeAux.getPath();
				
				//printMap(nodeAux.getMap());
				
				closed.add(nodeAux); // Adding to expanded nodes list
				open.remove(nodeAux); // Removing from all nodes list
				
				// Using operators to make nodes
				
				moveRight(keysFirst, endFirst, mapFirst, gFirst, pathFirst);
				moveLeft(keysFirst, endFirst, mapFirst, gFirst, pathFirst);
				moveUp(keysFirst, endFirst, mapFirst, gFirst, pathFirst);
				moveDown(keysFirst, endFirst, mapFirst, gFirst, pathFirst);
				
				moveDownRight(keysFirst, endFirst, mapFirst, gFirst, pathFirst);
				moveUpRight(keysFirst, endFirst, mapFirst, gFirst, pathFirst);
				moveDownLeft(keysFirst, endFirst, mapFirst, gFirst, pathFirst);
				moveUpLeft(keysFirst, endFirst, mapFirst, gFirst, pathFirst);
			}
			else{ // End == true (solution founded)
				success = true;
				printOutputMap(args[0], nodeAux.getPath() , map);
				
				// Time
				
				long endTime   = System.nanoTime();
				long totalTime = endTime - startTime;
				printStatistics(args[0], totalTime, nodeAux.getG(), pathDistance(nodeAux.getPath()), closed.size());
			}
				
		}while(open.size() != 0 && success == false);
		
		if(open.size() == 0){
			System.out.println("No found solution"); // Solution not founded
		}
	}

}
