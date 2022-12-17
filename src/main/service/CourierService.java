package main.service;

import main.interfaces.ICourier;
import main.interfaces.ItemInfo;
import main.interfaces.ItemState;
import main.interfaces.LogInfo;
import main.utils.MethodFactory;
import main.utils.SqlFactory;
import main.utils.annotations.Update;


import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author yjt
 * impl for courier
 */
public class CourierService implements ICourier {

    private final Predicate<LogInfo> identifyCheck = (id) -> id.type() == LogInfo.StaffType.Courier;
    private final Runnable role = () -> SqlFactory.setRole(LogInfo.StaffType.Courier);

    private boolean checkItem(ItemInfo item, LogInfo log) {
        return
                item.retrieval().courier() == null &&
                        item.name() != null &&
                        item.$class() != null &&
                        item.$import().officer() == null &&
                        item.export().officer() == null &&
                        item.delivery().courier() == null &&
                        item.state() == null &&
                        Objects.equals(item.retrieval().city(), MethodFactory.getCity(log)) &&
                        !MethodFactory.checkItemExist(item.name());
    }

    @Override
    @Update
    public boolean newItem(LogInfo log, ItemInfo item) {
        if (identifyCheck.test(log)) {
            role.run();
            if (!checkItem(item, log)) {
                return false;
            }
            String sql = "insert into record(item_name, item_class, item_price, state, company_id) " +
                    "values (?,?,?,?,?)";
            String sql2 = "insert into undertake (record_id,staff_id,city_id,type) values(?,?,?,1) ";
            return SqlFactory.handleUpdate(sql, item.name(), item.$class(), item.price(), 1, MethodFactory.getCompanyId(log))
                    && SqlFactory.handleUpdate(sql2,
                    MethodFactory.getRecordId(item.name()),
                    MethodFactory.getStaffId(log.name()),
                    MethodFactory.getCityId(log));
        } else {
            return false;
        }
    }

    @Override
    @Update
    public boolean setItemState(LogInfo log, String name, ItemState s) {
        if (identifyCheck.test(log)) {
            role.run();
            if (!MethodFactory.checkItemExist(name)) {
                return false;
            }
            if (s == SqlFactory.mapState(MethodFactory.getItemState(name))) {
                return false;
            }
            Integer itemState = MethodFactory.getItemState(name);
            Integer nextState = SqlFactory.mapStateToInt(s);
            if (itemState == null){
                return false;
            }
            if (nextState == null){
                return false;
            }
            if(nextState >= 4 && nextState <= 8) {
                return false;
            }
            if(itemState >= 4 && itemState <= 8) {
                return false;
            }
            if (MethodFactory.checkCourier(log, name,6) && itemState >= 9){
                if (nextState == 9){
                    if (itemState != 9){
                        return false;
                    }
                }else if (nextState == 10){
                    if (itemState != 9){
                        return false;
                    }
                }else if (nextState == 11){
                    if (itemState != 10){
                        return false;
                    }
                }
                String sql = """
                        select (select name from staff where id = u.staff_id) from undertake u where u.type = 6 and
                        u.record_id = (select id from record where item_name = ?)
                        """;
                try {
                    String courierName = SqlFactory.handleSingleResult(
                            SqlFactory.handleQuery(sql, name),
                            r -> r.getString(1)
                    );
                    if (courierName == null){
                        String sql2 = """
                                update undertake set
                                staff_id = (select id from staff where name = ?),
                                city_id = (select city_id from staff where name = ?)
                                where (type = 5 or type = 6) and record_id = (select id from record where item_name = ?)
                                """;
                        SqlFactory.handleUpdate(sql2,log.name(),log.name(),name);
                    }else if(!courierName.equals(log.name())){
                        return false;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }else if (MethodFactory.checkCourier(log, name,1) && itemState <= 2){
                if (nextState > 3 || nextState - itemState != 1){
                    return false;
                }
            }else {
                return false;
            }
            String sql = """
                    update record set state = ? where item_name = ?
                    """;
            return SqlFactory.handleUpdate(sql,nextState,name);
        } else {
            return false;
        }
    }
}
