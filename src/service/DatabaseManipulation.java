package service;

import main.interfaces.*;
import utils.ItemRecord;
import utils.StaffRecord;

import java.sql.*;
import java.util.*;

public class DatabaseManipulation implements IDatabaseManipulation {
    public DatabaseManipulation() {

    }

    private final CompanyManagerService companyManagerService = new CompanyManagerService();
    private final CourierService courierService = new CourierService();
    private final SeaportOfficerService seaportOfficerService = new SeaportOfficerService();
    private final SustcManagerService sustcManagerService = new SustcManagerService();

    private static final int MALE = 1;
    private static final int FEMALE = 0;

    private static final int DOCKING = 0;
    private static final int SHIPPING = 1;

    private static final int CONTAINERUNPACKED = 0;
    private static final int CONTAINERPACKED = 1;
    private static final int CONTAINERSHIPPING = 2;

    private static final int RETRIEVAL = 1;
    private static final int EXPORT = 2;
    private static final int EXPORTCHECKING = 3;
    private static final int IMPORTCHECKING = 4;
    private static final int IMPORT = 5;
    private static final int DELIVERY = 6;

    private static final int SUSTCDEPARTMENTMANAGER = 1;
    private static final int COMPANYMANAGER = 2;
    private static final int SEAPORTOFFICER = 3;
    private static final int COURIER = 4;
    private static final HashMap<String, Integer> staffType = new HashMap<>() {{
        put("SUSTC Department Manager", SUSTCDEPARTMENTMANAGER);
        put("Seaport Officer", SEAPORTOFFICER);
        put("Company Manager", COMPANYMANAGER);
        put("Courier", COURIER);
    }};

    static final HashMap<String, Integer> itemState = new HashMap<>() {{
        put("Start", 0);
        put("Picking-up", 1);
        put("To-Export Transporting", 2);
        put("To-export Transporting", 2);
        put("Export Checking", 3);
        put("Packing to Container", 4);
        put("Waiting for Shipping", 5);
        put("Shipping", 6);
        put("Unpacking from Container", 7);
        put("Import Checking", 8);
        put("Importing Checking", 8);
        put("From-Import Transporting", 9);
        put("From-import Transporting", 9);
        put("Delivering", 10);
        put("Finish", 11);
        put("Export Check Fail", 12);
        put("Import Check Fail", 13);
    }};

    static final int BATCH_SIZE = 9961;

    HashMap<String, FileRecordWrapper> cityMap = new HashMap<>();
    HashMap<String, FileRecordWrapper> companyMap = new HashMap<>();
    HashMap<String, FileRecordWrapper> staffMap = new HashMap<>();
    HashMap<String, FileRecordWrapper> shipMap = new HashMap<>();
    HashMap<String, FileRecordWrapper> containerMap = new HashMap<>();
    HashMap<String, FileRecordWrapper> recordMap = new HashMap<>();

    Connection connection;

    public DatabaseManipulation(String database, String root, String pass) {
        try {
//            System.out.printf("%s %s %s",database,root,pass);
            Class.forName("org.postgresql.Driver");
            String parameter = "?useUnicode=true&characterEncoding=utf8&rewriteBatchedStatement=true";
            connection = DriverManager.getConnection("jdbc:postgresql://" + database + parameter, root, pass);
            connection.setAutoCommit(false);

            String sql1 = """
                    create table if not exists company(
                        name varchar(30) unique not null,
                        id serial primary key
                    );""";

            String sql2 = """
                    create table if not exists city(
                        name varchar(20) unique not null,
                        id serial primary key
                    );
                    """;

            String sql3 = """
                    create table if not exists ship(
                        name varchar(20) unique not null,
                        state int,
                        company_id int not null,
                        city_id int,
                        id serial primary key,
                        foreign key (company_id) references company(id),
                        foreign key (city_id) references city(id)
                    );
                    """;

            String sql4 = """
                    create table if not exists container(
                        code varchar(15) unique not null,
                        type varchar(30) not null,
                        state int,
                        ship_id int,
                        city_id int,
                        id serial primary key,
                        foreign key (city_id) references city(id),
                        foreign key (ship_id) references ship(id)
                    );
                    """;

            String sql5 = """
                    create table if not exists staff(
                        phone bigint,
                        name varchar(20) unique not null,
                        birth_year int,
                        gender int8,
                        type int not null,
                        password varchar(20) not null,
                        id serial primary key,
                        company_id int,
                        city_id int,
                        foreign key (company_id) references company(id),
                        foreign key (city_id) references city(id)
                    );
                    """;

            String sql6 = """
                    create table if not exists record(
                        item_name varchar(18) unique not null,
                        item_class varchar(18) not null,
                        item_price bigint not null,
                        state int,
                        company_id int not null,
                        container_id int,
                        id serial primary key,
                        foreign key (container_id) references container(id),
                        foreign key (company_id) references company(id)
                    );
                    """;

            String sql7 = """
                    create table if not exists undertake(
                        type int not null,
                        record_id int not null,
                        staff_id int,
                        city_id int,
                        tax numeric(24,6),
                        id serial primary key,
                        unique(record_id,staff_id,type),
                        foreign key (staff_id) references staff(id),
                        foreign key (record_id) references record(id),
                        foreign key (city_id) references city(id)
                    );
                    """;
            connection.prepareStatement(sql1).execute();
            connection.prepareStatement(sql2).execute();
            connection.prepareStatement(sql3).execute();
            connection.prepareStatement(sql4).execute();
            connection.prepareStatement(sql5).execute();
            connection.prepareStatement(sql6).execute();
            connection.prepareStatement(sql7).execute();

        } catch (Exception e) {
//            e.printStackTrace();
            try {
                connection.prepareStatement("drop table undertake;").execute();
            } catch (Exception ignored) {
            }
            try {
                connection.prepareStatement("drop table record;").execute();
            } catch (Exception ignored) {
            }
            try {
                connection.prepareStatement("drop table container;").execute();
            } catch (Exception ignored) {
            }
            try {
                connection.prepareStatement("drop table ship;").execute();
            } catch (Exception ignored) {
            }
            try {
                connection.prepareStatement("drop table staff;").execute();
            } catch (Exception ignored) {
            }
            try {
                connection.prepareStatement("drop table city;").execute();
            } catch (Exception ignored) {
            }
            try {
                connection.prepareStatement("drop table company;").execute();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void $import(String recordsCSV, String staffsCSV) {
        String[] records = recordsCSV.split("\n");
        recordsCSV = null;
        String[] staffs = staffsCSV.split("\n");
        staffsCSV = null;
        try {
//            BufferedReader reader = new BufferedReader(new FileReader(recordsCSV));

            for (int i = 1; i < staffs.length; i++) {
//                String tmpRecord = reader.readLine();
//                writeStaffRecord(new StaffRecord(tmpRecord));
                writeStaffRecord(new StaffRecord(staffs[i]));
            }
            for (int i = 1; i < records.length; i++) {
//                String tmpRecord = reader.readLine();
//                writeStaffRecord(new ItemRecord(tmpRecord));
                writeItemRecord(new ItemRecord(records[i]));
            }
            records = null;
            staffs = null;
            onFinish();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                connection.prepareStatement("drop table undertake;").execute();
            } catch (Exception ignored) {
            }
            try {
                connection.prepareStatement("drop table record;").execute();
            } catch (Exception ignored) {
            }
            try {
                connection.prepareStatement("drop table container;").execute();
            } catch (Exception ignored) {
            }
            try {
                connection.prepareStatement("drop table ship;").execute();
            } catch (Exception ignored) {
            }
            try {
                connection.prepareStatement("drop table staff;").execute();
            } catch (Exception ignored) {
            }
            try {
                connection.prepareStatement("drop table city;").execute();
            } catch (Exception ignored) {
            }
            try {
                connection.prepareStatement("drop table company;").execute();
            } catch (Exception ignored) {
            }
        }
    }

    public void writeStaffRecord(StaffRecord record) {
        putMap(cityMap, record.city, record);
        putMap(companyMap, record.company, record);
        putMap(staffMap, record.name, record);
    }

    public void writeItemRecord(ItemRecord record) {
        putMap(shipMap, record.shipName, record);
        putMap(containerMap, record.containerCode, record);
        putMap(recordMap, record.itemName, record);
    }

    private void putMap(HashMap map, String key, StaffRecord record) {
        if (key != null && key.length() != 0) {
            if (!map.containsKey(key))
                map.put(key, new FileRecordWrapper(record));
        }
    }

    private void putMap(HashMap map, String key, ItemRecord record) {
        if (key != null && key.length() != 0) {
            if (!map.containsKey(key))
                map.put(key, new FileRecordWrapper(record));
        }
    }

    public void onFinish() throws Exception {
        writeCompany();
        writeCity();
        writeStaff();
        writeShip();
        writeContainer();
        writeRecord();
        writeUndertake();
    }

    private void writeCity() throws Exception {
        String sql = "insert into city(name) values (?)";
        writeDB("City", sql, cityMap, (PreparedStatement st, String key, FileRecordWrapper record) -> {
            st.setString(1, key);
        });
    }

    private void writeCompany() throws Exception {
        String sql = "insert into company(name) values (?)";
        writeDB("Company", sql, companyMap, (PreparedStatement st, String key, FileRecordWrapper record) -> {
            st.setString(1, key);
        });
    }

    private void writeStaff() throws Exception {
        String sql = "insert into staff(name,gender,phone,birth_year,type,password,company_id,city_id) " +
                "values (?,?,?,?,?,?,?,?)";
        writeDB("Staff", sql, staffMap, (PreparedStatement st, String key, FileRecordWrapper record) -> {
            String name = key;
            String phone = record.stRecord.phone;
            String gender = record.stRecord.gender;
            String age = String.valueOf(2022 - Integer.parseInt(record.stRecord.age));
            Integer type = staffType.get(record.stRecord.type);
            st.setString(1, name);
            st.setInt(2, gender.equals("male") ? MALE : FEMALE);
            st.setLong(3, Long.parseLong(phone));
            st.setInt(4, Math.round(Float.parseFloat(age)));
            st.setInt(5, type);
            st.setString(6, record.stRecord.password);
            if (type == COURIER || type == COMPANYMANAGER) {
                st.setInt(7, companyMap.get(record.stRecord.company).id);
            } else {
                st.setNull(7, Types.INTEGER);
            }
            if (type == COURIER || type == SEAPORTOFFICER) {
                st.setInt(8, cityMap.get(record.stRecord.city).id);
            } else {
                st.setNull(8, Types.INTEGER);
            }
        });
    }

    private void writeShip() throws Exception {
        String sql = "insert into ship(name,company_id,state) values (?,?,?)";
        writeDB("Ship", sql, shipMap, (PreparedStatement st, String key, FileRecordWrapper record) -> {
            st.setString(1, key);
            st.setObject(2, companyMap.get(record.itRecord.companyName).id);
            st.setInt(3, record.itRecord.itemState.equals("Shipping") ? 1 : 0);
        });
        //Todo: Update the state of the ship
    }

    private void writeContainer() throws Exception {
        String sql = "insert into container(code,type,state) values (?,?,?)";
        writeDB("Container", sql, containerMap, (PreparedStatement st, String key, FileRecordWrapper record) -> {
            st.setString(1, key);
            st.setString(2, record.itRecord.containerType);
            st.setInt(3, record.itRecord.itemState.equals("Shipping")
                    ? 2 : record.itRecord.itemState.equals("Waiting for Shipping") ? 1 : 0);
        });

        //Todo: Update the state of the container
    }

    private void writeRecord() throws Exception {
        String sql = "insert into record(item_name,item_class,item_price,"
                + "state,company_id,container_id) values (?,?,?,?,?,?)";
        writeDB("Record", sql, recordMap, (PreparedStatement st, String key, FileRecordWrapper record) -> {
            st.setString(1, record.itRecord.itemName);
            st.setString(2, record.itRecord.itemClass);
            if (record.itRecord.itemPrice.length() > 0) {
                st.setInt(3, (int) Double.parseDouble(record.itRecord.itemPrice));
            } else {
                st.setNull(3, Types.BIGINT);
            }
            st.setInt(4, itemState.get(record.itRecord.itemState));
            st.setObject(5, companyMap.get(record.itRecord.companyName).id);
            if (itemState.get(record.itRecord.itemState) >= itemState.get("Waiting for Shipping")
                    && record.itRecord.containerCode != null && record.itRecord.containerCode.length() != 0) {
                st.setObject(6, containerMap.get(record.itRecord.containerCode).id);
            } else {
                st.setObject(6, null);
            }
            if (Objects.equals(itemState.get(record.itRecord.itemState), itemState.get("Shipping"))) {
                String tmpsql1 = String.format("update ship set state = 1 where name = '%s';", record.itRecord.shipName);
                String tmpsql2 = String.format("update container set state = 2 where code = '%s';", record.itRecord.containerCode);
                PreparedStatement st1 = connection.prepareStatement(tmpsql1);
                PreparedStatement st2 = connection.prepareStatement(tmpsql2);
                st1.execute();
                st2.execute();
            }
            if (Objects.equals(itemState.get(record.itRecord.itemState), itemState.get("Waiting for Shipping"))) {
                String tmpsql2 = String.format("update container set state = 1 where code = '%s';", record.itRecord.containerCode);
                PreparedStatement st2 = connection.prepareStatement(tmpsql2);
                st2.execute();
            }
            //Todo: Check the legality of IDs
        });

    }

    private void writeUndertake() throws Exception {
        String sql1 = "insert into undertake(type,record_id,city_id,staff_id) values(?,?,?,?)";
        String sql2 = "insert into undertake(type,record_id,city_id,staff_id,tax) values(?,?,?,?,?)";
        writeDB("Retrieval", sql1, recordMap, (PreparedStatement st, String key, FileRecordWrapper record) -> {
            st.setInt(1, RETRIEVAL);
            st.setObject(2, recordMap.get(record.itRecord.itemName).id);
            if (record.itRecord.retrievalCity != null) {
                st.setObject(3, cityMap.get(record.itRecord.retrievalCity).id);
                st.setObject(4, staffMap.get(record.itRecord.retrievalCourier).id);
            } else {
                st.setObject(3, null);
                st.setObject(4, null);
            }
        }, false);

        writeDB("Export", sql1, recordMap, (PreparedStatement st, String key, FileRecordWrapper record) -> {
            st.setInt(1, EXPORT);
            st.setObject(2, recordMap.get(record.itRecord.itemName).id);
            if (itemState.get(record.itRecord.itemState) >= itemState.get("Export Checking")) {
                st.setObject(3, cityMap.get(record.itRecord.exportCity).id);
                st.setObject(4, staffMap.get(record.itRecord.retrievalCourier).id);
            } else {
                st.setObject(3, null);
                st.setObject(4, null);
            }
        }, false);

        writeDB("Export Checking", sql2, recordMap, (PreparedStatement st, String key, FileRecordWrapper record) -> {
            st.setInt(1, EXPORTCHECKING);
            st.setObject(2, recordMap.get(record.itRecord.itemName).id);
            if (itemState.get(record.itRecord.itemState) >= itemState.get("Export Checking")) {
                st.setObject(3, cityMap.get(record.itRecord.exportCity).id);
                st.setDouble(5, Double.parseDouble(record.itRecord.exportTax));
            } else {
                st.setObject(3, null);
                st.setObject(5, null);
            }
            if (staffMap.get(record.itRecord.exportOfficer) != null) {
                st.setObject(4, staffMap.get(record.itRecord.exportOfficer).id);
            } else {
                st.setObject(4, null);
            }
        }, false);

        writeDB("Import Checking", sql2, recordMap, (PreparedStatement st, String key, FileRecordWrapper record) -> {
            st.setInt(1, IMPORTCHECKING);
            st.setObject(2, recordMap.get(record.itRecord.itemName).id);
            if (itemState.get(record.itRecord.itemState) >= itemState.get("Import Checking")) {
                st.setObject(3, cityMap.get(record.itRecord.importCity).id);
                st.setDouble(5, Double.parseDouble(record.itRecord.importTax));
            } else {
                st.setObject(3, null);
                st.setObject(5, null);
            }
            if (staffMap.get(record.itRecord.importOfficer) != null) {
                st.setObject(4, staffMap.get(record.itRecord.importOfficer).id);
            } else {
                st.setObject(4, null);
            }
        }, false);

        writeDB("Import", sql1, recordMap, (PreparedStatement st, String key, FileRecordWrapper record) -> {
            st.setInt(1, IMPORT);
            st.setObject(2, recordMap.get(record.itRecord.itemName).id);
            if (itemState.get(record.itRecord.itemState) >= itemState.get("Import Checking")) {
                st.setObject(3, cityMap.get(record.itRecord.importCity).id);
            } else {
                st.setObject(3, null);
            }
            if (record.itRecord.deliveryCourier != null && record.itRecord.deliveryCourier.length() != 0) {
                st.setObject(4, staffMap.get(record.itRecord.deliveryCourier).id);
            } else {
                st.setObject(4, null);
            }
        }, false);

        writeDB("Deliver", sql1, recordMap, (PreparedStatement st, String key, FileRecordWrapper record) -> {
            st.setInt(1, DELIVERY);
            st.setObject(2, recordMap.get(record.itRecord.itemName).id);
            st.setObject(3, cityMap.get(record.itRecord.deliveryCity).id);
            if (staffMap.get(record.itRecord.deliveryCourier) != null) {
                st.setObject(4, staffMap.get(record.itRecord.deliveryCourier).id);
            } else {
                st.setObject(4, null);
            }
        }, false);
    }

    private void writeDB(String table, String sql, HashMap<String, FileRecordWrapper> map,
                         ParameterSetCallback callback) throws Exception {
        writeDB(table, sql, map, callback, true);
    }

    private void writeDB(String table, String sql, HashMap<String, FileRecordWrapper> map,
                         ParameterSetCallback callback, boolean return_keys) throws Exception {
        PreparedStatement st;
        if (return_keys) {
            st = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } else {
            st = connection.prepareStatement(sql);
        }

        Iterator<Map.Entry<String, FileRecordWrapper>> iter = map.entrySet().iterator();
        ArrayList<FileRecordWrapper> arr = new ArrayList(BATCH_SIZE);
        int num = 0;
        int total = 0;

        while (iter.hasNext()) {
            Map.Entry<String, FileRecordWrapper> entry = (Map.Entry) iter.next();
            String key = entry.getKey();
            FileRecordWrapper rw = entry.getValue();
            callback.setParameter(st, key, rw);
            st.addBatch();
            arr.add(rw);

            total++;
            num++;
            if (num >= BATCH_SIZE) {
                executeSQL(st, arr, return_keys);
                arr.clear();
                num = 0;
                System.out.println(table + ": " + total);
            }
        }

        if (arr.size() > 0) {
            executeSQL(st, arr, return_keys);
            System.out.println(table + ": " + total);
        }
    }

    private void executeSQL(PreparedStatement st, ArrayList<FileRecordWrapper> arr, boolean return_keys) throws Exception {
        st.executeBatch();
        connection.commit();

        if (return_keys) {
            ResultSet rs = st.getGeneratedKeys();
            int i = 0;
            while (rs.next()) {
                FileRecordWrapper rw = arr.get(i++);
                rw.id = rs.getInt("id");
            }
        }

        st.clearBatch();
        st.clearParameters();
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

class FileRecordWrapper {
    StaffRecord stRecord = null;
    ItemRecord itRecord = null;
    Integer id = null;

    public FileRecordWrapper(StaffRecord r) {
        stRecord = r;
    }

    public FileRecordWrapper(ItemRecord r) {
        itRecord = r;
    }
}

interface ParameterSetCallback {
    void setParameter(PreparedStatement st, String key, FileRecordWrapper record) throws Exception;
}