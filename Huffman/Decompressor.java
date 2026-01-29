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
import java.io.OutputStream;

/**
 * A decompressor class that helps with uncompress the file and break down into
 * smaller steps
 */
public class Decompressor {
	// instance variable
	private HuffmanTree thisTree;

	/**
	 * Main method to uncompress the file
	 * 
	 * @param input help to read from the compressed file
	 * @param out   help to write on the new file
	 * @return the number of bits written on the new file
	 * @throws IOException if the header format is invalid
	 */
	public int uncompressHelper(BitInputStream input, OutputStream out) throws IOException {
		int storeType = input.readBits(IHuffConstants.BITS_PER_INT);
		if (storeType == IHuffConstants.STORE_TREE) {
			// tree size
			int treeSize = input.readBits(IHuffConstants.BITS_PER_INT);
			// build tree
			thisTree = new HuffmanTree(input, treeSize);
			// read data
			BitOutputStream output = new BitOutputStream(out);
			return thisTree.decode(input, output);
		} else if (storeType == IHuffConstants.STORE_COUNTS) {
			// frequency array
			int[] freq = createFreqArr(input);
			// priority queue and create a huffman tree
			thisTree = new HuffmanTree(freq);
			// read data
			BitOutputStream output = new BitOutputStream(out);
			return thisTree.decode(input, output);
		}
		// if the format header is invalid throw error
		throw new IOException("Header Format is invalid");
	}

	/**
	 * Create the frequency array from the bits
	 * 
	 * @param input help to read from the compressed file
	 * @return the frequency array
	 * @throws IOException if the file ends during the process
	 */
	private int[] createFreqArr(BitInputStream input) throws IOException {
		// create a frequency array
		int result[] = new int[IHuffConstants.ALPH_SIZE];
		// loop through the next 256 bytes
		for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
			// read and interpret the byte
			int bit = input.readBits(IHuffConstants.BITS_PER_INT);
			if (bit == -1) {
				throw new IOException("some thing is wrong here");
			}
			// set the frequency array to the corresponding value
			result[i] = bit;
		}
		// return the frequency array
		return result;
	}
}
