#include <stdio.h>
#include <math.h>
#include <string.h>


// dummy methods to simply handle the test sum prod functions
	void foo(double sum){ printf("%3.1f",sum +1.0); }
	void foo2(double sum, double prod){printf("%3.1f",sum + prod +1);}
	void foo3(double sum, double prod, double temp){printf("%3.1f",sum + prod + temp);}


//Type-1 Clones - Krawitz
	float Type1a_Krawitz(int n)
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


int mult(int a, int b){
	return a*b;
}


	// Type 1 Clone - Krawitz
	float Type1b_Krawitz(int n)
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

	
	

	//Type-2 Clones - Krawitz
	float Type2a_Krawitz(int n)
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
	float Type2b_Krawitz(int t)
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

		
	// type 3 clone from Krawitz
	float Type3a_Krawitz(int n)
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
	float Type3b_Krawitz(int t)
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


//Type-4 Clones  - Krawitz
    double Type4a_Krawitz(int limit){
	  double* d;
	  double tot = 0;
	  int n;
      //to prevent stack overflow when large random values are input
      if (limit > 1000 || limit < 1)
            limit = 1;
	  
	  d = (double*)malloc(limit*sizeof(double));

	  for (n = 0; n < limit; n++)
            d[n] = n * n * n;

      for (n = 0; n < limit; n++)
            tot += d[n];
	  free ((void*) d);
      return tot;
    }

 
// Type 4 Clone - Krawitz
	double Type4b2_Krawitz(char s, int limit, double tot, int n){

		//to prevent stack overflow when large random values are input
		if (limit > 1000 || limit < 1)
			limit = 1000;

		if (n < limit)
			tot = Type4b2_Krawitz('-', limit, tot + n*n*n, ++n);

		return tot;
  }
	
// Type 4 Clone - Krawitz
    double Type4b_Krawitz(int limit){

      //to prevent stack overflow when large random values are input
      if (limit > 1000 || limit < 1)
            limit = 1000;

      return Type4b2_Krawitz('-', limit, 0, 0);
     }


// Note, these clones were taken from the work by Cordy(2008)

// Original Code - Cordy
    void sumProdO(int n) {
        double sum=0.0; //C1
        double prod=1.0;
		int i;
        for (i=1; i<=n; i++)
        {
            sum=sum + i;
            prod = prod * i;
            foo2(sum, prod);
        }
    }

// Example 1A - Type 1 Clone - Cordy
    void sumProd1A(int n) {
		double sum=0.0; //C1
		double prod =1.0;
		int i;
		for (i=1; i<=n; i++)
		{
			sum=sum + i;
			prod = prod * i;
			foo2(sum, prod);
		}
  }

// Example 1B - Type 1 Clone - Cordy
	void sumProd1B(int n) {
		double sum=0.0; //C1
		double prod =1.0; //C
		int i;
		for (i=1; i<=n; i++)
		{
			sum=sum + i;
			prod = prod * i;
			foo2(sum, prod);
		}
	}

// Example 1C - Type 1 Clone - Cordy
    void sumProd1C(int n) {
        double sum=0.0; //C1
        double prod =1.0;
		int i;
        for (i=1; i<=n; i++) 
		{
            sum=sum + i;
            prod = prod * i;
            foo2(sum, prod);
        }
    }

// Example 2A - Type 2 Clone - Cordy
    void sumProd2A(int n){
        double s=0.0; //C1
        double p =1.0;
		int j;
        for (j=1; j<=n; j++)
        {
            s=s + j;
            p = p * j;
            foo2(s, p);
        }
    }

// Example 2B - Type 2 Clone - Cordy
    void sumProd2B(int n){
        double s=0.0; //C1
        double p =1.0;
		int j;
        for (j=1; j<=n; j++)
        {
            s=s + j;
            p = p * j;
            foo2(p, s);
        }
    }

// Example 2C - Type 2 Clone - Cordy
	void sumProd2C(int n) {
		int sum=0; //C1
		int prod =1;
		int i;
		for (i=1; i<=n; i++)
		{
			sum=sum + i;
			prod = prod * i;
			foo2(sum, prod);
		}
	}

// Example 2D - Type 2 Clone - Cordy
	void sumProd2D(int n) {
		double sum=0.0; //C1
		double prod =1.0;
		int i;
		for (i=1; i<=n; i++)
		{
			sum=sum + (i*i);
			prod = prod*(i*i);
			foo2(sum, prod);
		}
	}

// Example 3A - Type 3 Clone - Cordy
    void sumProd3A(int n) {
        double sum=0.0; //C1
        double prod =1.0;
		int i;
        for (i=1; i<=n; i++)
        {
            sum=sum + i;
            prod = prod * i;
            foo3(sum, prod, n);
        }
    }

// Example 3B - Type 3 Clone - Cordy
    void sumProd3B(int n) {
        double sum=0.0; //C1
        double prod =1.0;
		int i;
        for (i=1; i<=n; i++)
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
		int i;
        for (i=1; i<=n; i++)
        {
            sum=sum + i;
            prod = prod * i;
            if ((n % 2) == 0) {
                foo2(sum, prod);
            }
        }
    }           

// Example 3D - Type 3 Clone - Cordy
	void sumProd3D(int n) {
		double sum=0.0; //C1
		double prod =1.0;
		int i;
		for (i=1; i<=n; i++)
		{
			sum=sum + i;
			//line deleted
			foo2(sum, prod);
		}
	}

// Example 3E - Type 3 Clone - Cordy
// For syntax purposes, the precise functionality was altered.
	void sumProd3E(int n) {
		double sum=0.0; //C1
		double prod =1.0;
		int i;
		for (i=1; i<=n; i++)
		{
			if (i %2 == 0)
			{
				sum+= i;
			}
			prod = prod * i;
			foo2(sum, prod);
		}
	}

// Example 4a - Type 4 Clone - Cordy
    void sumProd4A(int n) {
        double prod =1.0;
        double sum=0.0; //C1
		int i;
        for (i=1; i<=n; i++)
        {
            sum=sum + i;
            prod = prod * i;
            foo2(sum, prod);
        }
    }

// Example 4B - Type 4 Clone - Cordy
    void sumProd4B(int n) {
        double sum=0.0; //C1
        double prod =1.0;
		int i;
        for (i=1; i<=n; i++)
        {
            prod = prod * i;
            sum=sum + i;
            foo2(sum, prod);
        }
    }

// Example 4C - Type 4 Clone - Cordy
    void sumProd4C(int n) {
        double sum=0.0; //C1
        double prod =1.0;
		int i;
        for (i=1; i<=n; i++)
        {
            sum=sum + i;
            foo2(sum, prod);
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
        }
    }




void main()
{
	printf("Hello world!\n");


	printf("mult %d \n", mult(5,2));

printf("Type1a_Krawitz: %lf \n", Type1a_Krawitz(4));
printf("Type1b_Krawitz: %lf \n", Type1b_Krawitz(4));
printf("Type2a_Krawitz: %lf \n", Type2a_Krawitz(4));
printf("Type2b_Krawitz: %lf \n", Type2b_Krawitz(4));
printf("Type3a_Krawitz: %lf \n", Type3a_Krawitz(4));
printf("Type3b_Krawitz: %lf \n", Type3b_Krawitz(4));
printf("Type4a_Krawitz: %lf \n", Type4a_Krawitz(4));
printf("Type4b_Krawitz: %lf \n", Type4b_Krawitz(4));
printf("Type4b2_Krawitz: %lf \n", Type4b2_Krawitz('-',3,3.0,4));

printf("\nsumProdO_Cordy: %lf ");
sumProdO(4);
printf("\nsumProd1A_Cordy: %lf ");
sumProd1A(4);
printf("\nsumProd1B_Cordy: %lf ");
sumProd1B(4);
printf("\nsumProd1C_Cordy: %lf ");
sumProd1C(4);
printf("\nsumProd2A_Cordy: %lf ");
sumProd2A(4);
printf("\nsumProd2B_Cordy: %lf ");
sumProd2B(4);
printf("\nsumProd2C_Cordy: %lf ");
sumProd2C(4);
printf("\nsumProd2D_Cordy: %lf ");
sumProd2D(4);
printf("\nsumProd3A_Cordy: %lf ");
sumProd3A(4);
printf("\nsumProd3B_Cordy: %lf ");
sumProd3B(4);
printf("\nsumProd3C_Cordy: %lf ");
sumProd3C(4);
printf("\nsumProd3D_Cordy: %lf ");
sumProd3D(4);
printf("\nsumProd3E_Cordy: %lf ");
sumProd3E(4);
printf("\nsumProd4A_Cordy: %lf ");
sumProd4A(4);
printf("\nsumProd4B_Cordy: %lf ");
sumProd4B(4);
printf("\nsumProd4C_Cordy: %lf ");
sumProd4C(4);
printf("\nsumProd4D_Cordy: %lf ");
sumProd4D(4);





//sumProd4A(4);



	printf("\n\n\n\n\n\n\n\n");
	system("pause");
}



