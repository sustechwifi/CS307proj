package utils;

import POJO.LogInfo;
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

    @Aggregated(sql = "select city_id from staff where name = ? ")
    public static int getCityId(LogInfo log) {
        try {
            return SqlFactory.query(
                    clazz.getMethod("getCityId",LogInfo.class),
                    r -> r.getInt(1),
                    log.name()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Aggregated(sql = "select s.company_id from staff s where s.name = ?")
    public static int getCompanyId(LogInfo log){
        try {
            return SqlFactory.query(
                    clazz.getMethod("getCompanyId", LogInfo.class),
                    r -> r.getInt(1),
                    log.name());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

}
