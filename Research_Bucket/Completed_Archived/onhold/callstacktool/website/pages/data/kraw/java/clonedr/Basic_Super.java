
public class Basic_Super {

	Basic_Class1 bc1 = new Basic_Class1();
	
	public static void main(String[] args) {
		Basic_Super sb = new Basic_Super();
		int a =0;
		int b =3;
		sb.Run(a ,b);
	}
	
	public void Run(int a, int b){
		
		bc1.foo1(a); // dummy non-clone function #1
		
		bc1.foo2(a,b); // dummy non-clone function #2
		
		bc1.foo3(a); // dummy non-clone function #3
		
		bc1.foo4(a); // dummy non-clone function #4

		bc1.Type1a_Krawitz(a);
		bc1.Type1b_Krawitz(a);
		
		bc1.Type2a_Krawitz(a);
		bc1.Type2b_Krawitz(a);
		
		bc1.Type3a_Krawitz(a);
		bc1.Type3b_Krawitz(a);
		
		bc1.Type4a_Krawitz(a);
		bc1.Type4b_Krawitz(a);
		

		
		bc1.sumProdO(a); // run original version
		bc1.sumProd1A(a); // Version 1A
		bc1.sumProd1B(a); // Version 1B
		bc1.sumProd1C(a); // Version 1C
		bc1.sumProd2A(a); // Version 2A
		bc1.sumProd2B(a); // Version 2B
		bc1.sumProd2C(a); // Version 2C
		bc1.sumProd2D(a); // Version 2D
		bc1.sumProd3A(a); // Version 3A
		bc1.sumProd3B(a); // Version 3B
		bc1.sumProd3C(a); // Version 3C	// Crashes
		bc1.sumProd3D(a); // Version 3D
		bc1.sumProd3E(a); // Version 3E
		bc1.sumProd4A(a); // Version 4A
		bc1.sumProd4B(a); // Version 4B
		bc1.sumProd4C(a); // Version 4C
		bc1.sumProd4D(a); // Version 4D
		
		
		
	}

}
