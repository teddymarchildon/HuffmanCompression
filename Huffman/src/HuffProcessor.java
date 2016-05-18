import java.util.HashMap;
import java.util.PriorityQueue;

public class HuffProcessor implements Processor{

	private int[] charFreqs = new int[ALPH_SIZE + 1];
	private HashMap<Integer, String> charCodes = new HashMap<Integer, String>();

	@Override
	public void compress(BitInputStream in, BitOutputStream out) {
		setFrequencies(in);
		HuffNode root = setTree();
		getCodes(root, "");
		out.writeBits(BITS_PER_INT, HUFF_NUMBER);
		writeHeader(root, out);
		int a = 0;
		while((a = in.readBits(BITS_PER_WORD)) != -1){
			String code = charCodes.get(a);
			out.writeBits(code.length(), Integer.parseInt(code, 2));
		}
		String code = charCodes.get(PSEUDO_EOF);
		out.writeBits(code.length(), Integer.parseInt(code, 2));
	}

	private void setFrequencies(BitInputStream in){
		int a = 0;
		while((a = in.readBits(BITS_PER_WORD)) != -1){
			charFreqs[a]++;
		}
		charFreqs[PSEUDO_EOF] = 0;
		in.reset();
	}

	private HuffNode setTree(){
		PriorityQueue<HuffNode> pq = new PriorityQueue<HuffNode>();
		for(int i = 0; i < ALPH_SIZE; i++){
			if(charFreqs[i] != 0){
				pq.add(new HuffNode(i, charFreqs[i]));
			}
		}
		pq.add(new HuffNode(PSEUDO_EOF, 0));
		while(pq.size() > 1){
			HuffNode n1 = pq.poll();
			HuffNode n2 = pq.poll();
			pq.add(new HuffNode(-1 , n1.weight() + n2.weight(), n1, n2));
		}
		return pq.poll();
	}

	private void getCodes(HuffNode root, String path){
		if(isLeafNode(root)){
			charCodes.put(root.value(), path);
			return;
		}
		else{
			getCodes(root.left(), path + 0);
			getCodes(root.right(), path + 1);
		}
	}

	private void writeHeader(HuffNode root, BitOutputStream out){
		if(isLeafNode(root)){
			out.writeBits(1, 1);
			out.writeBits(BITS_PER_WORD + 1, root.value());
			return;
		}
		else{
			out.writeBits(1, 0);
			writeHeader(root.left(), out);
			writeHeader(root.right(), out);
		}
	}

	private boolean isLeafNode(HuffNode n){
		return n.left() == null && n.right() == null;
	}

	@Override
	public void decompress(BitInputStream in, BitOutputStream out) {
		if(in.readBits(BITS_PER_INT) != HUFF_NUMBER){
			throw new HuffException("The first number is not the Huff Number");
		}
		HuffNode root = readHeader(in);
		HuffNode current = root;
		int a = 0;
		while((a = in.readBits(1)) != -1){
			if(a == 1){
				current = current.right();
			}
			else{
				current = current.left();
			}
			if(isLeafNode(current)){
				if(current.value() == PSEUDO_EOF){
					return;
				}
				else{
					out.writeBits(BITS_PER_WORD, current.value());
					current = root;
				}
			}
		}
	}

	private HuffNode readHeader(BitInputStream in){
		if(in.readBits(1) == 0){
			HuffNode left = readHeader(in);
			HuffNode right = readHeader(in);
			return new HuffNode(-1, 0, left, right);
		}
		else{
			return new HuffNode(in.readBits(9), 0);
		}
	}
}
