import java.util.ArrayList;
import java.util.List;

public class MyPriorityQueue {

    private List<TreeNode> queue;

    public MyPriorityQueue() {
        queue = new ArrayList<>();
    }

    // Add a node in the correct place (sorted by frequency)
    public void add(TreeNode node) {
        int i = 0;

        // find the first index where the frequency is greater than the new node
        while (i < queue.size() && queue.get(i).getFrequency() <= node.getFrequency()) {
            i++;
        }

        // insert the node at the right spot to maintain order
        queue.add(i, node);
    }

    // Remove and return the first (lowest frequency) element
    public TreeNode remove() {
        if (queue.isEmpty()) {
            throw new IllegalStateException("Cannot remove from empty queue");
        }
        return queue.remove(0);
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public TreeNode buildHuffmanTree(int[] freqTable) {
        MyPriorityQueue queue = new MyPriorityQueue();
    
        // Step 1: Create leaf nodes for all values with non-zero frequency
        for (int i = 0; i < freqTable.length; i++) {
            if (freqTable[i] > 0) {
                queue.add(new TreeNode(i, freqTable[i]));
            }
        }
    
        // Step 2: Combine nodes until one tree remains
        while (queue.size() > 1) {
            TreeNode left = queue.remove();  // least frequency
            TreeNode right = queue.remove(); // second least
    
            // Create internal node with dummy value (e.g., -1)
            TreeNode parent = new TreeNode(left, -1, right);
    
            queue.add(parent);
        }
    
        // Final tree root
        return queue.remove();
    }
}
