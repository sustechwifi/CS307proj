package service;

import main.interfaces.ISeaportOfficer;
import main.interfaces.LogInfo;
import utils.MethodFactory;
import utils.SqlFactory;
import utils.annotations.Multiple;
import utils.annotations.Update;

import java.util.function.Predicate;

/**
 * seaport officer impl
 *
 * @author yjt
 */
public class SeaportOfficerService implements ISeaportOfficer {

    private final Predicate<LogInfo> identifyCheck = (id) -> id.type() == LogInfo.StaffType.SeaportOfficer;

    private final Runnable role = () -> SqlFactory.setRole(LogInfo.StaffType.SeaportOfficer);

    @Override
    @Multiple(sql = """
            select item_name
            from record
            where id in
                  (select record_id from undertake
                   where (city_id = ? and (type = 3 or type = 4))
                  )
              
            """)
    public String[] getAllItemsAtPort(LogInfo log) {
        if (identifyCheck.test(log)) {
            role.run();
            int cityId = MethodFactory.getCityId(log);
            System.out.println(cityId);
            try {
                return SqlFactory.query(
                        this.getClass().getMethod("getAllItemsAtPort", LogInfo.class),
                        r -> r.getString(1),
                        arr -> arr.toArray(String[]::new),
                        cityId
                );
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    @Update
    public boolean setItemCheckState(LogInfo log, String itemName, boolean success) {
        if (identifyCheck.test(log)) {
            role.run();
            if (!MethodFactory.checkItemExist(itemName)) {
                return false;
            }
            Integer recordId = MethodFactory.getRecordId(itemName);
            Integer staffId = MethodFactory.getStaffId(log.name());
            Integer cityId = MethodFactory.getCityId(log);
            if (recordId == null || staffId == null || cityId == null){
                return false;
            }
            String sql1 = """
                    update record set state = %d where id = ?
                    """;

            String sql2 = """
                    update undertake set staff_id = ?,city_id = ? where record_id = ? and type = ?
                    """;
            Integer state = MethodFactory.getItemState(itemName);
            if (state == null) {
                return false;
            }else if(state == 3){
                SqlFactory.handleUpdate(String.format(sql1, success ? 4 : 12), recordId);
                SqlFactory.handleUpdate(sql2,staffId,cityId,recordId,3);
            }else if(state == 8){
                SqlFactory.handleUpdate(String.format(sql1, success ? 9 : 13), recordId);
                SqlFactory.handleUpdate(sql2,staffId,recordId,4);
            }else {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
}
