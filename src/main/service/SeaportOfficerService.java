package main.service;

import main.interfaces.ISeaportOfficer;
import main.interfaces.LogInfo;
import main.utils.DatabaseManipulationProxy;
import main.utils.MethodInterFaces;
import main.utils.SqlFactory;
import main.utils.annotations.Multiple;
import main.utils.annotations.Update;

import java.lang.reflect.Proxy;
import java.util.function.Predicate;

/**
 * seaport officer impl
 *
 * @author yjt
 */
public class SeaportOfficerService implements ISeaportOfficer {

    private final Predicate<LogInfo> identifyCheck = (id) -> id.type() == LogInfo.StaffType.SeaportOfficer;

    private final Runnable role = () -> SqlFactory.setRole(LogInfo.StaffType.SeaportOfficer);

    private final MethodInterFaces mapper = (MethodInterFaces) Proxy.newProxyInstance(
            SeaportOfficerService.class.getClassLoader(),
            new Class[]{MethodInterFaces.class},
            new DatabaseManipulationProxy()
    );
    
    @Override
    @Multiple(sql = """
            select item_name
            from record
            where id in (select record_id from undertake
                   where (city_id = ? and (type = 3 or type = 4) and staff_id is null))
              and (state = 3 or state = 8)
            """)
    public String[] getAllItemsAtPort(LogInfo log) {
        if (identifyCheck.test(log)) {
            role.run();
            int cityId = mapper.getCityId(log.name());
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
            if (!mapper.checkItemExist(itemName)) {
                return false;
            }
            Integer recordId = mapper.getRecordId(itemName);
            Integer staffId = mapper.getStaffId(log.name());
            Integer cityId = mapper.getCityId(log.name());
            if (recordId == null || staffId == null || cityId == null){
                return false;
            }
            String sql2 = """
                    update undertake set staff_id = ?,city_id = ? where record_id = ? and type = ?
                    """;
            Integer state = mapper.getItemState(itemName);
            if (state == null) {
                return false;
            }else if(state == 3){
                mapper.updateRecord(success ? 4 : 12,itemName);
                SqlFactory.handleUpdate(sql2,staffId,cityId,recordId,3);
            }else if(state == 8){
                mapper.updateRecord(success ? 9 : 13,itemName);
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
