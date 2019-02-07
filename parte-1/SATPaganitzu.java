import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.jacop.core.BooleanVar;
import org.jacop.core.Store;
import org.jacop.jasat.utils.structures.IntVec;
import org.jacop.satwrapper.SatWrapper;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;
import org.jacop.search.SimpleSelect;
import org.jacop.search.SmallestDomain;


public class SATPaganitzu {

	//Cuenta las casillas del laberinto y devuelve una matriz de char con las mismas dimensiones
	public static char[][] countFile(String file) throws FileNotFoundException, IOException, InterruptedException {
	      String cadena;
	      int width = 0, height = 0;
	      FileReader f = new FileReader(file);
	      BufferedReader b = new BufferedReader(f);
	      while((cadena = b.readLine())!=null) {
	          width = cadena.length();
	          height++;
	      }
	      b.close();
	      return new char [height][width];
	}
	//Lee el fichero que contiene el laberinto y crea una matriz de char, la cual es devuelta. Cada elemento de la matriz se corresponde con una casilla del laberinto
	public static char[][] readInputMap(String file) throws FileNotFoundException, IOException, InterruptedException {
	      String cadena;
	      char[][] array = countFile(file);
	      FileReader f = new FileReader(file);
	      BufferedReader b = new BufferedReader(f);
	      for(int i = 0; (cadena = b.readLine())!=null; i++)
	    	  array [i] = cadena.toCharArray();
	      b.close();
	      return array;
	}
	//Muestra el laberinto (matriz de char recibida) en la consola
	public static void printMap(char[][] map) {
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[i].length; j++) {
				System.out.print(map[i][j]);
			}
			System.out.println();
		}
		System.out.println("\n"+ "Filas: " + map.length);
		System.out.println("Columnas: " + map[0].length + "\n");
	}
	//Añade una clausula de tipo --> (l1 v l2)
	public static void addClause(SatWrapper satWrapper, int literal1, int literal2){
		IntVec clause = new IntVec(satWrapper.pool);
		clause.add(literal1);
		clause.add(literal2);
		satWrapper.addModelClause(clause.toArray());
	}
	//Añade una clausula de tipo --> (l1 v l2 v l3)
	public static void addClause (SatWrapper satWrapper, int literal1, int literal2, int literal3){
		IntVec clause = new IntVec(satWrapper.pool);
		clause.add(literal1);
		clause.add(literal2);
		clause.add(literal3);
		satWrapper.addModelClause(clause.toArray());
	}
	//Añade una clausula formada por disyunciones de las posibles posiciones de Al
	public static void addClause(SatWrapper satWrapper, int[][] alLiteral, char[][] map) {
		IntVec clause = new IntVec(satWrapper.pool);
		for(int i = 0; i < alLiteral.length; i++)
			for(int j = 0; j < alLiteral[i].length; j++)
				if(map[i][j] == 32)
					clause.add(alLiteral[i][j]);
		satWrapper.addModelClause(clause.toArray());
	}
	//Añade tantas clausulas como serpientes formadas por disyunciones de las posibles posiciones de cada serpiente
	public static void addClause(SatWrapper satWrapper, int[][][] snakesLiteral, char[][] map) {
		IntVec[] clause = new IntVec[snakesLiteral.length];
		for(int i = 0; i < snakesLiteral.length; i++) {
			clause[i] = new IntVec(satWrapper.pool);
			for(int j = 0; j < snakesLiteral[i].length; j++)
				for(int k = 0; k < snakesLiteral[i][j].length; k++)
					if(map[j][k] == 32)
						clause[i].add(snakesLiteral[i][j][k]);
			satWrapper.addModelClause(clause[i].toArray());
		}
	}
	//Actualiza la matriz con las posiciones de Al y las serpientes
	public static void updateMap(char[][] map, BooleanVar[][] al, BooleanVar[][][] snakes) {
		for(int i = 0; i < al.length; i++)
			for(int j = 0; j < al[i].length; j++)
				if(al[i][j].value() == 1) map[i][j] = 'A';
		for(int i = 0; i < snakes.length; i++) {
			for(int j = 0; j < snakes[i].length; j++)
				for(int k = 0; k < snakes[i][j].length; k++)
					if(snakes[i][j][k].value() == 1) map[j][k] = 'S';
		}
	}
	//Abre el archivo, y lo crea si no existe, en el que se escribe el laberinto con Al y las serpientes
	public static void printOutputMap(String file, char[][] map) throws IOException {
      File archivo = new File(file + ".output");
      BufferedWriter bw;
		bw = new BufferedWriter(new FileWriter(archivo));
      for(int i = 0; i < map.length; i++) {
      	for(int j = 0; j < map[i].length; j++) {
      		bw.write(map[i][j]);
      	}
      	bw.write("\n");
      }
          
      bw.close();
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		//Si no se introducen los dos parametros, se aborta la ejecucion
		if(args.length != 2) {
			System.out.println("Error: parametros mal introducidos");
			System.exit(-1);
		}
		char[][] map;	//Matriz en la que se almacena el laberinto
		int numSnakes;
		String mapFile = "";	//Ruta del fichero que contiene el laberinto
		mapFile = args[0];
		map = readInputMap(mapFile);
		numSnakes = Integer.parseInt(args[1]);
		Store store = new Store();
		SatWrapper satWrapper = new SatWrapper(); 
		store.impose(satWrapper);
		
		// 1. DECLARACION DE VARIABLES

		BooleanVar[][] al = new BooleanVar [map.length][map[0].length];
		BooleanVar[][][] snakes = new BooleanVar [numSnakes][map.length][map[0].length];
		BooleanVar[][] emptySpaces = new BooleanVar [map.length][map[0].length];
		
		for(int i = 0; i < al.length; i++)
			for (int j = 0; j < al[i].length; j++)
				al[i][j] = new BooleanVar(store);
		
		for(int i = 0; i < snakes.length; i++)
			for (int j = 0; j < snakes[i].length; j++)
				for (int k = 0; k < snakes[i][j].length; k++)
					snakes[i][j][k] = new BooleanVar(store);
		
		for(int i = 0; i < emptySpaces.length; i++)
			for (int j = 0; j < emptySpaces[i].length; j++)
				emptySpaces[i][j] = new BooleanVar(store);
		
		// Se registran las variables
		for(int i = 0; i < al.length; i++)
			for (int j = 0; j < al[i].length; j++)
				satWrapper.register(al[i][j]);
		
		for(int i = 0; i < snakes.length; i++)
			for (int j = 0; j < snakes[i].length; j++)
				for (int k = 0; k < snakes[i][j].length; k++)
					satWrapper.register(snakes[i][j][k]);
		
		for(int i = 0; i < emptySpaces.length; i++)
			for (int j = 0; j < emptySpaces[i].length; j++)
				satWrapper.register(emptySpaces[i][j]);
		
		// Todas las variables en un unico array para despues invocar al metodo que nos
		// permite resolver el problema
		
		BooleanVar[] allVariables = new BooleanVar[(al.length * al[0].length) * 2 + snakes.length * snakes[0].length * snakes[0][0].length];
		
		int aux = 0;
		for(int i = 0; i < al.length; i++)
			for (int j = 0; j < al[i].length; j++){
				allVariables[aux] = al[i][j];
				aux++;
			}
		for(int i = 0; i < snakes.length; i++)
			for (int j = 0; j < snakes[i].length; j++)
				for (int k = 0; k < snakes[i][j].length; k++){
					allVariables[aux] = snakes[i][j][k];
					aux++;
				}
		for(int i = 0; i < emptySpaces.length; i++)
			for (int j = 0; j < emptySpaces[i].length; j++){
				allVariables[aux] = emptySpaces[i][j];
				aux++;
			}
				
		// 2. DECLARACION DE LOS LITERALES
		
		int[][] alLiteral = new int [map.length][map[0].length];
		int[][][] snakesLiteral = new int [numSnakes][map.length][map[0].length];
		int[][] emptySpacesLiteral = new int [map.length][map[0].length];
		
		for(int i = 0; i < al.length; i++)
			for (int j = 0; j < al[i].length; j++)
				alLiteral[i][j] = satWrapper.cpVarToBoolVar(al[i][j], 1, true);
		
		for(int i = 0; i < snakes.length; i++)
			for (int j = 0; j < snakes[i].length; j++)
				for (int k = 0; k < snakes[i][j].length; k++)
					snakesLiteral[i][j][k] = satWrapper.cpVarToBoolVar(snakes[i][j][k], 1, true);
		
		for(int i = 0; i < emptySpaces.length; i++)
			for (int j = 0; j < emptySpaces[i].length; j++){
				if(map[i][j] == 32)
					emptySpacesLiteral[i][j] = satWrapper.cpVarToBoolVar(emptySpaces[i][j], 1, true);
				else
					emptySpacesLiteral[i][j] = satWrapper.cpVarToBoolVar(emptySpaces[i][j], 0, false);
			}
				
		// 3. RESTRICCIONES
		
		//Al y las serpientes solo se pueden colocar en celdas vacias
		for(int i = 0; i < snakesLiteral.length; i++)
			for (int j = 0; j < snakesLiteral[i].length; j++)
				for(int k = 0; k < snakesLiteral[i][j].length; k++)
					addClause(satWrapper, -emptySpacesLiteral[j][k], alLiteral[j][k], snakesLiteral[i][j][k]);
		
		
		//Una serpiente no puede estar en la misma fila que otra serpiente
		for(int i = 0; i < snakesLiteral.length - 1; i++)
			for(int j = i + 1; j < snakesLiteral.length; j++)
				for (int k = 0; k < snakesLiteral[i].length; k++)
					for (int l = 0; l < snakesLiteral[i][k].length; l++)
						for(int m = 0; m < snakesLiteral[i][k].length; m++)
							addClause(satWrapper, -snakesLiteral[i][k][l], -snakesLiteral[j][k][m]);

		
		//No puede haber ninguna serpiente ni en la misma fila ni en la misma columna que Al
		for(int i = 0; i < snakesLiteral.length; i++)
			for (int j = 0; j < alLiteral.length; j++)
				for(int k = 0; k < alLiteral[j].length; k++)
					for(int l = 0; l < snakesLiteral[i].length; l++)
						for(int m = 0; m < snakesLiteral[i][l].length; m++){
							if(k == m) addClause(satWrapper, -alLiteral[j][k], -snakesLiteral[i][l][m]); 	//Misma columna
							if(j == l) addClause(satWrapper, -alLiteral[j][k], -snakesLiteral[i][l][m]);	//Misma fila
						}
		
		
		//Si Al esta en una posicion no esta en resto
		for(int i = 0; i < alLiteral.length; i++)
			for(int j = 0; j < alLiteral[i].length; j++)
				for(int k = 0; k < alLiteral.length; k++)
					for(int l = 0; l < alLiteral[k].length; l++)
						if(i != k || j != l) addClause(satWrapper, -alLiteral[i][j], -alLiteral[k][l]);
		
		
		//Si S est� en una posicion no esta en resto
		for(int i = 0; i < snakesLiteral.length; i++)
			for(int j = 0; j < snakesLiteral[i].length; j++)
				for(int k = 0; k < snakesLiteral[i][j].length; k++)
					for(int l = 0; l < snakesLiteral[i].length; l++)
						for(int m = 0; m < snakesLiteral[i][l].length; m++)
							if(j != l || k != m) addClause(satWrapper, -snakesLiteral[i][j][k], -snakesLiteral[i][l][m]);
		
		
		//Al tiene que aparecer una vez
		addClause(satWrapper, alLiteral, map);
		
		
		//Una serpiente tiene que aparecer una vez
		addClause(satWrapper, snakesLiteral, map);

		
		// 4. INVOCAR AL SOLUCIONADOR
		
		Search<BooleanVar> search = new DepthFirstSearch<BooleanVar>();
		SelectChoicePoint<BooleanVar> select = new SimpleSelect<BooleanVar>(allVariables,new SmallestDomain<BooleanVar>(), new IndomainMin<BooleanVar>());
		Boolean result = search.labeling(store, select);
		
		if (result) {
			System.out.println("Solution: ");
			System.out.print("Al: ");
			for(int i = 0; i < al.length; i++)
				for(int j = 0; j < al[i].length; j++)
					if(al[i][j].value() == 1) System.out.println(i + "," + j);
			for(int i = 0; i < snakes.length; i++){
				System.out.print("Snake " + i + ": ");
				for(int j = 0; j < snakes[i].length; j++)
					for(int k = 0; k < snakes[i][j].length; k++)
						if(snakes[i][j][k].value() == 1) System.out.println(i + "," + j + "\n");
			}
		} else{
			System.out.println("*** No solution");
		}
		updateMap(map, al, snakes);
		printMap(map);
		printOutputMap(mapFile, map);
	}

}
