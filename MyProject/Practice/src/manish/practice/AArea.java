package src.manish.practice;

public abstract class AArea implements IArea{

	public int areaRec(int a, int b) {
	int area = a*b;
	return area;
	}

	public abstract int areaSquare(int l);
	
}
