public class Stack {
	private int top;
	private Object[] elements;

	Stack(int capacity) {
		elements = new Object[capacity];
		top = -1;

	}

	public void push(Object i) {
		if (!isFull()) {
			top++;
			elements[top] = i;
		}
	}

	public Object pop() {

		if (isEmpty()) {
			return null;
		} else {
			Object returndata = elements[top];
			top--;
			return returndata;
		}
	}

	public Object peek() {
		if (isEmpty()) {
			return null;
		} else {
			return elements[top];
		}

	}

	public boolean isEmpty() {

		return top == -1;

	}

	public boolean isFull() {
		return top + 1 == elements.length;
	}

	public int size() {
		return top + 1;
	}
}