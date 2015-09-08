package st.ata.util;

import java.util.Arrays;

public class TestTextPool {

    public static void main(String[] argv) {

        System.out.print("TestTextPool") ;

        TextPool tPool1 = new TextPool() ;
        TextPool tPool2 = new TextPool() ;

        byte[] buffer = new byte[9 * 1024] ;

        Text testText1 = new Text(buffer) ;

        Text testText2 = tPool1.copy(testText1) ;

        X.check (testText1.equals(testText2) ) ;
        X.check ( (tPool1.copy("Matlabi Duniya")).equals("Matlabi Duniya") ) ;
      	System.out.print(".") ;

        TextPool.OutputStream out = tPool2.getOutputStream(15) ;
        int byteswritten = 0;

        try {

            out.write(65) ;
            byteswritten++ ;

            Arrays.fill(buffer, (byte)65) ;

            out.write (buffer, 0, buffer.length ) ;
            byteswritten += buffer.length ;
      		System.out.print(".") ;

            out.write (testText1) ;
            byteswritten += (testText1.end - testText1.start) ;

            testText2 = out.done() ;

            X.check ( testText2.end == byteswritten ) ;
      		System.out.print(".") ;
            out.close() ;

            tPool2.reset() ;
	    System.out.print(".") ;

        } catch (Exception e) {
            throw X.toRTE(e);
        }
        System.out.println() ;

    }

}
