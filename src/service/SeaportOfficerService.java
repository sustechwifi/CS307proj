package service;

import Interfaces.ISeaportOfficer;
import POJO.LogInfo;
import utils.MethodFactory;
import utils.SqlFactory;
import utils.annotations.Multiple;
import utils.annotations.Update;

import java.sql.SQLException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * seaport officer impl
 *
 * @author yjt
 */
public class SeaportOfficerService implements ISeaportOfficer {

    private final Predicate<LogInfo> identifyCheck =
            (id) -> id.type() == LogInfo.StaffType.SeaportOfficer;


    @Override
    @Multiple(sql = """
            select r.item_name from record r join undertake u on r.id = u.record_id
            where (r.state = 3 and u.type = 3 and u.city_id = ?)
               or (r.state = 8 and u.type = 4 and u.city_id = ?)
            """)
    public String[] getAllItemsAtPort(LogInfo log) {
        if (identifyCheck.test(log)) {
            int cityId = MethodFactory.getCityId(log);
            try {
                return SqlFactory.query(
                        this.getClass().getMethod("getAllItemsAtPort", LogInfo.class),
                        r -> r.getString(1),
                        arr -> arr.toArray(String[]::new),
                        cityId, cityId
                );
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    @Update
    public boolean setItemCheckState(LogInfo log, String itemName, boolean success) {
        if (identifyCheck.test(log)) {
            if (MethodFactory.checkItemExist(itemName)) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
}
