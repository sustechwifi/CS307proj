package service;

import Interfaces.ISeaportOfficer;
import POJO.LogInfo;
import utils.MethodFactory;
import utils.SqlFactory;

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

    private int getCityId(LogInfo log) {
        String sql = "select city_id from staff where name = ? ";
        try {
            return SqlFactory.handleSingleResult(
                    SqlFactory.handleQuery(sql, log.name()),
                    r -> r.getInt(1)
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public String[] getAllItemsAtPort(LogInfo log) {
        if (identifyCheck.test(log)) {
            String sql = "select r.item_name from record r join undertake u on r.id = u.record_id\n" +
                    "where (r.state = 3 and u.type = 3 and u.city_id = ?)\n" +
                    "   or (r.state = 8 and u.type = 4 and u.city_id = ?)";
            int cityId = getCityId(log);
            try {
                return SqlFactory.handleMultipleResult(
                        SqlFactory.handleQuery(sql, cityId, cityId),
                        r -> r.getString(1),
                        arr -> arr.toArray(String[]::new)
                );
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
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
