
import java.lang.Math;

public class Basic_Class1 {

	
	
	
	
	// Example of a dummy, non-clone function
	public void foo1(int a){
		if(a <3){
			while(a <3){
				a = a+1;
				System.out.println("foo");
			}
		}
	}
	
	
	// Example of a dummy, non-clone function
	public int foo2(int a, int b)
	{
		if(a>b){
			b = a;
		}
		return a;
	}
	
	// Example of a dummy, non-clone function
	public int foo3(int a)
	{
		for (int i=0; i<a;i++){
			a = a+1;
		}
		return a;
	}
	
	// Example of a dummy, non-clone function
	public boolean foo4(int a){
		if (a>3){
			return true;
		}else{
			return false;
		}
	}
	
	
	// ****************************************************************
	// ************************ Type 1 Clones   ***********************
	// ****************************************************************
	
	
	// Note: The following clones were taken from work by Krawitz
	
	//Type-1 Clones - Krawitz
	public double Type1a_Krawitz(int n)
	{
	int p = -1;
	int sum = 0;

	for (p = 0; p < n; p++)
	{
		sum += p;
	}

	if (n == 0) return sum;
	else return sum / n;
	}

	// Type 1 Clone - Krawitz
	public double Type1b_Krawitz(int n)
	{
	int p = -1;
	int sum = 0;

	//this is a comment that is not in any other method()
	for (p = 0; p < n; p++)
		sum += p;

	if (n == 0)
		return sum;
	else
		return sum / n;
	}

	
	
	// ****************************************************************
	// ************************ Type 2 Clones   ***********************
	// ****************************************************************

	//Type-2 Clones - Krawitz
	public double Type2a_Krawitz(int n)
	{
	int q = -1;
	double sum = 0;

	for (q = 0; q < n; q++)
	{
	sum += q;
	}

	if (n == 0) return sum;
	else return sum / n;
	}

	//Type-2 Clones - Krawitz
	public double Type2b_Krawitz(int t)
	{
	int p = -1;
	int tot = 0;

	//this is a comment that is not the same as any other comment
	for (p = 0; p < t; p++)
	tot += p;

	if (t == 0)
	return tot;
	else
	return tot / t;
	}

	
	// ****************************************************************
	// ************************ Type 3 Clones   ***********************
	// ****************************************************************
	
	// type 3 clone from Krawitz
	public double Type3a_Krawitz(int n)
	{
	int q = -1;
	double sum = 0;

	q = 0;
	while(q < n)
	{
	sum += q;
	q++;
	}

	if (n == 0) return sum;
	else return sum / n;
	}

	// type 3 clone - Krawitz
	public double Type3b_Krawitz(int t)
	{
	int p = -1, tot = 0;

	//this is another unique comment
	for (p = 0; p < t; p++)
		tot += p;

	if (t == 0)
		return (double)tot;
	else
		return (double)tot / t;
	}

	
	// ****************************************************************
	// ************************ Type 4 Clones   ***********************
	// ****************************************************************
	
	//Type-4 Clones  - Krawitz
	public double Type4a_Krawitz(int limit)
	{
	//to prevent stack overflow when large random values are input
	if (limit > 1000 || limit < 1)
		limit = 1;

	double[] d = new double[limit];
	double tot = 0;

	for (int n = 0; n < d.length; n++)
		d[n] = n * n * n;

	for (int n = 0; n < d.length; n++)
		tot += d[n];

	return tot;
	}

	// Type 4 Clone - Krawitz
	public double Type4b_Krawitz(int limit)
	{
	//to prevent stack overflow when large random values are input
	if (limit > 1000 || limit < 1)
		limit = 1000;

	return Type4b2_Krawitz("-", limit, 0, 0);
	}

	public double Type4b2_Krawitz(String s, int limit, double tot, int n)
	{
	if (limit > 1000 || limit < 1)//to prevent stack overflow
		limit = 1000;

	if (n < limit)
		tot = Type4b2_Krawitz("-", limit, tot + Math.pow(n, 3), ++n);

	return tot;
	}


	
	
	
	
	// ****************************************************************
	// ************************ Type 1 Clones   ***********************
	// ****************************************************************
	
	// Note, these clones were taken from the work by Cordy
	
	// Original Code - Cordy
	void sumProdO(int n) {
		double sum=0.0; //C1
		double prod =1.0;
		for (int i=1; i<=n; i++)
		{
			sum=sum + i;
			prod = prod * i;
			foo(sum, prod); 
		}
	}
	
	
	// Example 1A - Type 1 Clone - Cordy
		void sumProd1A(int n) {
			double sum=0.0; //C1
			double prod =1.0;
				for (int i=1; i<=n; i++)
				{
					sum=sum + i;
					prod = prod * i;
					foo(sum, prod); 
				}
		}
	
	
		// Example 1B - Type 1 Clone - Cordy
		void sumProd1B(int n) {
			double sum=0.0; //C1
			double prod =1.0; //C
			for (int i=1; i<=n; i++)
			{
				sum=sum + i; 
				prod = prod * i;
				foo(sum, prod); 
			}
		}
		
		
	// Example 1C - Type 1 Clone - Cordy
		void sumProd1C(int n) {
			double sum=0.0; //C1
			double prod =1.0;
			for (int i=1; i<=n; i++) {
				sum=sum + i;
				prod = prod * i;
				foo(sum, prod); 
			}
		}
		
		
		
		// Example 2A - Type 2 Clone - Cordy
		void sumProd2A(int n){
			double s=0.0; //C1
			double p =1.0;
			for (int j=1; j<=n; j++)
			{
				s=s + j;
				p = p * j;
				foo(s, p); 
			}
		}
		
		
		
	// Example 2B - Type 2 Clone - Cordy
		void sumProd2B(int n){
			double s=0.0; //C1
			double p =1.0;
			for (int j=1; j<=n; j++)
			{
				s=s + j;
				p = p * j;
				foo(p, s); 
				}
			}
		
		
		// Example 2C - Type 2 Clone - Cordy
		void sumProd2C(int n) {
			int sum=0; //C1
			int prod =1;
			for (int i=1; i<=n; i++)
			{
				sum=sum + i;
				prod = prod * i;
				foo(sum, prod); 
				}
			}
		
		
		// Example 2D - Type 2 Clone - Cordy
		void sumProd2D(int n) {
			double sum=0.0; //C1
			double prod =1.0;
			for (int i=1; i<=n; i++)
			{
				sum=sum + (i*i);
				prod = prod*(i*i);
				foo(sum, prod); 
				}
			}
		
		
		// Example 3A - Type 3 Clone - Cordy
		void sumProd3A(int n) {
			double sum=0.0; //C1
			double prod =1.0;
			for (int i=1; i<=n; i++)
			{
				sum=sum + i;
				prod = prod * i;
				foo(sum, prod, n); 
			}
		}
		
		
		
		// Example 3B - Type 3 Clone - Cordy
		void sumProd3B(int n) {
			double sum=0.0; //C1
			double prod =1.0;
			for (int i=1; i<=n; i++)
			{
				sum=sum + i;
				prod = prod * i;
				foo(prod); 
				}
			}
	
		
		// Example 3C - Type 3 Clone - Cordy
		void sumProd3C(int n) {
			double sum=0.0; //C1
			double prod =1.0;
			for (int i=1; i<=n; i++)
			{
				sum=sum + i;
				prod = prod * i;
				if ((n % 2) == 0) { // extra brackets added for syntatic purposes
					foo(sum, prod);
				} 
			}
		}
		
		
		// Example 3D - Type 3 Clone - Cordy
		void sumProd3D(int n) {
			double sum=0.0; //C1
			double prod =1.0;
			for (int i=1; i<=n; i++)
			{
				sum=sum + i;
				//line deleted
				foo(sum, prod); 
				}
			}
		
		// Example 3E - Type 3 Clone - Cordy
		// For syntax purposes, the precise functionality was altered.
		public void sumProd3E(int n) {
		double sum=0.0; //C1
		double prod =1.0;
		for (int i=1; i<=n; i++)
		{ 
			if (i %2 == 0){ 
				sum+= i;
			}
			prod = prod * i;
			foo(sum, prod); 
			}
		}
		
		// Example 4a - Type 4 Clone - Cordy
		void sumProd4A(int n) {
			double prod =1.0;
			double sum=0.0; //C1
			for (int i=1; i<=n; i++)
			{
				sum=sum + i;
				prod = prod * i;
				foo(sum, prod); 
			}
		}
		
		
		// Example 4B - Type 4 Clone - Cordy
		void sumProd4B(int n) {
			double sum=0.0; //C1
			double prod =1.0;
			for (int i=1; i<=n; i++)
			{
				prod = prod * i;
				sum=sum + i;
				foo(sum, prod); 
			}
		}
		
		
		// Example 4C - Type 4 Clone - Cordy
		void sumProd4C(int n) {
			double sum=0.0; //C1
			double prod =1.0;
			for (int i=1; i<=n; i++)
			{
				sum=sum + i;
				foo(sum, prod);
				prod=prod * i; 
			}
		}
		
		// Example 4D - Type 4 Clone - Cordy
		void sumProd4D(int n) {
			double sum=0.0; //C1
			double prod =1.0;
			int i=0;
			while (i<=n)
			{ 
				sum=sum + i;
				prod = prod * i;
				foo(sum, prod);
				i++ ; 
			}
		}
		
		
		
	
	// dummy methods to simply handle the test sum prod functions
	private double foo(double sum, double prod){return sum + prod +1;}
	private double foo(double sum){return sum +1.0;}
	
	private double foo(double sum, double prod, double temp){
		return sum + prod + temp;
	}


	
	
	
	
}

