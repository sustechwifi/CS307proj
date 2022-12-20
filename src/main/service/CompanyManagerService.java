package main.service;

import main.interfaces.ICompanyManager;
import main.interfaces.LogInfo;
import main.utils.DatabaseManipulationProxy;
import main.utils.MethodInterFaces;
import main.utils.SqlFactory;
import main.utils.annotations.Multiple;



import java.lang.reflect.Proxy;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author yjt
 * impl for companyManager
 */
public class CompanyManagerService implements ICompanyManager {
    private final Predicate<LogInfo> identifyCheck = (id) -> id.type() == LogInfo.StaffType.CompanyManager;
    private final Runnable role = () -> SqlFactory.setRole(LogInfo.StaffType.CompanyManager);

    private final MethodInterFaces mapper = (MethodInterFaces) Proxy.newProxyInstance(
            CompanyManagerService.class.getClassLoader(),
            new Class[]{MethodInterFaces.class},
            new DatabaseManipulationProxy()
    );

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
    public boolean loadItemToContainer(LogInfo log, String itemName, String containerCode) {
        if (identifyCheck.test(log)) {
            role.run();
            Integer id = mapper.getEmptyContainer(containerCode);
            if (id == null){
                return false;
            }
            Integer state = mapper.getItemState(itemName);
            if (state == null || state != 4){
                return false;
            }
            return mapper.updateRecordByContainer(id,itemName) && mapper.setContainerFilled(containerCode);
        } else {
            return false;
        }
    }

    @Override
    public boolean loadContainerToShip(LogInfo log, String shipName, String containerCode) {
        if (identifyCheck.test(log)) {
            role.run();
            Integer shipState = mapper.getShipState(shipName);
            Integer containerState = mapper.getContainerState(containerCode);
            if(shipState == null || shipState != 0){
                return false;
            }
            if (containerState == null || containerState != 1){
                return false;
            }
            return mapper.setContainerShip(shipName, containerCode) &&
                    mapper.updateRecordContainer(containerCode);
        } else {
            return false;
        }
    }

    @Override
    public boolean shipStartSailing(LogInfo log, String shipName) {
        if (identifyCheck.test(log)) {
            role.run();
            Integer shipState = mapper.getShipState(shipName);
            if (shipState == null || shipState == 1){
                return false;
            }
            if (!mapper.checkLoaded(shipName)){
                return false;
            }
            if (!mapper.checkShipCompanyByStaffName(shipName, log.name())){
                return false;
            }
            return mapper.setShipSailing(shipName) &&
                    mapper.setItemStateSailing(shipName);
        } else {
            return false;
        }
    }

    @Override
    public boolean unloadItem(LogInfo log, String itemName) {
        if (identifyCheck.test(log)) {
            role.run();
            Integer itemState = mapper.getItemState(itemName);
            if (itemState == null || itemState != 6) {
                return false;
            }
            Integer containerId = mapper.getContainerIdByRecord(itemName);
            if (containerId == null){
                return false;
            }
            if (mapper.shipFreed(containerId)){
                mapper.setShipFreeByContainerId(containerId);
            }
            return mapper.updateRecord(7,itemName) &&
                    mapper.setContainerEmptyById(containerId);
        } else {
            return false;
        }
    }

    @Override
    public boolean itemWaitForChecking(LogInfo log, String item) {
        if (identifyCheck.test(log)) {
            role.run();
            Integer itemState = mapper.getItemState(item);
            if (itemState == null || itemState != 7) {
                return false;
            }
            return mapper.updateRecord(8,item);
        } else {
            return false;
        }
    }
}
