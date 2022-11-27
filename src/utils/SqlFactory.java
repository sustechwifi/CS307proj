package utils;

import POJO.ContainerInfo;
import POJO.ItemState;
import POJO.LogInfo;
import POJO.StaffInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class SqlFactory {

    private static <T> void handlePara(PreparedStatement p, Wrapper<T> w, int index) throws SQLException {
        if (w.type().equals(int.class)) {
            p.setInt(index, (int)w.value());
        } else if (w.type().equals(String.class)) {
            p.setString(index, (String) w.value());
        } else if (w.type().equals(Long.class)) {
            p.setLong(index, (long) w.value());
        } else if (w.type().equals(double.class)){
            p.setDouble(index, (double) w.value());
        }
    }

    private static <I,O> O loadCondition(String sql,
                                       Wrapper<I>[] conditions,
                                       BiFunction<Connection, PreparedStatement, O> f)
            throws SQLException {
        Connection connection = JdbcUtil.getConnection();
        var p = connection.prepareStatement(sql);
        if(conditions != null){
            for (int i = 0; i < conditions.length; i++) {
                handlePara(p, conditions[i],  i + 1);
            }
        }
        return f.apply(connection, p);
    }

    public static <I> ResultSet handleQuery(String sql, Wrapper<I>[] conditions) throws SQLException {
        return loadCondition(sql, conditions, (con, p) -> {
            try {
                return p.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public static <I> void handleUpdate(String sql, Wrapper[] conditions) throws SQLException {
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

    public static <I, O> O handleMultipleResult(ResultSet resultSet,
                                           Function<ResultSet, I> map,
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

    public static <O> O handleSingleResult(ResultSet resultSet, Function<ResultSet, O> map)
            throws SQLException {
        resultSet.next();
        return map.apply(resultSet);
    }

    public static boolean checkCondition(Condition[] conditions){
        for (var condition : conditions) {
            if (!condition.input().equals(condition.expect())) {
                return false;
            }
        }
        return true;
    }

    public static  boolean checkItemExist(String item){
        String sql = "select count(id) from record where item_name = ?";
        try {
            return SqlFactory.handleSingleResult(
                    SqlFactory.handleQuery(sql,
                            new Wrapper[]{
                                    new Wrapper<>(String.class,item)
                            }
                    ),
                    r -> {
                        try {
                            return r.getInt(1) == 1;
                        } catch (SQLException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static ItemState mapState(int state){
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

    public static ContainerInfo.Type mapContainerType(String type){
        return switch (type) {
            case "Dry Container" -> ContainerInfo.Type.Dry;
            case "Open Top Container" -> ContainerInfo.Type.OpenTop;
            case "ISO Tank Container" -> ContainerInfo.Type.ISOTank;
            case "Reefer Container" -> ContainerInfo.Type.Reefer;
            case "Flat Rack Container" -> ContainerInfo.Type.FlatRack;
            default -> null;
        };
    }

    public static LogInfo.StaffType mapStaffType(int type){
        return switch (type) {
            case 1 -> LogInfo.StaffType.SustcManager;
            case 2 -> LogInfo.StaffType.CompanyManager;
            case 3 -> LogInfo.StaffType.SeaportOfficer;
            case 4 -> LogInfo.StaffType.Courier;
            default -> null;
        };
    }


}
