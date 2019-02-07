
import java.util.ArrayList;
import java.util.Collections;

public class List {
	/* The list of elements */
	@SuppressWarnings("rawtypes")
	private ArrayList list = new ArrayList();
	
	// Get methods
	
	public Object first() {
		return list.get(0);
	}
	
	public  Object get(int i) {
		return list.get(i);
	}
	
	public int size() {
		return list.size();
	}
	
	// Clear list
	
	public void clear() {
		list.clear();
	}
	
	// Add and sort a new node
	
	@SuppressWarnings("unchecked")
	public void add(Object o) {
		list.add(o);
		Collections.sort(list);
	}

	// Remove method
	
	public void remove(Object o) {
		list.remove(o);
	}
	
	// Contains method
	
	public boolean contains(Object o) {
		return list.contains(o);
	}
	
	public int containsState(Object o){
		DNode nodeAux = (DNode) o;
		
		int keysAux = nodeAux.getKeys(), keysIndex;
		char [][] mapAux = nodeAux.getMap(), mapIndex;
		
		for(int i=0; i<list.size(); i++){
			keysIndex = ((DNode) get(i)).getKeys();
			mapIndex = ((DNode) get(i)).getMap();
			
			if(keysAux == keysIndex){
				for(int j=0; j<mapAux.length; j++){
					for(int k=0; k<mapAux[0].length; k++){
						if(mapAux[j][k] != mapIndex[j][k]){
							j = mapAux.length;
							k= mapAux[0].length;
						}
						else if(mapAux[j][k] == mapIndex[j][k] && j+1 == mapAux.length && k+1 == mapAux[0].length){
								return i;
						}
					}
				}
			}	
		}
		
		return -1;
	}
	
}
