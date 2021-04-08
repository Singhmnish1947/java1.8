
public class Pick {
	public static void main(String args[])
	{
	Scanner select = new Scanner (System.in);
	String a[][] = new String [10][2];
	int i,j,roll;
	String name = "";
	for (i=0 ; i<10 ; i++)
	{
	for (j=0 ; j<2 ; j++)
	{
	if (j=0)
	{
	System.out.println("Enter the name");
	a[i][j] = select.nextLine();
	}
	else if (j=1)
	{
	System.out.println("Enter the roll. no.");
	a[i][j] = select.nextInt();
	}
	}
	}
	System.out.println("Enter the name and roll no. to be searched");
	name = select.nextLine();
	roll = select.nextInt();
	for (i=0 ; i<10 ; i++)
	{
	for (j=0 ; j<2 ; j++)
	{
	if (j=0) && (a[i][j].equals(name))
	{
	System.out.println("Roll no. of the given name is =" + a[i][j+1]);
	}
	else if (j=1) && (a[i][j] == roll)
	{
	System.out.println("Name for the give roll no. is =" +a[i][j-1]); 
	}
	}
	}
	}
}


