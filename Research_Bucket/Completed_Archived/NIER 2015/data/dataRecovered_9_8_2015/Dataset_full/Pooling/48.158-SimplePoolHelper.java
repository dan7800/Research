
import org.enhydra.jdbc.pool.PoolHelper;
import org.enhydra.jdbc.pool.GenerationObject;

import java.sql.SQLException;

public class SimplePoolHelper implements PoolHelper {
    public void expire(Object o) {
        ((Car)o).setBrand(null);
        ((Car)o).setColor(null);
        o = null;
    }

    public boolean checkThisObject(Object o) {
        return o != null;
    }

    public boolean testThisObject(Object o) {
        return (o != null) &
                (((Car)o).getColor() != null) &
                (((Car)o).getBrand() != null);
    }

    public GenerationObject create() throws SQLException {
        return new GenerationObject(new Car(), 0, null, null);
    }

    public GenerationObject create(String _user, String _password) throws SQLException {
        return new GenerationObject(new Car(), 0, _user, _password);
    }
}
