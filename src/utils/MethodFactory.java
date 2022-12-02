package utils;

import utils.annotations.Aggregated;

/**
 * @author yjt
 */
public class MethodFactory {

    private static Class<?> clazz;

    static {
        try {
            clazz = Class.forName("utils.MethodFactory");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Aggregated(sql = "select count(id) from record where item_name = ?")
    public static boolean checkItemExist(String item) {
        try {
            return SqlFactory.query(
                    clazz.getMethod("checkItemExist", String.class),
                    r -> r.getInt(1) == 1,
                    item
                    );
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
