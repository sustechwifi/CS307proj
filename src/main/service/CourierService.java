package main.service;

import main.interfaces.ICourier;
import main.interfaces.ItemInfo;
import main.interfaces.ItemState;
import main.interfaces.LogInfo;
import main.utils.DatabaseManipulationProxy;
import main.utils.MethodInterFaces;
import main.utils.SqlFactory;

import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author yjt
 * impl for courier
 */
public class CourierService implements ICourier {

    private final Predicate<LogInfo> identifyCheck = (id) -> id.type() == LogInfo.StaffType.Courier;
    private final Runnable role = () -> SqlFactory.setRole(LogInfo.StaffType.Courier);
    private final MethodInterFaces mapper = (MethodInterFaces) Proxy.newProxyInstance(
            CourierService.class.getClassLoader(),
            new Class[]{MethodInterFaces.class},
            new DatabaseManipulationProxy()
    );

    private boolean checkItem(ItemInfo item, LogInfo log) {
        return
                item.retrieval().courier() == null &&
                        item.name() != null &&
                        item.$class() != null &&
                        item.price() > 0 &&
                        !item.export().city().equals(item.$import().city()) &&
                        item.$import().officer() == null &&
                        item.export().officer() == null &&
                        item.delivery().courier() == null &&
                        item.state() == null &&
                        item.export().tax() != 0 &&
                        item.$import().tax() != 0 &&
                        Objects.equals(item.retrieval().city(), mapper.getCity(log.name())) &&
                        !mapper.checkItemExist(item.name());
    }

    @Override
    public boolean newItem(LogInfo log, ItemInfo item) {
        if (identifyCheck.test(log)) {
            role.run();
            if (!checkItem(item, log)) {
                return false;
            }
            return mapper.addRecord(item.name(), item.$class(), item.price(), 1, mapper.getCompanyId(log.name()))
                    && mapper.addUndertake(mapper.getRecordId(item.name()), mapper.getStaffId(log.name()), mapper.getCityId(log.name()), 1);
        } else {
            return false;
        }
    }

    @Override
    public boolean setItemState(LogInfo log, String name, ItemState s) {
        if (identifyCheck.test(log)) {
            role.run();
            if (!mapper.checkItemExist(name)) {
                return false;
            }
            if (s == SqlFactory.mapState(mapper.getItemState(name))) {
                return false;
            }
            Integer itemState = mapper.getItemState(name);
            Integer nextState = SqlFactory.mapStateToInt(s);
            if (itemState == null) {
                return false;
            }
            if (nextState == null) {
                return false;
            }
            if (nextState >= 4 && nextState <= 8) {
                return false;
            }
            if (itemState >= 4 && itemState <= 8) {
                return false;
            }
            if (mapper.checkCourier(name, 6, log.name()) && itemState >= 9) {
                if (nextState == 9) {
                    if (itemState != 9) {
                        return false;
                    }
                } else if (nextState == 10) {
                    if (itemState != 9) {
                        return false;
                    }
                } else if (nextState == 11) {
                    if (itemState != 10) {
                        return false;
                    }
                }
                String courierName = mapper.getStaffNameByItem(name);
                if (courierName == null) {
                    mapper.updateUndertake(log.name(), log.name(), name);
                } else if (!courierName.equals(log.name())) {
                    return false;
                }
            } else if (mapper.checkCourier(name, 1, log.name()) && itemState <= 2) {
                if (nextState > 3 || nextState - itemState != 1) {
                    return false;
                }
            } else {
                return false;
            }
            return mapper.updateRecord(nextState, name);
        } else {
            return false;
        }
    }
}
