package utils;

import POJO.ContainerInfo;
import POJO.ItemState;
import POJO.LogInfo;
import utils.annotations.Aggregated;
import utils.annotations.Multiple;


import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SqlFactory {

    private static void handlePara(PreparedStatement p, Object w, int index) throws SQLException {
        if (w instanceof Integer) {
            p.setInt(index, (Integer) w);
        } else if (w instanceof String) {
            p.setString(index, (String) w);
        } else if (w instanceof Long) {
            p.setLong(index, (long) w);
        } else if (w instanceof Double) {
            p.setDouble(index, (double) w);
        }
    }

    private static <O> O loadCondition(String sql,
                                       Object[] conditions,
                                       BiFunction<Connection, PreparedStatement, O> f)
            throws SQLException {
        Connection connection = JdbcUtil.connection;
        var p = connection.prepareStatement(sql);
        if (conditions != null) {
            for (int i = 0; i < conditions.length; i++) {
                handlePara(p, conditions[i], i + 1);
            }
        }
        return f.apply(connection, p);
    }

    public static SqlResult handleQuery(String sql, Object... conditions) throws SQLException {
        return loadCondition(sql, conditions, (con, p) -> {
            try {
                return new SqlResult(p.executeQuery());
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public static void handleUpdate(String sql, Object... conditions) throws SQLException {
        loadCondition(sql, conditions, (con, p) -> {
            try {
                p.executeUpdate();
                con.commit();
                return 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        });
    }

    public static <I, O> O handleMultipleResult(SqlResult resultSet,
                                                Function<SqlResult, I> map,
                                                Function<Collection<I>, O> transform)
            throws SQLException {
        var tmp = new ArrayList<I>();
        if (resultSet.next()) {
            do {
                tmp.add(map.apply(resultSet));
            } while (resultSet.next());
        }
        return transform.apply(tmp);
    }

    public static <O> O handleSingleResult(SqlResult resultSet, Function<SqlResult, O> map)
            throws SQLException {
        resultSet.next();
        return map.apply(resultSet);
    }


    public static <T, O> O query(Method method, Function<SqlResult, O> map,Object... args) {
        try {
            if (method.isAnnotationPresent(Aggregated.class)) {
                Aggregated init = method.getAnnotation(Aggregated.class);
                String sql = init.sql();
                return handleSingleResult(handleQuery(sql,args), map);
            }else {
                throw new RuntimeException("need annotation");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T, I, O> O query(Method method,
                                    Function<SqlResult, I> map,
                                    Function<Collection<I>, O> transform,
                                    Object... args) {
        try {
            if (method.isAnnotationPresent(Multiple.class)) {
                Multiple init = method.getAnnotation(Multiple.class);
                String sql = init.sql();
                return handleMultipleResult(handleQuery(sql,args), map, transform);
            }else {
                throw new RuntimeException("need annotation");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemState mapState(int state) {
        return switch (state) {
            case 1 -> ItemState.PickingUp;
            case 2 -> ItemState.ToExportTransporting;
            case 3 -> ItemState.ExportChecking;
            case 4 -> ItemState.ExportCheckFailed;
            case 5 -> ItemState.PackingToContainer;
            case 6 -> ItemState.WaitingForShipping;
            case 7 -> ItemState.Shipping;
            case 8 -> ItemState.UnpackingFromContainer;
            case 9 -> ItemState.ImportChecking;
            case 10 -> ItemState.ImportCheckFailed;
            case 11 -> ItemState.FromImportTransporting;
            case 12 -> ItemState.Delivering;
            case 13 -> ItemState.Finish;
            default -> null;
        };
    }

    public static ContainerInfo.Type mapContainerType(String type) {
        return switch (type) {
            case "Dry Container" -> ContainerInfo.Type.Dry;
            case "Open Top Container" -> ContainerInfo.Type.OpenTop;
            case "ISO Tank Container" -> ContainerInfo.Type.ISOTank;
            case "Reefer Container" -> ContainerInfo.Type.Reefer;
            case "Flat Rack Container" -> ContainerInfo.Type.FlatRack;
            default -> null;
        };
    }

    public static LogInfo.StaffType mapStaffType(int type) {
        return switch (type) {
            case 1 -> LogInfo.StaffType.SustcManager;
            case 2 -> LogInfo.StaffType.CompanyManager;
            case 3 -> LogInfo.StaffType.SeaportOfficer;
            case 4 -> LogInfo.StaffType.Courier;
            default -> null;
        };
    }


}
