package dataStructure;

public class LinkedList {

	Node head;
	int value = 0;

}

class Node {
	Node next = null;
	int data = 0;

	Node(int d) {

		data = d;
		next = null;
	}

}

class Prac {
	public static void main(String[] args) {

		LinkedList ll = new LinkedList();
		Node second = new Node(2);

		ll.head.next = second;
		second.next = null;

		System.out.println(ll);

	}
}
