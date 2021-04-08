public class JavaPrac {

	public static void main(String a[]) {
/*		int n = 4;
		int[][] arr = new int[n][n];
		int count = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				count++;
				arr[i][j] = count;
				System.out.print(count + " ");

			}

		}

		int lengthRun = n;
		int sizeN = -1;
		int sizeM = 0;
		String output = "";
		System.out.println();
		System.out.println(run(lengthRun, sizeN, sizeM, output, arr));*/
		
		System.out.println(10 + 20 +"Java");

	}

	static String run(int lengthRun, int sizeN, int sizeM, String output, int[][] arr) {
		for (int i = 0; i < lengthRun; i++) {
			sizeN++;
			output = output + arr[sizeM][sizeN] + " ";

		}
		lengthRun--;

		for (int i = 0; i < lengthRun; i++) {
			sizeM = sizeM + 1;
			output = output + arr[sizeM][sizeN] + " ";
		}

		for (int i = lengthRun; i > 0; i--) {
			sizeN = sizeN - 1;
			output = output + arr[sizeM][sizeN] + " ";

		}
		lengthRun--;
		for (int i = lengthRun; i > 0; i--) {
			sizeM = sizeM - 1;
			output = output + arr[sizeM][sizeN] + " ";
		}
		if (lengthRun>0) {
			output = run(lengthRun, sizeN, sizeM, output, arr);
		}
		return output;
	}

}
