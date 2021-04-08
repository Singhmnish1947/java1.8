package dataStructure;

import java.util.Scanner;

public class BinaryTree {

	static Scanner sc;
	public static void main(String[] args) {
		
		sc = new Scanner(System.in);
		createTree();	
		
	}
	public static Node createTree() {
		Node root = null;
		
		System.out.println("Enter data: ");
		int data = sc.nextInt();
		
		if(data == -1) return null;
		
		root = new Node(data);
		
		System.out.println("Enter left for "+ data);
		root.left = createTree();
		
		System.out.println("Enter right for "+data);
		root.left = createTree();
		
		return root;
	}

}

class Node {
	Node left, right;
	int data;
	
	Node(int data){
		this.data =data;	
	}	
}
