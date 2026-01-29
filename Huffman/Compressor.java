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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Compressor class to help compressing the file and calculating the bits saved
 */
public class Compressor {
	// store the header format type: STORE_TREE or STORE_COUNTS
	private int headerFormat;

	// store the frequency array
	private int[] freq;

	// store the number of bits in the initial files
	private int initialBitsNum;

	// store a map with the int value and its corresponding code
	private Map<Integer, String> charCode;

	// store the number of bits used to compressed the data
	private int compressedDataBitsNum;

	// store the huffman tree
	private HuffmanTree thisTree;

	/**
	 * Constructor for Compressor object
	 * 
	 * @param in           InputStream from the original file
	 * @param headerFormat type of header format
	 * @throws IOException
	 */
	public Compressor(InputStream in, int headerFormat) throws IOException {
		// initialize frequency array, initial bits value, map
		freq = new int[IHuffConstants.ALPH_SIZE];
		charCode = new HashMap<>();

		// set the header format and
		this.headerFormat = headerFormat;

		// counts how many bits in the orginal file
		processBits(in);

		// create a huffman tree
		thisTree = new HuffmanTree(freq);

		// create the map for int value and their corresponding code
		charCode = thisTree.createMap();

		// calculate the number of bits for data
		compressedDataBitsNum = bitsForData(freq, charCode);

	}

	/**
	 * Counts how many bits in the original file
	 * 
	 * @param in input stream reading from the original file
	 * @throws IOException
	 */
	private void processBits(InputStream in) throws IOException {
		// convert to Bit Input Stream
		BitInputStream input = new BitInputStream(in);

		// read every 8 bits (a byte each time)
		int bit = input.readBits(IHuffConstants.BITS_PER_WORD);

		// if we haven't reach the end of file
		while (bit != -1) {
			// increment the number of byte
			initialBitsNum++;
			// increment the corresponding value frequency in the array
			freq[bit]++;
			// read the next byte
			bit = input.readBits(IHuffConstants.BITS_PER_WORD);
		}

		// times 8 to find the number of bits since I was adding one each byte before
		initialBitsNum *= IHuffConstants.BITS_PER_WORD;
		// close the input stream
		input.close();
	}

	/**
	 * Count the number of bits used to represent the compressed data
	 * 
	 * @param freq frequency array
	 * @param map  map of int value and their code
	 * @return the total number of bits used
	 */
	private int bitsForData(int[] freq, Map<Integer, String> map) {
		int count = 0;

		for (int i = 0; i < freq.length; i++) {
			if (freq[i] > 0) {
				count += (freq[i] * map.get(i).length());
			}
		}
		count += map.get(IHuffConstants.PSEUDO_EOF).length();
		return count;
	}

	/**
	 * Get the number of bits saved based on the types of the header format
	 * 
	 * @return the number of bits saved
	 */
	public int getBitsSaved() {
		// add the number of bits for: MAGIC_NUMBER(32) + HEADER_FORMAT(32) + number of
		// bits for data (calculated)
		int compressed = compressedDataBitsNum + IHuffConstants.BITS_PER_INT + 
				IHuffConstants.BITS_PER_INT;
		if (headerFormat == IHuffConstants.STORE_TREE) {
			// if it's a tree format
			// add: TREE_SIZE (32) + number of bits to represent the tree
			compressed += IHuffConstants.BITS_PER_INT + thisTree.bitsForTree();
		} else if (headerFormat == IHuffConstants.STORE_COUNTS) {
			// if it's a counts format
			// add the number of bits to store the frequency of all 256 character (256 * 32)
			compressed += IHuffConstants.ALPH_SIZE * IHuffConstants.BITS_PER_INT;
		}
		// return the number of bits saved
		return initialBitsNum - compressed;
	}

	/**
	 * Main method for compressing the file
	 * 
	 * @param in    InputStream from original file
	 * @param out   OutputStream for compressed file
	 * @param force if this is true create the output file even if it is larger than
	 *              the input file. If this is false do not create the output file
	 *              if it is larger than the input file.
	 * @return the number of bits written
	 * @throws IOException
	 */
	public int compressFile(InputStream in, OutputStream out, boolean force) throws IOException {
		// convert to a BitOutputStream
		BitOutputStream output = new BitOutputStream(out);
		int count = 0;
		// write the magic number
		output.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);
		count += IHuffConstants.BITS_PER_INT;
		// write the STORE_COUNTS or STORE_TREE constant
		output.writeBits(IHuffConstants.BITS_PER_INT, headerFormat);
		count += IHuffConstants.BITS_PER_INT;
		// Write information after the store constant that allows the Huffman tree to be
		// recreated
		if (headerFormat == IHuffConstants.STORE_TREE) {
			// if the it is a tree format
			// size of the tree
			output.writeBits(IHuffConstants.BITS_PER_INT, thisTree.bitsForTree());
			count += IHuffConstants.BITS_PER_INT;
			// write the structure of the tree
			thisTree.writeTree(output);
			count += thisTree.bitsForTree();
		} else if (headerFormat == IHuffConstants.STORE_COUNTS) {
			// if it's a count format
			for (int i = 0; i < freq.length; i++) {
				// write the frequency for each letter
				output.writeBits(IHuffConstants.BITS_PER_INT, freq[i]);
				count += IHuffConstants.BITS_PER_INT;
			}
		}
		// add the number of bits for the compressed data
		count += compressData(in, output);
		// important to close the output to flush the bit
		output.close();
		return count;
	}

	/**
	 * Read the orginal file and convert using new code generated by huffman tree
	 * and map
	 * 
	 * @param in     help to read from original file
	 * @param output write on the compressed file
	 * @return the number of bits written
	 * @throws IOException
	 */
	private int compressData(InputStream in, BitOutputStream output) throws IOException {
		BitInputStream input = new BitInputStream(in);
		int count = 0;
		int bit = input.readBits(IHuffConstants.BITS_PER_WORD);
		// read the file in 8 bits
		while (bit != -1) {
			// use the map to find the corresponding code
			String code = charCode.get(bit);
			// add code one by one
			for (int i = 0; i < code.length(); i++) {
				output.writeBits(1, code.charAt(i) == '1' ? 1 : 0);
				count += 1;
			}
			// read the next byte
			bit = input.readBits(IHuffConstants.BITS_PER_WORD);
		}
		// add the end of file code
		String code = charCode.get(IHuffConstants.PSEUDO_EOF);
		for (int i = 0; i < code.length(); i++) {
			output.writeBits(1, code.charAt(i) == '1' ? 1 : 0);
			count += 1;
		}
		input.close();
		return count;
	}

}
