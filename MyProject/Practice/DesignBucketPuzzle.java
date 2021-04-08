import java.util.Scanner;

public class DesignBucketPuzzle {

	private int a = 8;
	private int b = 0;
	private int c = 0;

	public static void main(String[] args) {
		DesignBucketPuzzle run = new DesignBucketPuzzle();
		run.solvePuzzle();
	}

	public void solvePuzzle() {
		int d = a + b + c;
		String sequence = "";
		boolean success = false;
		System.out.println("Initially 8 Litres Bucket is full, 5 litres and 3 litres Bucket is empty");
		if (d == 8 && a <= 8 && b <= 5 && c <= 3) {

			System.out.println("Please choose an Operation.");
			System.out.println(
					"(1) 8to5 = 8 litres bucket to 5 litres bucket. \n(2) 8to3 = 8 litres bucket to 3 litres bucket. \n(3) 5to3 = 5 litres bucket to 3 litres bucket. \n(4) 5to8 = 5 litres bucket to 8 litres bucket. \n(5) 3to8 = 3 litres bucket to 8 litres bucket. \n(6) 3to5 = 3 litres bucket to 5 litres bucket.");

			while (false == success) {

				System.out.println(
						"\nChoose either Serial No or Key of Operation.\nEg. 1 or 8to5 for 8 litres bucket to 5 litres bucket.");
				System.out.println("8 litres bucket is " + a + " litre/s full.\n5 litres bucket is " + b
						+ " litre/s full.\n3 litres bucket is " + c + " litre/s full.");
				Scanner input = new Scanner(System.in);
				String key = input.nextLine();


				if ("1".equals(key) || "8to5".equals(key)) {
					operationAtoB();
					sequence = sequence + "8to5";
				}

				if ("2".equals(key) || "8to3".equals(key)) {
					operationAtoC();
					sequence = sequence + "8to3";
				}

				if ("3".equals(key) || "5to3".equals(key)) {
					operationBtoC();
					sequence = sequence + "5to3";
				}

				if ("4".equals(key) || "5to8".equals(key)) {
					operationBtoA();
					sequence = sequence + "5to8";
				}

				if ("5".equals(key) || "3to8".equals(key)) {
					operationCtoA();
					sequence = sequence + "3to8";
				}

				if ("6".equals(key) || "3to5".equals(key)) {
					operationCtoB();
					sequence = sequence + "3to5";
				}

				if (a == 4 && b == 4) {
					success = true;
					System.out.println("\n8 litres bucket is " + a + " litre/s full.\n5 litres bucket is " + b
							+ " litre/s full.\n3 litres bucket is " + c + " litre/s full.");

					System.out.println(
							"=============================================================================\nBingo! You Did it.\n=============================================================================");
					System.out.println("Your Pattern was " + sequence);
					break;
				}
				sequence = sequence + " >> ";
				System.out.println(sequence);

			}
		}

	}

	public void operationAtoB() {
		if (getA() >= 5 && getB() >= 0) {

			setA(getA() - 5 + getB());
			setB(5);

		} else if (getA() < 5 && getB() <= 5) {
			if ((getB() + getA()) <= 5) {
				setB(getB() + getA());
				setA(0);
			} else {

				setA((getB() + getA()) - 5);
				setB(5);
			}
		} else {
			System.out.println("Invalid Move");
		}
	}

	public void operationAtoC() {
		if (getA() >= 3 && getC() >= 0) {

			setA(getA() - 3 + getC());
			setC(3);

		} else if (getA() < 3 && getC() <= 3) {
			if ((getC() + getA()) <= 3) {
				setC(getC() + getA());
				setA(0);
			} else {

				setA((getC() + getA()) - 3);
				setC(3);
			}
		} else {
			System.out.println("Invalid Move");
		}
	}

	public void operationBtoC() {
		if (getB() >= 3 && getC() >= 0) {

			setB(getB() - 3 + getC());
			setC(3);

		} else if (getB() < 3 && getC() <= 3) {
			if ((getC() + getB()) <= 3) {
				setC(getC() + getB());
				setB(0);
			} else {

				setB((getC() + getB()) - 3);
				setC(3);
			}
		} else {
			System.out.println("Invalid Move");
		}
	}

	public void operationBtoA() {
		setA(getA() + getB());
		setB(0);

	}

	public void operationCtoB() {
		setB(getC() + getB());
		setC(0);
	}

	public void operationCtoA() {
		setA(getC() + getA());
		setC(0);
	}

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		this.c = c;
	}
}
