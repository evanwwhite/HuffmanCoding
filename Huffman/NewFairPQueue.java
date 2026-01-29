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

import java.util.LinkedList;
import java.util.ListIterator;

/*
 * A fair priority queue I created. When elements with equal weights are added, 
 * the earliest added element will be dequeued first, maintaining FIFO order among ties.
 */
public class NewFairPQueue <E extends Comparable<? super E>>{
	
	// internal storage using arrayList
	private LinkedList<E> myCon;
	
	// constructor to initialize the container
	public NewFairPQueue() {
		myCon = new LinkedList<>();
	}
	
	/**
	 * Check if the queue is empty
	 * @return true if it is empty else false
	 */
	public boolean isEmpty () {
		return myCon.size() == 0;
	}
	
	/**
	 * Return the size of the current queue
	 * @return the size of the queue
	 */
	public int size() {
		return myCon.size();
	}
	
	/**
	 * Add the element to the queue based on their weights, less weight in the front, if there is an
	 * element with the same weight as the item, add item behind the element.
	 * @param item item we want to add
	 */
	public void enqueue(E item) {
		if (item == null) {
			throw new IllegalArgumentException("node can not be null");
		}
		myCon.add(findIndex(item), item);
	}
	
	/**
	 * Find the index you need to add
	 * @param item item we need to compare
	 * @return return the add index
	 */
	private int findIndex(E item) {
		int index = 0;
		ListIterator<E>thisIter = myCon.listIterator();
		while (thisIter.hasNext() && thisIter.next().compareTo(item) <= 0) {
			index ++;
		}
		return index;
	}
	
	/**
	 * Remove the first one in the queue
	 * @return E type which was in front of the queue
	 */
	public E dequeue() {
		if (isEmpty()) {
			throw new IllegalArgumentException("the queue is empty");
		}
		return myCon.removeFirst();
	}
	
	/**
	 * Print out the queue from front to back
	 */
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (E object : myCon) {
			result.append(object.toString() + "\n");
		}
		return result.toString();
	}

}