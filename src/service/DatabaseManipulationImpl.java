package service;

import Interfaces.IDatabaseManipulation;
import POJO.*;

public class DatabaseManipulationImpl implements IDatabaseManipulation {
    private final CompanyManagerService companyManagerService;
    private final CourierService courierService;
    private final SeaportOfficerService seaportOfficerService;
    private final SustcManagerService sustcManagerService;

    public DatabaseManipulationImpl(){
        companyManagerService = new CompanyManagerService();
        courierService = new CourierService();
        seaportOfficerService = new SeaportOfficerService();
        sustcManagerService = new SustcManagerService();
    }

    @Override
    public double getImportTaxRate(LogInfo log, String city, String itemClass) {
        return companyManagerService.getImportTaxRate(log, city, itemClass);
    }

    @Override
    public double getExportTaxRate(LogInfo log, String city, String itemClass) {
        return companyManagerService.getExportTaxRate(log, city, itemClass);
    }

    @Override
    public boolean loadItemToContainer(LogInfo log, String itemName, String containerCode) {
        return companyManagerService.loadItemToContainer(log, itemName, containerCode);
    }

    @Override
    public boolean loadContainerToShip(LogInfo log, String shipName, String containerCode) {
        return companyManagerService.loadContainerToShip(log, shipName, containerCode);
    }

    @Override
    public boolean shipStartSailing(LogInfo log, String shipName) {
        return companyManagerService.shipStartSailing(log, shipName);
    }

    @Override
    public boolean unloadItem(LogInfo log, String itemName) {
        return companyManagerService.unloadItem(log, itemName);
    }

    @Override
    public boolean itemWaitForChecking(LogInfo log, String item) {
        return companyManagerService.itemWaitForChecking(log, item);
    }

    @Override
    public boolean newItem(LogInfo log, ItemInfo item) {
        return courierService.newItem(log, item);
    }

    @Override
    public boolean setItemState(LogInfo log, String name, ItemState s) {
        return courierService.setItemState(log, name, s);
    }

    @Override
    public void $import(String recordsCSV, String staffsCSV) {

    }

    @Override
    public String[] getAllItemsAtPort(LogInfo log) {
        return seaportOfficerService.getAllItemsAtPort(log);
    }

    @Override
    public boolean setItemCheckState(LogInfo log, String itemName, boolean success) {
        return seaportOfficerService.setItemCheckState(log, itemName, success);
    }

    @Override
    public int getCompanyCount(LogInfo log) {
        return sustcManagerService.getCompanyCount(log);
    }

    @Override
    public int getCityCount(LogInfo log) {
        return sustcManagerService.getCityCount(log);
    }

    @Override
    public int getCourierCount(LogInfo log) {
        return sustcManagerService.getCourierCount(log);
    }

    @Override
    public int getShipCount(LogInfo log) {
        return sustcManagerService.getShipCount(log);
    }

    @Override
    public ItemInfo getItemInfo(LogInfo log, String name) {
        return sustcManagerService.getItemInfo(log, name);
    }

    @Override
    public ShipInfo getShipInfo(LogInfo log, String name) {
        return sustcManagerService.getShipInfo(log, name);
    }

    @Override
    public ContainerInfo getContainerInfo(LogInfo log, String code) {
        return sustcManagerService.getContainerInfo(log, code);
    }

    @Override
    public StaffInfo getStaffInfo(LogInfo log, String name) {
        return sustcManagerService.getStaffInfo(log, name);
    }
}
