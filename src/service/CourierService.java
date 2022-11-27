package service;

import Interfaces.ICourier;
import POJO.CompanyInfo;
import POJO.ItemInfo;
import POJO.ItemState;
import POJO.LogInfo;
import utils.Condition;
import utils.SqlFactory;
import utils.Wrapper;

import java.sql.SQLException;
import java.util.function.Predicate;

/**
 * @author yjt
 * impl for courier
 */
public class CourierService implements ICourier {
    private final Predicate<LogInfo> identifyCheck =
            (id) -> id.type() == LogInfo.StaffType.Courier;

    private int getCompanyId(LogInfo log){
        String sql = "select s.company_id from staff s where s.name = ?";
        try {
            return SqlFactory.handleSingleResult(
                    SqlFactory.handleQuery(sql,
                            new Wrapper[]{
                                    new Wrapper<>(String.class,log.name())
                            }
                    ),
                    r -> {
                        try {
                            return r.getInt(1);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean newItem(LogInfo log, ItemInfo item) {
        if (identifyCheck.test(log)){
            if (!SqlFactory.checkCondition(new Condition[]{
                    new Condition<>(item.retrieval(),null),
                    new Condition<>(item.$import(),null),
                    new Condition<>(item.export(),null),
                    new Condition<>(item.delivery(),null),
                    new Condition<>(item.state(),null),
                    new Condition<>(SqlFactory.checkItemExist(item.name()),false)
            })) {
                return false;
            }
            String sql = "insert into record(item_name, item_class, item_price, state, company_id) " +
                    "values (?,?,?,?,?)";
            try {
                SqlFactory.handleUpdate(sql, new Wrapper[]{
                        new Wrapper<>(String.class,item.name()),
                        new Wrapper<>(String.class,item.$class()),
                        new Wrapper<>(double.class,item.price()),
                        new Wrapper<>(int.class,1),
                        new Wrapper<>(int.class,getCompanyId(log)),
                });
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
