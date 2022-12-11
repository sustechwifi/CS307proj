package utils;

import main.interfaces.LogInfo;
import utils.annotations.Aggregated;

import java.util.function.Predicate;


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

    @Aggregated(sql = "select state from record where item_name = ?")
    public static boolean checkItemState(String item, Predicate<Integer> condition){
        try {
            return SqlFactory.query(
                    clazz.getMethod("checkItemState", String.class,Predicate.class),
                    r -> condition.test(r.getInt(1)),
                    item
            );
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    @Aggregated(sql = "select state from record where item_name = ?")
    public static Integer getItemState(String item){
        try {
            return SqlFactory.query(
                    clazz.getMethod("getItemState", String.class),
                    r -> r.getInt(1),
                    item
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Aggregated(sql = "select city_id from staff where name = ? ")
    public static Integer getCityId(LogInfo log) {
        try {
            return SqlFactory.query(
                    clazz.getMethod("getCityId", LogInfo.class),
                    r -> r.getInt(1),
                    log.name()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    @Aggregated(sql = "select name from city where id = (select city_id from staff where name = ?) ")
    public static String getCity(LogInfo log) {
        try {
            return SqlFactory.query(
                    clazz.getMethod("getCity", LogInfo.class),
                    r -> r.getString(1),
                    log.name()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Aggregated(sql = """
             select (
                 select city_id from undertake
                 where record_id = (select id from record where item_name = ?)
                 and type = ?
                 ) =
                 (
                 select city_id from staff
                 where name = ?
                 );
            """)
    public static boolean checkCourier(LogInfo log,String name,int type){
        try {
            return SqlFactory.query(
                    clazz.getMethod("checkCourier", LogInfo.class,String.class,int.class),
                    r -> r.getBoolean(1),
                    name,type,log.name()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Aggregated(sql = "select id from container where state = 0 and code = ?")
    public static Integer getEmptyContainer(String code) {
        try {
            return SqlFactory.query(
                    clazz.getMethod("getEmptyContainer", String.class),
                    r -> r.getInt(1),
                    code
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Aggregated(sql = "select state from ship where name = ?")
    public static Integer getShipState(String name) {
        try {
            return SqlFactory.query(
                    clazz.getMethod("getShipState", String.class),
                    r -> r.getInt(1),
                    name
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Aggregated(sql = "select state from container where code = ?")
    public static Integer getContainerState(String code) {
        try {
            return SqlFactory.query(
                    clazz.getMethod("getContainerState", String.class),
                    r -> r.getInt(1),
                    code
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Aggregated(sql = """
            select count(c.id) from container c where c.ship_id = (
                            select s.id from ship s where s.name = ?
                        )
            """)
    public static boolean checkLoaded(String name) {
        try {
            return SqlFactory.query(
                    clazz.getMethod("checkLoaded", String.class),
                    r -> r.getInt(1) > 0,
                    name
            );
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Aggregated(sql = "select id from staff where name = ?")
    public static Integer getStaffId(String name){
        try {
            return SqlFactory.query(
                    clazz.getMethod("getStaffId", String.class),
                    r -> r.getInt(1),
                    name
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Aggregated(sql = "select id from record where item_name = ?")
    public static Integer getRecordId(String name){
        try {
            return SqlFactory.query(
                    clazz.getMethod("getRecordId", String.class),
                    r -> r.getInt(1),
                    name
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
