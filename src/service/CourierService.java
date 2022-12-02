package service;

import Interfaces.ICourier;
import POJO.ItemInfo;
import POJO.ItemState;
import POJO.LogInfo;
import utils.MethodFactory;
import utils.SqlFactory;
import utils.annotations.Aggregated;


import java.sql.SQLException;
import java.util.function.Predicate;

/**
 * @author yjt
 * impl for courier
 */
public class CourierService implements ICourier {
    private final Predicate<LogInfo> identifyCheck =
            (id) -> id.type() == LogInfo.StaffType.Courier;

    @Aggregated(sql = "select s.company_id from staff s where s.name = ?")
    public int getCompanyId(LogInfo log){
        try {
            return SqlFactory.query(
                    this.getClass().getMethod("getCompanyId", LogInfo.class),
                    r -> r.getInt(1),
                    log.name());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private boolean checkItem(ItemInfo item){
        return item.retrieval() == null && item.$import() == null && item.export() == null && item.delivery() == null
                && item.state() == null && !MethodFactory.checkItemExist(item.name());
    }

    @Override
    public boolean newItem(LogInfo log, ItemInfo item) {
        if (identifyCheck.test(log)){
            if (!checkItem(item)) {
                return false;
            }
            String sql = "insert into record(item_name, item_class, item_price, state, company_id) " +
                    "values (?,?,?,?,?)";
            try {
                SqlFactory.handleUpdate(sql, item.name(), item.$class(), item.price(), 1, getCompanyId(log));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean setItemState(LogInfo log, String name, ItemState s) {
        if (identifyCheck.test(log)){
            return true;
        }
        else {
            return false;
        }
    }
}
