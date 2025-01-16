/**
 * FibonacciHeap
 *
 * An implementation of Fibonacci heap over positive integers.
 *
 */
public class FibonacciHeap
{
	private int size = 0;
	private int numOfTrees = 0;
	private int numOfLinks = 0;
	private int numOfCuts = 0;

	public HeapNode min = null;


	/**
	 * Inner function:
	 * Constructor of FibonacciHeap initialized with node**/
	public FibonacciHeap(HeapNode node){
		this.size = 1;
		this.numOfTrees = 1;
		this.min = node;
	}
	
	/**
	 *
	 * Constructor to initialize an empty heap.
	 *
	 */
	public FibonacciHeap()
	{
		// should be replaced by student code
	}

	/**
	 * 
	 * pre: key > 0
	 * Insert (key,info) into the heap and return the newly generated HeapNode.
	 *
	 */
	public HeapNode insert(int key, String info) 
	{    
		HeapNode newNode = new HeapNode(key, info);
		FibonacciHeap fibHeap = new FibonacciHeap(newNode);
		this.meld(fibHeap);
		return newNode;
	}

	/**
	 * 
	 * Return the minimal HeapNode, null if empty.
	 *
	 */
	public HeapNode findMin()
	{
		return min; // should be replaced by student code
	}

	/**
	 * 
	 * Delete the minimal item
	 *
	 */
	// Overload deleteMin() to enable the use of a boolean parameter to support regular delete
	public void deleteMin(boolean b)
	{
		this.numOfCuts += this.min.rank;
		HeapNode min = this.findMin();
		HeapNode prev = min.prev;
		HeapNode next = min.next;

		if (min.child == null)  // min doesn't have children
		{
			// min has one or more siblings:
			if (prev != min && next != min)
			{
				prev.next = next;
				next.prev = prev;
				this.numOfTrees --;
			}
			else  // min has no siblings and no children
			{
				this.size = 0;
				this.numOfTrees = 0;
				this.min = null;
				// numOfCuts & numOflinks remain untouched for memory
			}
		}
		else // min has children:
		{
			// Take care of the parent-child relationship
			HeapNode child = min.child;
			child.parent = null; // detach children from min
			addToRoots(min.child, false);  // Add the children to the roots chain
			min.child = null;  // Detach min from its child
		}
		min.next = null;  // detach min from other nodes to ensure clean cut
		min.prev = null;

		// Successive linking - as numOfTrees may be larger than log(n) due to lazy inserts/melds
		// Enter code for successive linking
		///////////
		///////////
		/////////////

		// Find the new min after successive linking - roots length is bounded by log(n)
		HeapNode curr = prev;
		this.min = curr;
		while (curr.next != prev){
			curr = curr.next;
			if (curr.key < this.min.key)
			{
				this.min = curr;
			}
		}
	}

//		// Take care of the parent-child relationship
//		if (min.child != null){
//			HeapNode child = min.child;
//			if (min.parent != null)  // 'min' is not a root
//			{  // Bypass
//				HeapNode parent = min.parent;
//				if (next != min){  // min has brothers
//
//				}
//				parent.child = child;
//				child.parent = parent;
//			}
//			else  // There is a child but not parent - meaning the deleted node is indeed a root (but might not be the real min)
//			{
//				child.parent = null; // detach children from min
//			}
//			min.child = null;  // Detach min from its child
//		}
//		if (min.parent != null) // min is a leaf and not a root
//		{
//
//		}
//		min.next = null;  // detach min from other nodes to ensure clean cut
//		min.prev = null;
//		return; // should be replaced by student code
//	}


	public void deleteMin()
	{
		deleteMin(true);
	}

	/**
	 * 
	 * pre: 0<diff<x.key
	 * Decrease the key of x by diff and fix the heap. 
	 * 
	 */
	public void decreaseKey(HeapNode x, int diff) 
	{    
		assert x.key - diff > 0;
		x.key -= diff;
		if (x.key < this.min.key) { // Updating minimum node if needed
			this.min = x;
		}

		// Invariant doesn't preserved
		if ((x.parent != null) && (x.key < x.parent.key)) {
			this.cascadingCut(x, x.parent);
		}
		// else - Invariant is preserved and no cuts needed

	}

	/**
	 * 
	 * Delete the x from the heap.
	 *
	 */
	public void delete(HeapNode x) 
	{    
		return; // should be replaced by student code
	}


	/**
	 * 
	 * Return the total number of links.
	 * 
	 */
	public int totalLinks()
	{
		return this.numOfLinks; // should be replaced by student code
	}


	/**
	 * 
	 * Return the total number of cuts.
	 * 
	 */
	public int totalCuts()
	{
		return this.numOfCuts; // should be replaced by student code
	}


	/**
	 * 
	 * Meld the heap with heap2
	 *
	 */
	public void meld(FibonacciHeap heap2)
	{
		addToRoots(heap2.min, true); // Concatenating the heap to the list of roots by the minimum node
		this.size += heap2.size;
		this.numOfTrees += heap2.numOfTrees;
		if (heap2.min.key < this.min.key) {
			this.min = heap2.min;
		}
	}

	/**
	 * 
	 * Return the number of elements in the heap
	 *   
	 */
	public int size()
	{
		return this.size; // should be replaced by student code
	}


	/**
	 * 
	 * Return the number of trees in the heap.
	 * 
	 */
	public int numTrees()
	{
		return this.numOfTrees; // should be replaced by student code
	}

	/**
	 Inner function-cut
	 cut connection between two nodes
	 */
	public void cut(HeapNode childNode, HeapNode parentNode){
		childNode.parent = null;
		childNode.mark = false;
		parentNode.rank = parentNode.rank - 1;
		if (childNode.next == childNode){
			parentNode.child = null;
		}
		else {
			parentNode.child = childNode.next;
			childNode.prev.next = childNode.next;
			childNode.next.prev = childNode.prev;
		}
		numOfCuts++;
		this.addToRoots(childNode, false);
	}

	/**
	 Inner function-cascading cut
	 */
	public void cascadingCut(HeapNode childNode, HeapNode parentNode){
		cut(childNode, parentNode);
		if (parentNode.parent != null){
			if (!parentNode.mark){
				parentNode.mark = true;
			}
			else{
				cascadingCut(parentNode, parentNode.parent);
			}
		}
	}

	/**
	 Inner function-add to roots
	 @param node - connecting this node to the list of roots
	 @param externalHeap - if false ->  called by cut() and number of trees increases by 1
	 if true -> called by meld and number of trees increases according to the new external heap
	 */
	public void addToRoots(HeapNode node, Boolean externalHeap){
		if (this.min == null){ // adding first node to an empty heap
			this.min = node;
		}
		node.next = this.min.next;
		node.prev = this.min;
		this.min.next = node;
		if (!externalHeap){
			this.numOfTrees++;
		}
	}


	/**
	  Inner function-link
	  Link two nodes with the same rank
	  */
	public  void link(HeapNode node1, HeapNode node2){
		return;
	}


	/**
	 * Class implementing a node in a Fibonacci Heap.
	 *  
	 */
	public static class HeapNode{
		public int key;
		public String info;
		public HeapNode child;
		public HeapNode next;
		public HeapNode prev;
		public HeapNode parent;
		public int rank;
		public boolean mark;


		/**
		 * constructor
		 *
		 */
		public HeapNode(int key, String info){
			this.key = key;
			this.info = info;
			this.child = null;
			this.next = this;
			this.prev = this;
			this.parent = null;
			this.rank = 0;
			this.mark = false;

		}
	}
}