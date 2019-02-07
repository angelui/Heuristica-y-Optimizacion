
public class DNode implements Comparable<Object> {

	private int keys; // Number of keys left
	private int end; // 1 if Node == final state
	private char [][] map;
	private int g; // Cost
	private int h; // Heuristic
	private String path;
	
	// Constructors
	
	public DNode(){
		
	}
	
	public DNode(int keys, int end, char [][] map) {
		this.keys = keys;
		this.end = end;
		this.map = map;
		g = 0;
		h = 0;
		path = "";
	}
	
	public DNode(int keys, int end, char [][] map, int g, int h, String path) {
		this.keys = keys;
		this.end = end;
		this.map = map;
		this.g = g;
		this.h = h;
		this.path = path;
	}
	
	// Compare method to Collections.sort
	
	public int compareTo(Object node) {
		
		DNode aux = (DNode) node;
		
		int first = h + g;
		int second = aux.h + aux.g;
		
		if (first < second) {
			return -1;
		} else if (first > second) {
			return 1;
		} else {
			return 0;
		}
	}
	
	// Get/Set methods

	public int getKeys() {
		return keys;
	}

	public void setKeys(int keys) {
		this.keys = keys;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public char[][] getMap() {
		return map;
	}

	public void setMap(char[][] map) {
		this.map = map;
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}

