/*  Student information for assignment:
 *
 *  On <OUR> honor, Duc Anh Dang and Evan White,
 *  this programming assignment is <MY|OUR> own work
 *  and <WE> have not provided this code to any other student.
 *
 *  Number of slip days used: 0
 *
 *  Student 1: Duc Anh Dang
 *  UTEID: dd36956
 *  email address: danganhbill0806@gmail.com
 *  
 *  Student 2: Evan White
 *  UTEID: eww495
 *  email address: evanwhite@utexas.edu
 *
 *  Grader name: Karnika Choudhury
 *  Section number: 50770
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HuffmanTree {
	private TreeNode root;
	private int size;

	/**
	 * Constructor to help create tree from bits
	 * 
	 * @param freq frequency array of the character
	 */
	public HuffmanTree(int[] freq) {
		int internalNode = 0;
		int leafNode = 0;
		// create a Fair Priority Queue
		NewFairPQueue<TreeNode> queue = new NewFairPQueue<>();
		for (int i = 0; i < freq.length; i++) {
			// if the frequency is more than 0
			if (freq[i] > 0) {
				leafNode++;
				// create a treeNode and enqueue
				TreeNode n = new TreeNode(i, freq[i]);
				queue.enqueue(n);
			}
		}
		// enqueue the end of file character
		queue.enqueue(new TreeNode(IHuffConstants.PSEUDO_EOF, 1));
		leafNode++;

		// create the tree
		while (queue.size() > 1) {
			// pop the first and second of the queue
			TreeNode firstNode = queue.dequeue();
			TreeNode secondNode = queue.dequeue();
			internalNode++;
			TreeNode combined = new TreeNode(firstNode, 0, secondNode);
			// insert the combined treeNode with the the first and second node as child node
			queue.enqueue(combined);
		}

		// last element of the queue will be the root
		root = queue.dequeue();
		size = leafNode * (IHuffConstants.BITS_PER_WORD + 1) + leafNode + internalNode;
	}

	/**
	 * Constructor to recreate the tree from the bits read
	 * 
	 * @param input    read from file
	 * @param treeSize the number of bits represent the tree
	 * @throws IOException if file ends during the process
	 */
	public HuffmanTree(BitInputStream input, int treeSize) throws IOException {
		size = treeSize;
		root = createTreeFromBits(input, new int[] { treeSize });
	}

	/**
	 * Method to recreate the tree from the given bits
	 * 
	 * @param input    help to read the file
	 * @param treeSize how many bits will represent the tree
	 * @return the root of the tree
	 * @throws IOException if the file ends during the process
	 */
	private TreeNode createTreeFromBits(BitInputStream input, int[] treeSize) throws IOException {
		// if we have read all the number of bits, there is nothing to do
		if (treeSize[0] <= 0) {
			return null;
		}

		// read bit
		int bit = input.readBits(1);
		treeSize[0]--;
		// in case some error happen
		if (bit == -1) {
			throw new IOException("something went wrong");
		}

		if (bit == 0) {
			// this is an internal node
			TreeNode left = createTreeFromBits(input, treeSize);
			TreeNode right = createTreeFromBits(input, treeSize);
			return new TreeNode(left, 0, right);
		} else {
			// this is a leaf node
			// read the value
			int val = input.readBits(IHuffConstants.BITS_PER_WORD + 1);
			treeSize[0] -= IHuffConstants.BITS_PER_WORD + 1;
			return new TreeNode(val, 0);
		}
	}

	/**
	 * Calculate the size of the tree
	 * 
	 * @return the size of the tree
	 */
	public int bitsForTree() {
		return size;
	}

	/**
	 * Create map kick off method
	 * 
	 * @return map that stores the int and corresponding code
	 */
	public Map<Integer, String> createMap() {
		Map<Integer, String> result = new HashMap<>();
		createMapHelper(root, new StringBuilder(), result);
		return result;
	}

	/**
	 * Recursive method to generate code for each value by traversing through the
	 * tree
	 * 
	 * @param node the current node
	 * @param path the StringBuilder used to store the current path value
	 * @param map  map to store the integer and its corresponding code
	 */
	private void createMapHelper(TreeNode node, StringBuilder path, Map<Integer, String> map) {
		// if it is a leaf node
		if (node.isLeaf()) {
			map.put(node.getValue(), path.toString());
		} else {
			// traverse left
			path.append(0);
			createMapHelper(node.getLeft(), path, map);
			// backtrack: removing the last element added
			path.deleteCharAt(path.length() - 1);

			// traverse right
			path.append(1);
			createMapHelper(node.getRight(), path, map);
			// backtrack: removing the last element added
			path.deleteCharAt(path.length() - 1);

		}
	}

	/**
	 * Kick off method to write the tree structure
	 * 
	 * @param output connect to the output file
	 */
	public void writeTree(BitOutputStream output) {
		writeTreeHelper(output, root);
	}

	/**
	 * Method to write the structure of the tree in pre-order traversal: 0 as
	 * internal node, 1 as leaf node followed by a 9-bit ascii value
	 * 
	 * @param output help to write on the output file
	 * @param node   current node, starts with root
	 */
	private void writeTreeHelper(BitOutputStream output, TreeNode node) {
		// if current node is a leaf node
		if (node.isLeaf()) {
			// write number 1
			output.writeBits(1, 1);
			// then write the ascii value of the character in 9 bits
			output.writeBits(IHuffConstants.BITS_PER_WORD + 1, node.getValue());
		} else {
			// we write 0 for internal node
			output.writeBits(1, 0);
			// traverse left
			writeTreeHelper(output, node.getLeft());
			// traverse right
			writeTreeHelper(output, node.getRight());

		}
	}

	/**
	 * Use the huffman tree and use the data in the compressed file to decode
	 * 
	 * @param input  help to read from the compressed file
	 * @param root   the root of the huffman tree
	 * @param output help to print out the decoded data on the new file
	 * @return the number of bits written
	 * @throws IOException if the file ends during the process
	 */
	public int decode(BitInputStream input, BitOutputStream output) throws IOException {
		int count = 0;
		TreeNode node = root;
		boolean done = false;

		while (!done) {
			// read one bit
			int bit = input.readBits(1);

			// in case error happen
			if (bit == -1) {
				throw new IOException("Error reading compressed file. \n" 
						+ "unexpected end of input. No PSEUDO_EOF value.");
			}
			// decide where to move
			// if bit equals to 0 go left else go right
			node = bit == 0 ? node.getLeft() : node.getRight();

			// if it is a leaf node
			if (node.getLeft() == null && node.getRight() == null) {
				int value = node.getValue();

				// if we reach the end of file
				if (value == IHuffConstants.PSEUDO_EOF) {
					done = true;
				} else {
					// write the value to the file
					output.write(node.getValue());
					count += IHuffConstants.BITS_PER_WORD;
					// reset to root
					node = root;
				}
			}
		}
		output.flush();
		return count;
	}
}