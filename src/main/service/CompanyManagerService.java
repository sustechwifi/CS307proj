package main.service;

import main.interfaces.ICompanyManager;
import main.interfaces.LogInfo;
import main.utils.MethodFactory;
import main.utils.SqlFactory;
import main.utils.annotations.Multiple;
import main.utils.annotations.Update;


import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author yjt
 * impl for companyManager
 */
public class CompanyManagerService implements ICompanyManager {

    private final Predicate<LogInfo> identifyCheck = (id) -> id.type() == LogInfo.StaffType.CompanyManager;

    private final Runnable role = () -> SqlFactory.setRole(LogInfo.StaffType.CompanyManager);


    @Multiple(sql = """
            select u.tax , r.item_price from undertake u
            join record r on u.record_id = r.id
            where u.type = ? and r.item_class = ? and u.city_id =
            (select c.id from city c where c.name = ?)
            """)
    public double getTaxRate(String city, String itemClass, int type,String format) {
        try {
            return SqlFactory.query(
                    this.getClass().getMethod("getTaxRate", String.class, String.class, int.class,String.class),
                    r -> r.getDouble(1) / r.getLong(2),
                    res -> Double.parseDouble(String.format(format, res.stream()
                            .collect(Collectors.summarizingDouble(Double::doubleValue))
                            .getAverage())),
                    type, itemClass, city);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public double getImportTaxRate(LogInfo log, String city, String itemClass) {
        if (identifyCheck.test(log)) {
            role.run();
            return getTaxRate(city, itemClass, 4,"%.15f");
        } else {
            return -1;
        }
    }

    @Override
    public double getExportTaxRate(LogInfo log, String city, String itemClass) {
        if (identifyCheck.test(log)) {
            role.run();
            return getTaxRate(city, itemClass, 3,"%.18f");
        } else {
            return -1;
        }
    }

    @Override
    @Update
    public boolean loadItemToContainer(LogInfo log, String itemName, String containerCode) {
        if (identifyCheck.test(log)) {
            role.run();
            System.out.println("begin:");
            Integer id = MethodFactory.getEmptyContainer(containerCode);
            if (id == null){
                return false;
            }
            Integer state = MethodFactory.getItemState(itemName);
            if (state == null || state != 4){
                return false;
            }
            String sql1 = """
                    update record set state = 5,
                    container_id = ?
                    where item_name = ?
                    """;
            String sql2 = """
                    update container set state = 1
                    where code = ?
                    """;
            return SqlFactory.handleUpdate(sql1, id,  itemName) &&
                    SqlFactory.handleUpdate(sql2, containerCode);
        } else {
            return false;
        }
    }

    @Override
    @Update
    public boolean loadContainerToShip(LogInfo log, String shipName, String containerCode) {
        if (identifyCheck.test(log)) {
            role.run();
            Integer shipState = MethodFactory.getShipState(shipName);
            Integer containerState = MethodFactory.getContainerState(containerCode);
            if(shipState == null || shipState != 0){
                return false;
            }
            if (containerState == null || containerState != 1){
                return false;
            }
            String sql1 = """
                    update container set ship_id =
                    (select s.id from ship s where s.name = ? and s.state = 0)
                    where code = ? and state = 1
                    """;

            return SqlFactory.handleUpdate(sql1, shipName, containerCode);
        } else {
            return false;
        }
    }

    @Override
    @Update
    public boolean shipStartSailing(LogInfo log, String shipName) {
        if (identifyCheck.test(log)) {
            role.run();
            Integer shipState = MethodFactory.getShipState(shipName);
            if (shipState == null || shipState == 1){
                return false;
            }
            if (!MethodFactory.checkLoaded(shipName)){
                return false;
            }
            String sql1 = "update ship set state = 1 where name = ?";
            String sql2 = """
                    update record set state = 6
                    where state = 5 and container_id = (
                        select c.id from container c where c.ship_id = (
                            select s.id from ship s where s.name = ?
                        )
                    )
                    """;
            return SqlFactory.handleUpdate(sql1, shipName) &&
                    SqlFactory.handleUpdate(sql2,  shipName);
        } else {
            return false;
        }
    }

    @Override
    @Update
    public boolean unloadItem(LogInfo log, String itemName) {
        if (identifyCheck.test(log)) {
            role.run();
            Integer itemState = MethodFactory.getItemState(itemName);
            if (itemState == null || itemState != 6) {
                return false;
            }
            String sql = """
                    update record set state = 7
                    where item_name = ?
                    """;
            return SqlFactory.handleUpdate(sql,  itemName);
        } else {
            return false;
        }
    }

    @Override
    @Update
    public boolean itemWaitForChecking(LogInfo log, String item) {
        if (identifyCheck.test(log)) {
            role.run();
            Integer itemState = MethodFactory.getItemState(item);
            if (itemState == null || itemState != 7) {
                return false;
            }
            String sql = """
                    update record set state = 8
                    where item_name = ?
                    """;
            return SqlFactory.handleUpdate(sql, item);
        } else {
            return false;
        }
    }
}
