package service;

import Interfaces.ICompanyManager;
import POJO.LogInfo;
import utils.SqlFactory;
import utils.annotations.Aggregated;
import utils.annotations.Multiple;
import utils.annotations.Update;


import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author yjt
 * impl for companyManager
 */
public class CompanyManagerService implements ICompanyManager {

    private final Predicate<LogInfo> identifyCheck =
            (id) -> id.type() == LogInfo.StaffType.CompanyManager;


    @Multiple(sql = """
            select u.tax , r.item_price from undertake u
            join record r on u.record_id = r.id
            where u.type = ? and r.item_class = ? and u.city_id =
            (select c.id from city c where c.name = ?)
            """)
    private double getTaxRate(String city, String itemClass, int type) {
        try {
            return SqlFactory.query(
                    this.getClass().getMethod("getTaxRate", String.class, String.class, int.class),
                    (r) -> r.getDouble(1) / r.getLong(2),
                    (res) -> res.stream()
                            .collect(Collectors.summarizingDouble(Double::doubleValue))
                            .getAverage(),
                    type, itemClass, city);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public double getImportTaxRate(LogInfo log, String city, String itemClass) {
        if (identifyCheck.test(log)) {
            return getTaxRate(city, itemClass, 4);
        } else {
            return -1;
        }
    }

    @Override
    public double getExportTaxRate(LogInfo log, String city, String itemClass) {
        if (identifyCheck.test(log)) {
            return getTaxRate(city, itemClass, 3);
        } else {
            return -1;
        }
    }

    @Override
    @Update
    public boolean loadItemToContainer(LogInfo log, String itemName, String containerCode) {
        if (identifyCheck.test(log)) {
            String sql = "update record set state = ?," +
                    "container_id = (select c.id from container c where c.code = ? and c.state = 0) " +
                    "where state = ? and item_name = ?";
            try {
                SqlFactory.handleUpdate(sql, 4, containerCode, 3, itemName);
                System.out.println("update successfully");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    @Update
    public boolean loadContainerToShip(LogInfo log, String shipName, String containerCode) {
        if (identifyCheck.test(log)) {
            String sql1 = "update container set ship_id = " +
                    "(select s.id from ship s where s.name = ? and s.state = 0) " +
                    "where code = ? and state = 0";

            String sql2 = "update record set state = 5 " +
                    "where state = ? and container_id = (select c.id from container c where c.code = ? and c.state = 0)";
            try {
                SqlFactory.handleUpdate(sql1, shipName, containerCode);
                SqlFactory.handleUpdate(sql2, 4, containerCode);
                System.out.println("update successfully");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    @Update
    public boolean shipStartSailing(LogInfo log, String shipName) {
        if (identifyCheck.test(log)) {
            String sql1 = "update ship set state = 1 where name = ?";
            String sql2 = "update record set state = 6 " +
                    "where state = ? and container_id = " +
                    "(select c.id from container c where c.ship_id = " +
                    "(select s.id from ship s where s.name = ?))";
            try {
                SqlFactory.handleUpdate(sql1, shipName);
                SqlFactory.handleUpdate(sql2, 5, shipName);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    @Update
    public boolean unloadItem(LogInfo log, String itemName) {
        if (identifyCheck.test(log)) {
            String sql = "update record set state = ? " +
                    "where item_name = ? and state = 7";
            try {
                SqlFactory.handleUpdate(sql, 8, itemName);
                System.out.println("update successfully");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    @Update
    public boolean itemWaitForChecking(LogInfo log, String item) {
        if (identifyCheck.test(log)) {
            String sql = "update record set state = ? " +
                    "where item_name = ? and state = ?";
            try {
                SqlFactory.handleUpdate(sql, 9, item, 8);
                System.out.println("update successfully");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }
}
