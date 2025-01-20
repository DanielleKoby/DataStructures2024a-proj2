/**
 * FibonacciHeap
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
	public void deleteMin(boolean isMin)
	{
		HeapNode tempMin = this.detachMinNode();
		this.size --;
		if(size == 0 || tempMin == null){
			return;
		}

		HeapNode[] buckets;
		// Successive linking - as numOfTrees may be larger than log(n) due to lazy inserts/melds
		if (isMin) { // deleted the real min - find the new one:
			buckets = SuccessiveLinking(tempMin);
			this.updateMin(buckets);
		}

		return;
	}

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
//		assert x.key - diff > 0;
		x.key -= diff;
		HeapNode realMin = this.min;
		if (x.key < this.min.key) { // Updating minimum node if needed
			this.min = x;
		}

		// Invariant doesn't preserved
		if ((x.parent != null) && (x.key < x.parent.key)) {
			this.cascadingCut(x, x.parent, realMin);
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
		if (x == this.min){
			deleteMin(true);
		}
		else{ // x is not the min
			HeapNode realMin = this.min;
			this.decreaseKey(x, x.key - min.key +1);  // x.key will now be (min.key -1) --> the new min
			// Check if key will become min and if it can be negative;
			this.deleteMin(false);  // Without successive linking
			this.min = realMin;
		}
		return;
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
		addToRoots(heap2.min, false, null); // Concatenating the heap to the list of roots by the minimum node
		this.size += heap2.size;
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
	 * Inner function-SuccessiveLinking
	 * Consolidates the trees in the root list to ensure that no two trees have the same degree.
	 *
	 * @param node - the root of a valid list of roots (excluding the minimum node).
	 * @return an array of buckets, where each bucket contains at most one tree of a given degree.
	 */
	public HeapNode[] SuccessiveLinking(HeapNode node){
		// Create the log(n) buckets:
		int maxDegree = (int) Math.ceil(Math.log(this.size) / Math.log(2)) + 1;
		HeapNode[] buckets = new HeapNode[maxDegree];
		HeapNode curr = node;
		for (int tree = 0; tree < numOfTrees; tree++){
			if (buckets[curr.rank] == null){
				buckets[curr.rank] = curr;
				curr = curr.next;
			}
			else{
				HeapNode next = curr.next; // keeping a pointer to the next curr before detaching its sibling
				HeapNode rootOfLinked = this.link(buckets[curr.rank], curr);
				buckets[rootOfLinked.rank-1] = null;
				while (buckets[rootOfLinked.rank] != null){
					rootOfLinked = link(rootOfLinked, buckets[rootOfLinked.rank]);
					buckets[rootOfLinked.rank-1] = null;
				}
				buckets[rootOfLinked.rank] = rootOfLinked;
				curr = next;
			}
		}
		return buckets;
	}

	/**
	 * Inner function-updateMin
	 * Updates the minimum node in the heap after successive linking or other operations.
	 *
	 * @param buckets - an array of buckets representing the consolidated root list.
	 */
	public void updateMin(HeapNode[] buckets){
		int index = 0;
		while (index < buckets.length && buckets[index] == null){
			index++;
		}
		HeapNode current = buckets[index];
		int cnt = 1;
		this.min = current;
		while (current.next != buckets[index]){
			current = current.next;
			cnt ++;
			if (current.key < this.min.key)
			{
				this.min = current;
			}
		}
		this.numOfTrees = cnt;
	}

	/**
	 * Inner function-detachMinNode
	 * Removes the minimum node from the heap and adjusts the heap structure.
	 * - If the minimum node has children, they are added to the root list.
	 * - If the minimum node is the only root, the heap becomes empty.
	 *
	 * @return a reference to the next root node, or null if the heap becomes empty.
	 */
	public HeapNode detachMinNode(){
		if (this.min == null) { // If the heap is already empty, return null
			return null;
		}
		this.numOfCuts += this.min.rank;

		if (min.child != null) // min has children
		{
			// Take care of the parent-child relationship
			HeapNode child = min.child;

			// detach children from min
			do {
				child.parent = null;
				child = child.next;
			} while(child != min.child);

			addToRoots(min.child, false, null);  // Add the children to the list of roots
			min.child = null;  // Detach min from its child
		}

		// At this point, root is ready for deletion, its children moved up the heap and are now roots

		if (min.next == min)  // Min is the only node in the tree
		{
			this.min = null;
			this.numOfTrees = 0;
			return null;
		}
		else{ // If there are other roots, remove min from the root list (bypass)
			min.prev.next = min.next;
			min.next.prev = min.prev;
			this.numOfTrees--;
			this.min = min.next;
			return this.min;
		}
	}

	/**
	 * Inner function-cut
	 * Detaches a child node from its parent, promoting it to the root list.
	 *
	 * @param childNode - the child node to be removed from its parent.
	 * @param parentNode - the parent node from which the child is removed.
	 * @param realMin - the original minimum node before the operation.
	 */
	public void cut(HeapNode childNode, HeapNode parentNode, HeapNode realMin){
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
			childNode.prev = childNode;
			childNode.next = childNode;
		}
		numOfCuts++;
		this.addToRoots(childNode, true, realMin);
	}

	/**
	 * Inner function-cascading cut
	 * Recursively cuts a node from its parent and promotes it to the root list.
	 * If the parent is marked, the cut operation propagates up the tree.
	 *
	 * @param childNode - the node to be cut.
	 * @param parentNode - the parent of the node being cut.
	 * @param realMin - the original minimum node before the operation.
	 */
	public void cascadingCut(HeapNode childNode, HeapNode parentNode, HeapNode realMin){
		cut(childNode, parentNode, realMin);
		if (parentNode.parent != null){
			if (!parentNode.mark){
				parentNode.mark = true;
			}
			else{
				cascadingCut(parentNode, parentNode.parent, realMin);
			}
		}
	}

	/**
	 * Inner function-add to roots
	 * Adds a node to the list of roots in the heap.
	 *
	 * @param node - the node to be added to the root list.
	 * @param decreaseKey - true if called by the decreaseKey() operation where the minimum
	 *                      may temporarily be invalid; false otherwise.
	 * @param realMin - the original minimum node before decreaseKey was invoked.
	 */
	public void addToRoots(HeapNode node, Boolean decreaseKey, HeapNode realMin){
		// case 0- adding first node to an empty heap
		if (this.min == null){
			this.min = node;
		}

		// calculate the number of new trees
		int numOfNewTrees = numOfSibs(node);
		// case 0- Decrease key - node without brothers

		if (decreaseKey){
			node.next = realMin.next;
			node.prev = realMin;
			realMin.next.prev = node;
			realMin.next = node;
		}
		// case 1- node without brothers
		else {
			if (node.next == node && node.prev == node) {
				node.next = this.min.next;
				node.prev = this.min;
				this.min.next.prev = node;
				this.min.next = node;
			}
			// case 2- node have brothers (maybe sisters as well)
			else {
				this.min.next.prev = node.prev;
				node.prev.next = this.min.next;
				this.min.next = node;
				node.prev = this.min;

			}
		}
		// update number of trees
		this.numOfTrees += numOfNewTrees;
	}

	/**
	 * Inner function-numOfSibs
	 * Counts the number of siblings (including the given node) in a circular doubly linked list.
	 *
	 * @param node - the node whose sibling count is to be determined.
	 * @return the number of siblings, including the node itself.
	 */
	public int numOfSibs(HeapNode node){
		int numOfSibs = 1;
		HeapNode endNode = node;
		while (endNode.next != node){
			numOfSibs++;
			endNode = endNode.next;
		}
		return numOfSibs;
	}

	/**
	 * Inner function-link
	 * Links two nodes of the same rank into a single tree.
	 * - The node with the smaller key becomes the root, and the other becomes its child.
	 * - Updates the rank of the new root node.
	 *
	 * @param node1 - the first node to be linked.
	 * @param node2 - the second node to be linked.
	 * @return the root of the resulting tree after linking.
	 */
	public HeapNode link(HeapNode node1, HeapNode node2){
		this.numOfLinks ++;
		// Find new root
		HeapNode root = node1;
		HeapNode degraded = node2;
		if (node1.key > node2.key){
			root = node2;
			degraded = node1;
		}

		// Remove the node with the greater key from the list of roots
		degraded.prev.next = degraded.next;
		degraded.next.prev = degraded.prev;

		// Make degraded a child of root
		degraded.parent = root;
		// Make root a parent of degraded
		// First case: root has no children
		if (root.child == null){
			root.child = degraded;
			degraded.next = degraded;
			degraded.prev = degraded;
		}
		// Second case: root has children
		else{
			degraded.next = root.child;
			degraded.prev = root.child.prev;
			root.child.prev.next = degraded;
			root.child.prev = degraded;
		}

		// Update rank of root
		root.rank ++;

		return root;



		// Ziv's version
//		if (node2.key < root.key){
//			root = node2;
//			HeapNode oldChild = root.child;
//			node1.next = oldChild;
//			oldChild.prev = node1;
//			root.child = node1;
//		}
//		else{
//			HeapNode oldChild = root.child;
//			node2.next = oldChild;
//			if (oldChild != null) {
//				oldChild.next = node2;
//				node2.prev = oldChild;
//			}
//			root.child = node2;
//		}
//		return root;
	}

	public Boolean isEmpty(){
		return this.size == 0;
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