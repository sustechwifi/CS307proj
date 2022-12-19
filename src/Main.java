import main.utils.DatabaseManipulationProxy;
import main.utils.JdbcUtil;
import main.utils.MethodInterFaces;
import main.utils.annotations.SqlSupport;

import java.lang.reflect.Proxy;

@SqlSupport
public class Main {
    public static void main(String[] args) {
        JdbcUtil.getConnection(Main.class);
        MethodInterFaces m = (MethodInterFaces) Proxy.newProxyInstance(Main.class.getClassLoader(),new Class[]{MethodInterFaces.class},new DatabaseManipulationProxy());
        System.out.println(m.getItemState("orange-90b43"));

    }
}
