package service;

import Interfaces.ISustcManager;
import POJO.*;
import utils.SqlFactory;
import utils.Wrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author yjt
 */
public class SustcManagerService implements ISustcManager {

    private final Predicate<LogInfo> identifyCheck =
            (id) -> id.type() == LogInfo.StaffType.SustcManager;


    private int handleCount(String type) {
        String sql = String.format("select count(id) from %s", type);
        try {
            return SqlFactory.handleSingleResult(
                    SqlFactory.handleQuery(sql, null),
                    r -> {
                        try {
                            return r.getInt(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int getCompanyCount(LogInfo log) {
        if (identifyCheck.test(log)) {
            return handleCount("company");
        } else {
            return -1;
        }
    }

    @Override
    public int getCityCount(LogInfo log) {
        if (identifyCheck.test(log)) {
            return handleCount("city");
        } else {
            return -1;
        }
    }

    @Override
    public int getCourierCount(LogInfo log) {
        if (identifyCheck.test(log)) {
            String sql = "select count(id) from staff where type = 4";
            try {
                return SqlFactory.handleSingleResult(
                        SqlFactory.handleQuery(sql, null),
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
        } else {
            return -1;
        }
    }

    @Override
    public int getShipCount(LogInfo log) {
        if (identifyCheck.test(log)) {
            return handleCount("ship");
        } else {
            return -1;
        }
    }

    private <O> O getRetrievalDelivery(String itemName, int type, String model, Function<ResultSet, O> func) throws SQLException {
        String sql = String.format(model, type);
        return SqlFactory.handleSingleResult(
                SqlFactory.handleQuery(sql, new Wrapper[]{
                        new Wrapper<>(String.class, itemName)
                }),
                func
        );
    }


    @Override
    public ItemInfo getItemInfo(LogInfo log, String name) {
        if (identifyCheck.test(log)) {
            String sql = "select * from record where item_name = ?";
            String model = "select (select c.name from city c where c.id = u.city_id) city, s.name , u.tax" +
                    "       from undertake u join staff s on u.staff_id = s.id " +
                    "      where u.type = %d and  " +
                    "      record_id = (select r.id from record r where item_name = ?)";

            try {
                ItemInfo info = SqlFactory.handleSingleResult(
                        SqlFactory.handleQuery(sql, new Wrapper[]{
                                new Wrapper<>(String.class, name)
                        }),
                        r -> {
                            try {
                                return new ItemInfo(
                                        r.getString(1),
                                        r.getString(2),
                                        r.getDouble(3),
                                        SqlFactory.mapState(r.getInt(4)),
                                        null, null, null, null
                                );
                            } catch (SQLException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                );
                var retrieval = getRetrievalDelivery(name, 1, model,
                        r -> {
                            try {
                                return new ItemInfo.RetrievalDeliveryInfo(
                                        r.getString(1),
                                        r.getString(2)
                                );
                            } catch (SQLException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        });
                var export = getRetrievalDelivery(name, 3, model,
                        r -> {
                            try {
                                return new ItemInfo.ImportExportInfo(
                                        r.getString(1),
                                        r.getString(2),
                                        r.getDouble(3)
                                );
                            } catch (SQLException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        });
                var $import = getRetrievalDelivery(name, 4, model,
                        r -> {
                            try {
                                return new ItemInfo.ImportExportInfo(
                                        r.getString(1),
                                        r.getString(2),
                                        r.getDouble(3)
                                );
                            } catch (SQLException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        });
                var delivery = getRetrievalDelivery(name, 6, model,
                        r -> {
                            try {
                                return new ItemInfo.RetrievalDeliveryInfo(
                                        r.getString(1),
                                        r.getString(2)
                                );
                            } catch (SQLException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        });
                return new ItemInfo(info.name(), info.$class(), info.price(), info.state(), retrieval, delivery, $import, export);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public ShipInfo getShipInfo(LogInfo log, String name) {
        if (identifyCheck.test(log)) {
            String sql = "select s.name,c.name ,s.state from ship s join company c on s.company_id = c.id " +
                    "where s.name = ?";
            try {
                return SqlFactory.handleSingleResult(
                        SqlFactory.handleQuery(sql, new Wrapper[]{
                                new Wrapper<>(String.class, name)
                        }),
                        r -> {
                            try {
                                return new ShipInfo(
                                        r.getString(1),
                                        r.getString(2),
                                        r.getInt(3) == 1);
                            } catch (SQLException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
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
    public ContainerInfo getContainerInfo(LogInfo log, String code) {
        if (identifyCheck.test(log)) {
            String sql = "select c.type,c.code,c.state from container c where c.code = ?";
            try {
                return SqlFactory.handleSingleResult(
                        SqlFactory.handleQuery(sql, new Wrapper[]{
                                new Wrapper<>(String.class, code)
                        }),
                        r -> {
                            try {
                                return new ContainerInfo(
                                        SqlFactory.mapContainerType(r.getString(1)),
                                        r.getString(2),
                                        r.getInt(3) == 1);
                            } catch (SQLException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
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
    public StaffInfo getStaffInfo(LogInfo log, String name) {
        if (identifyCheck.test(log)) {
            String sql = """
                    select s.type , s.name , c.name,
                           (select ci.name from city ci where ci.id = s.city_id)
                         , s.gender, (2022 - s.birth_year), s.phone, s.password
                    from staff s
                             join company c on s.company_id = c.id
                    where s.name = ?;""";
            try {
                return SqlFactory.handleSingleResult(
                        SqlFactory.handleQuery(sql, new Wrapper[]{
                                new Wrapper<>(String.class, name)
                        }),
                        r -> {
                            try {
                                return new StaffInfo(
                                        new LogInfo(
                                                r.getString(2),
                                                SqlFactory.mapStaffType(r.getInt(1)),
                                                r.getString(8)
                                        ),
                                        r.getString(2),
                                        r.getString(3),
                                        r.getInt(4) == 0,
                                        r.getInt(5),
                                        r.getString(6)
                                );
                            } catch (SQLException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                );
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
}
