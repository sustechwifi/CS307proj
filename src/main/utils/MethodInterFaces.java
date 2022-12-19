package main.utils;

import main.utils.annotations.Aggregated;
import main.utils.annotations.Update;

public interface MethodInterFaces {

    @Aggregated(sql = "select count(id) from staff where type = 4")
    Integer getCourierCount();

    @Aggregated(sql = "select state from record where item_name = ?")
    Integer getItemState(String item);

    @Aggregated(sql = "select ((select count(id) from record where item_name = ?) > 0)")
    boolean checkItemExist(String item);

    @Aggregated(sql = "select city_id from staff where name = ? ")
    Integer getCityId(String name);

    @Aggregated(sql = "select s.company_id from staff s where s.name = ?")
    Integer getCompanyId(String name);

    @Aggregated(sql = "select name from city where id = (select city_id from staff where name = ?) ")
    String getCity(String name);

    @Aggregated(sql = """
             select
             (
                 (select city_id from undertake
                 where record_id = (select id from record where item_name = ?) and type = ?)
                 =
                 (select city_id from staff where name = ?)
             );
            """)
    boolean checkCourier(String itemName,int type,String name);

    @Aggregated(sql = "select id from container where state = 0 and code = ?")
    Integer getEmptyContainer(String code);

    @Aggregated(sql = "select state from ship where name = ?")
    Integer getShipState(String name);

    @Aggregated(sql = "select state from container where code = ?")
    Integer getContainerState(String code);

    @Aggregated(sql = """
            select count(c.id) from container c where c.ship_id =
            (select s.id from ship s where s.name = ?)
            """)
    boolean checkLoaded(String name);

    @Aggregated(sql = "select id from staff where name = ?")
    Integer getStaffId(String name);

    @Aggregated(sql = "select id from record where item_name = ?")
    Integer getRecordId(String name);

    @Aggregated(sql = "select container_id from record where item_name = ?")
    Integer getContainerIdByRecord(String name);

    @Aggregated(sql = """
            select (select name from staff where id = u.staff_id) from undertake u where u.type = 6 and
                        u.record_id = (select id from record where item_name = ?)
            """)
    String getStaffNameByItem(String name);

    @Update(sql = "insert into record(item_name, item_class, item_price, state, company_id) values (?,?,?,?,?)")
    boolean addRecord(String item_name,String item_class,double item_price,int state,int company_id);

    @Update (sql = "insert into undertake (record_id,staff_id,city_id,type) values(?,?,?,?) ")
    boolean addUndertake(Integer record_id,Integer staff_id,Integer city_id,Integer type);

    @Update(sql = """
            update undertake set
            staff_id = (select id from staff where name = ?),
            city_id = (select city_id from staff where name = ?)
            where (type = 5 or type = 6) and record_id = (select id from record where item_name = ?)
            """)
    boolean updateUndertake(String staffName1,String staffName2,String itemName);

    @Update(sql = """
            update record set state = ? where item_name = ?
            """)
    boolean updateRecord(Integer state,String item_name);

    @Update(sql = """
             update record set state = 4,
                    container_id = ?
                    where item_name = ?
            """)
    boolean updateRecordByContainer(Integer id, String itemName);

    @Update(sql = """
            update container set state = 1 where code = ?
            """)
    boolean setContainerFilled(String containerCode);

    @Update(sql = """
            update container set ship_id =
                    (select s.id from ship s where s.name = ? and s.state = 0)
                    where code = ? and state = 1
            """)
    boolean setContainerShip(String shipName, String containerCode);

    @Update(sql = """
            update record set state = 5 where container_id = (select id from container where code = ?)
                                and state = 4
            """)
    boolean updateRecordContainer(String containerCode);

}
