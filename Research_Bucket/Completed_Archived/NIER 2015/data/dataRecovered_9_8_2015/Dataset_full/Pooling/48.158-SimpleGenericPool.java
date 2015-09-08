
import org.enhydra.jdbc.pool.GenericPool;
import org.enhydra.jdbc.util.Logger;
import org.apache.commons.logging.LogFactory;

public class SimpleGenericPool {
    static public void main(String[] argv) throws Exception{

        GenericPool genPool = new GenericPool(new SimplePoolHelper(), 4);
        genPool.setLogger(new Logger(LogFactory.getLog("simplepool")));
        System.out.println("set the max size of the pool");
        genPool.setMaxSize(10);
        System.out.println("set the min size of the pool");
        genPool.setMinSize(2);

        System.out.println("start the pool");
        genPool.start();

        System.out.println("get a car from the pool");
        Car aCar = (Car)(genPool.checkOut(null, null));
        aCar.setBrand("Mercedes");
        aCar.setColor("black");

        System.out.println("get another car from the pool");
        Car anotherCar = (Car)(genPool.checkOut(null, null));
        anotherCar.setBrand("Porsche");
        anotherCar.setColor("red");

        System.out.println("stop the pool");
        genPool.stop();
        System.exit(1);
    }
}
