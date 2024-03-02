package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;


/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * 
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) {
        fileName = f;
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by
     * frequency
     */
    public void makeSortedList() {
        StdIn.setFile(fileName);
        int count = 0;
        int[] freqOccur = new int[128];
        ArrayList<CharFreq> sortedCharFreqList = new ArrayList<>();

        while (StdIn.hasNextChar()) {
            char toRead = StdIn.readChar();
            freqOccur[toRead] = freqOccur[toRead] + 1;
            count++;
        }

        for (int i = 0; i < 128; i++) {
            if (freqOccur[i] != 0) {
                double probability = freqOccur[i];
                sortedCharFreqList.add(new CharFreq((char) i, probability / count));
            }
        }

        if (sortedCharFreqList.size() == 1) {
            CharFreq value = sortedCharFreqList.get(0);
            int ascii = (int) value.getCharacter() + 1;
            if ((int) value.getCharacter() == 127) {
                ascii = 0;
            }
            sortedCharFreqList.add(new CharFreq((char) ascii, 0));
        }

        Collections.sort(sortedCharFreqList);
        this.sortedCharFreqList = sortedCharFreqList;
    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    Queue<CharFreq> source = new Queue<CharFreq>();
    Queue<TreeNode> target = new Queue<TreeNode>();

    public void makeTree() {
        for (int i = 0; i < sortedCharFreqList.size(); i++) {
            source.enqueue(sortedCharFreqList.get(i));
        }

        // run steps to make tree

        while (!source.isEmpty() || target.size() >= 2) {
            TreeNode leftNode = new TreeNode();
            TreeNode rightNode = new TreeNode();

            //Set leftNode
            if (target.size() >= 1) {
                if (!source.isEmpty()) {
                    Double sourceProb = source.peek().getProbOcc();
                    Double targetProb = target.peek().getData().getProbOcc();
                    if (sourceProb <= targetProb) {
                        leftNode = new TreeNode(source.dequeue(), null, null);
                    } else {
                        leftNode = new TreeNode(target.peek().getData(), target.peek().getLeft(), target.peek().getRight());
                        target.dequeue();
                    }
                } else {
                    leftNode = new TreeNode(target.peek().getData(), target.peek().getLeft(), target.peek().getRight());
                    target.dequeue();
                }
            } else {
                leftNode = new TreeNode(source.dequeue(), null, null);
            }

            // Set rightNode
            if (target.size() >= 1) {
                if (!source.isEmpty()) {
                    Double sourceProb = source.peek().getProbOcc();
                    Double targetProb = target.peek().getData().getProbOcc();
                    if (sourceProb <= targetProb) {
                        rightNode = new TreeNode(source.dequeue(), null, null);
                    } else {
                        rightNode = new TreeNode(target.peek().getData(), target.peek().getLeft(), target.peek().getRight());
                        target.dequeue();
                    }
                } else {
                    rightNode = new TreeNode(target.peek().getData(), target.peek().getLeft(), target.peek().getRight());
                    target.dequeue();
                }
            } else {
                rightNode = new TreeNode(source.dequeue(), null, null);
            }

            // create new node to insert into tree
            CharFreq toInsert = new CharFreq(null, leftNode.getData().getProbOcc() + rightNode.getData().getProbOcc());
            TreeNode newNode = new TreeNode(toInsert, leftNode, rightNode);

            // Add newNode to tree
            target.enqueue(newNode);
        }
        // Set root of the tree
        huffmanRoot = target.peek();

    }

    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding.
     * Characters not
     * present in the huffman coding tree should have their spots in the array left
     * null.
     * Set encodings to this array.
     */
    public void makeEncodings() {
       encodings = new String[128];
        ArrayList<String> coded = getCodes();
        for (int i = 0 ; i < coded.size() ; i++){
            String toAdd = coded.get(i);
            int position = (int)toAdd.charAt(0);
            encodings[position] = toAdd.substring(1);; 
        }
    }
    
    private ArrayList<String> getCodes(){
        TreeNode ptr = huffmanRoot;
        ArrayList<String> out = new ArrayList<String>();
        if (ptr == null){
        }
        traverse (out, ptr.getLeft(), "0");
        traverse (out, ptr.getRight(), "1");
        return out;
    }

    private void traverse (ArrayList<String> code, TreeNode ptr, String prefix){
        if (ptr == null){
            return;
        }
        traverse(code, ptr.getLeft(), prefix +"0");
        traverse (code, ptr.getRight(), prefix + "1");
        if (ptr.getData().getCharacter() != null){
        code.add(ptr.getData().getCharacter() + prefix);}
    }

    

    /**
     * Using encodings and filename, this method makes use of the writeBitString
     * method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);
        String toWrite = "";
        while (StdIn.hasNextChar()){
            char toCode = StdIn.readChar();
            toWrite += encodings[(int)toCode];
        }
        writeBitString(encodedFile, toWrite);
        /* Your code goes here */
    }

    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename  The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding - 1; i++)
            pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1')
                currentByte += 1 << (7 - byteIndex);
            byteIndex++;

            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }

        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        } catch (Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString
     * method
     * to convert the file into a bit string, then decodes the bit string using the
     * tree, and writes it to a decoded file.
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {
        StdOut.setFile(decodedFile);
        TreeNode ptr = huffmanRoot;
        String toDecode = readBitString(encodedFile);
        int length = toDecode.length();
        String decodedString = "";
        for (int i = 0 ; i < length ; i++){
            if (toDecode.charAt(i) == '0' ){
                ptr = ptr.getLeft();
            }
            else {
                ptr = ptr.getRight();
            }
            if (ptr.getLeft() == null && ptr.getRight() == null){
                decodedString += ptr.getData().getCharacter();
                ptr = huffmanRoot;
            }
        }
        StdOut.print(decodedString);
        /* Your code goes here */
    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";

        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();

            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString +
                        String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1')
                    return bitString.substring(i + 1);
            }

            return bitString.substring(8);
        } catch (Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver.
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() {
        return fileName;
    }

    public ArrayList<CharFreq> getSortedCharFreqList() {
        return sortedCharFreqList;
    }

    public TreeNode getHuffmanRoot() {
        return huffmanRoot;
    }

    public String[] getEncodings() {
        return encodings;
    }
}
