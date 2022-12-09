package service;

import Interfaces.ICourier;
import POJO.ItemInfo;
import POJO.ItemState;
import POJO.LogInfo;
import utils.MethodFactory;
import utils.SqlFactory;
import utils.annotations.Aggregated;
import utils.annotations.Update;


import java.sql.SQLException;
import java.util.function.Predicate;

/**
 * @author yjt
 * impl for courier
 */
public class CourierService implements ICourier {
    private final Predicate<LogInfo> identifyCheck =
            (id) -> id.type() == LogInfo.StaffType.Courier;


    private boolean checkItem(ItemInfo item){
        return item.retrieval() == null && item.$import() == null && item.export() == null && item.delivery() == null
                && item.state() == null && !MethodFactory.checkItemExist(item.name());
    }

    @Override
    @Update
    public boolean newItem(LogInfo log, ItemInfo item) {
        if (identifyCheck.test(log)){
            if (!checkItem(item)) {
                return false;
            }
            String sql = "insert into record(item_name, item_class, item_price, state, company_id) " +
                    "values (?,?,?,?,?)";
            try {
                SqlFactory.handleUpdate(sql, item.name(), item.$class(), item.price(), 1, MethodFactory.getCompanyId(log));
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
    @Update
    public boolean setItemState(LogInfo log, String name, ItemState s) {
        if (identifyCheck.test(log)){
            return true;
        }
        else {
            return false;
        }
    }
}
