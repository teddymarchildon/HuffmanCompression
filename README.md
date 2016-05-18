# HuffmanCompression

### This project utilizes a Huffman tree to compress files of various formats. 

### HuffProcessor
  This is the primary functionality class for the application. It first sets the frequencies of all the letters read in from the file
  in an array that doubles as a map. In order to set the tree, we use a priority queue sorted by weights of the nodes, which is the 
  frequency of that character. We then set the tree up according to Huffman tree guidelines. We are able to recursively get the path
  codes corresponding to each leaf node, which consist of binary digits 0s and 1s, and from that map we can write the file back out 
  recursively as well. 
