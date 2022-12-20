# CS307 Project II: "SUSTC" Project Report

### 12110919 游俊涛 12112627 李乐平

Lab Session 3, Teacher: 程然, 王维语

Dec, 2022

Project Source Code: https://github.com/sustechwifi/CS307proj

Contributions: 游俊涛 50% 李乐平 50%

---
Configuration:
```
manipulationImplClassName = "main.service.DatabaseManipulation";
```

## Task 1: Database Design

+ §E-R Model

Completed by 李乐平

<img src="C:\Users\25874\Desktop\md\imgs\图片1.png" style="zoom: 50%;" />

Software: 亿图图示

Figure 1.1 E-R Diagram Snapshot

+ §Database Structure

    + Completed by 李乐平

    + Modified by 游俊涛

      <img src="C:\Users\25874\Desktop\md\imgs\图片2.png" style="zoom: 50%;" />

Figure 1.2 Database Structure

**Explanation:**

+ §General Description

All tables are established abiding by the E-R diagram above, yet trivial modifications are applied.

In the database, every table in the database use an "**id**" attribute that can automatically increases as its primary
key, and connect with other tables by foreign keys formatted "***table name*\_id**". Hence that query format can be
unified.

The database should be established from tables that don't rely on any other table. Then continue establishment in
accordance to the reliance relationship.

For this concrete database, entities "**city**", and "**company**" do not have any reliance, so that they should first
be established. Then entities "**ship**" and "**staff**" are next. Then, "**container**". And the final entity "**
record**" is the last entity to be imported. Furthermore, we used a m-n-p relationship "**undertake**" to link
tables "**city**", "**staff**" and "**record**".

**P.S.**: It should be easy to read and understand the meaning of each attribute, so there is no need to explain further
in most cases.

+ §City

No reliance. Distinguished by its not null "**name**".

+ §Company

No reliance. Distinguished by its not null "**name**".

+ §Ship

Subject to "**company**" (1:n) and "**city**" (1:n). Distinguished by its not null "**name**", and has its "**state**"
revealing whether it is shipping(1) or not(0).

+ §Container

Subject to "**ship**" (1:n) and "**city**" (1:n). Distinguished by its not null "**code**". Has common attribute "**
type**", as well as "**state**" revealing whether it is shipping(2), waiting for shipping(1) or not(0).

+ §Staff

Subject to "**company**" (1:n, optional) and "**city**" (1:n, optional, this attribute is established for convenience).
Distinguished by its not null "**name**". Has common attributes "**birth_year**", "**gender**", "**password**" and "**
phone_number**".

For **gender**, integer 1 means MALE and 0 means FEMALE.

Different type of staffs are distinguished by its attribute "**type**". 1 for *SUSTC Department Manager*, 2 for *Company
Manager*, 3 for *Seaport Officer* and 4 for *Courier*.

+ §Record

Subject to "**company**" (1:n), "**container**" (1:n), "**city**" (m:n),"**courier**" (m:n) and "**ship**" (1:n),
Distinguished by its not null "**item_name**". Have common attributes "**item_class**", "**item_price**" and "**state**"
.

The item is not specially separated into a table because it is claimed not necessary in the project requirement.

+ §Undertake

Embodiment of the m:n:p relationship of "**city**", "**staff**" and "**record**". Distinguished by its "**city_id**"
, "**staff_id**", "**record_id**" and "**type**".

There are 4 **type**s in total: 1 for RETRIEVAL, 2 for EXPORT, 3 for EXPORT CHECKING, 4 for IMPORT CHECKING, 5 for
IMPORT and 6 for DELIVERY. For types 3 and 4, it has extra attributes "**tax**", and their corresponding staffs are
seaport officers (while others correspond to couriers).

+ §Ability of Expansion

The database can easily expand when requirement is changed. For example, if there are more cities that item went
through, we can simply add a new record to table **undertake**. In the case of extra shipment process is added, we can
define **type** = 7, 8, \... to describe the new process. In this example, there is no need to change the structure of
the database when requirement is increased, so that the database has good ability of expansion.

Actually, we do have expanded the tables mentioned above considering there are only 4 types in project I.

+ §P.S.

In the designing period, we considered relationships between ship and city(Dock), as well as container and city(Stay)
since the description of API-18(unloadItem) mentioned that this API **IMPLIES** that 「*the corresponding container is
being unloaded to "Import City" of the item and the ship is currently docking at "Import City"*」. Yet the time
attributes revealing chronological event sequences are missing in the .csv file. Hence we can only let this relationship
lies idle.

### §Permission Granting

Designation and SQL: 李乐平

Server Checking: 游俊涛

+ §Role Creating and Authorizing

Run the **role.sql** to create four types of role, which are endowed with corresponding operating authority.

```sql
create role sustcmanager;
create role seaportofficer;
create role courier;
create role seaportofficer;

ALTER
DEFAULT PRIVILEGES IN SCHEMA public
GRANT ALL PRIVILEGES ON SEQUENCES TO sustcmanager;
ALTER
DEFAULT PRIVILEGES IN SCHEMA public
GRANT ALL PRIVILEGES ON SEQUENCES TO seaportofficer;
ALTER
DEFAULT PRIVILEGES IN SCHEMA public
GRANT ALL PRIVILEGES ON SEQUENCES TO courier;
ALTER
DEFAULT PRIVILEGES IN SCHEMA public
GRANT ALL PRIVILEGES ON SEQUENCES TO seaportofficer;

grant select on sustc2.public.undertake,city,company,container,staff,ship,record to sustcmanager,courier,seaportofficer,companymanager;
grant all on undertake, record to courier;
grant update on record to seaportofficer, courier;
grant update on record,container,ship to companymanager;

# set role postgres;
# show role ;
```

+ §Permission Controlling Process:

--\Front-end Layer and Controller Layer (Front-end and Back-end Checking)--\

The user logs in via the random `token` distributed by UUID. Then the front will limit the information the user can
access, while the back-end will save the log in information and call the constructor only once. Meanwhile, the back-end
will switch to the corresponding role.

When jumping to other pages, the front-end will send a request together with the token. Then back-end will check the
validation of the token and decide whether accept the request or not.

When sending a request, the client information carried will be wrapped into a `LogInfo` class.

--\Service Layer and DAO Layer (API Checking)--\

When calling the API, it will call the `identifyCheck` method to check the validation of the LogInfo instance.

If all the checking processes are passed, then the API will execute corresponding SQL and return the result.

```java
private final Predicate<LogInfo> identifyCheck=(id)->id.type()==LogInfo.StaffType.CompanyManager;
private final Runnable role=()->SqlFactory.setRole(LogInfo.StaffType.CompanyManager);
```

```java
@Override
public double getImportTaxRate(LogInfo log,String city,String itemClass){
    if(identifyCheck.test(log)){
        role.run();
        //...
    }
}
```

```java
//Defined in SqlFactory.java
public static void setRole(LogInfo.StaffType type){
        Connection con = JdbcUtil.connection;
        if (con == null || type == null){
            return;
        }
        String sql = "set role %s";
        try {
            PreparedStatement ps = null;
            if (type == LogInfo.StaffType.SustcManager) {
                ps = con.prepareStatement(String.format(sql,"sustcmanager"));
            }else if(type == LogInfo.StaffType.CompanyManager){
                ps = con.prepareStatement(String.format(sql,"companymanager"));
            }else if(type == LogInfo.StaffType.SeaportOfficer){
                ps = con.prepareStatement(String.format(sql,"seaportofficer"));
            }else if(type == LogInfo.StaffType.Courier){
                ps = con.prepareStatement(String.format(sql,"courier"));
            }
            //ps = con.prepareStatement(String.format(sql, "postgres"));
            ps.executeUpdate();
            System.out.println("curr change:"+getCurrDatabaseUser());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
```

## Task 2 & Advanced Tasks: API Implementation and Client-Server System

API 1-2: Completed by李乐平

API 3-21: Completed by游俊涛

Fore-end and back-end environment: Completed by 游俊涛


+ §Local judge result:

  ![](C:\Users\25874\Desktop\md\imgs\图片12.png)

---
As for detailed basic, supplemental and advanced SQLs, please refer to the appendix at the end of this report.

+ §Identity Check

Use login `token` to check and store staff id.

Both fore-end and back-end servers will check whether the request is valid.

```java
@PostMapping("/login")
    public Result<?> login(@RequestBody LogInfo staff) {
        String sql = "select type from staff where name = ? and password = ?";
        try {
            LogInfo me = SqlFactory.handleSingleResult(
                    SqlFactory.handleQuery(sql, staff.name(), staff.password()),
                    r -> {
                        Integer type = r.getInt(1);
                        String name = staff.name();
                        String password = staff.password();
                        if (type == null) return null;
                         else if (type == 1) 
                            return new LogInfo(name,LogInfo.StaffType.SustcManager,password);
                         else if (type == 2) 
                            return new LogInfo(name,LogInfo.StaffType.CompanyManager,password);
                         else if (type == 3) 
                            return new LogInfo(name,LogInfo.StaffType.SeaportOfficer,password);
                         else if (type == 4) 
                            return new LogInfo(name,LogInfo.StaffType.Courier,password);
                         else return null;
                    }
            );
            String token;
            if (me != null) {
                token = UUID.randomUUID().toString();
                users.put(token, me); // HashMap<String,LogInfo> user;
                return Result.ok(me, token);
            } else return Result.error("wrong name or password");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
```

Use personal login token to invoke method. **If the user has not login/ has invalid token, then reject the request**.

```java
@GetMapping("/me")
    public Result<?> tokenCheck(@RequestParam("token") String token) {
        System.out.println(token);
        if (!users.containsKey(token)) {
            return Result.error("no login!");
        } else {
            LogInfo me = users.get(token);
            return Result.ok(me);
        }
    }
```

### §Self defined SQL Framework

+ Use simple `encapsulated templates` to handle all kinds of query or update SQLs.

    + Basic technical dependency : Java Reflection, Annotation, Proxy & Functional Programming

+ Advantages
    + Users only need to configure sql string, parameters and functions of handling query results.

    + No need to create or load PrepareStatement or deal with resultSet manually.

---
+ §DatabaseManipulationProxy.java (Dynamic proxy and resolve annotations that auto-execute sqls and handle exceptions
  behind)

Codes like:

```java
// Proxy Object
private final MethodInterFaces mapper = (MethodInterFaces) Proxy.newProxyInstance(
            CompanyManagerService.class.getClassLoader(),
            new Class[]{MethodInterFaces.class},
            new DatabaseManipulationProxy()
        );
```

```java
public class DatabaseManipulationProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Proxy handled");
        var returnType = method.getReturnType();
        if (method.isAnnotationPresent(Update.class)){
            System.out.println("Execute update...");
            Update update = method.getAnnotation(Update.class);
            String sql = update.sql();
            return SqlFactory.handleUpdate(sql,args);
        }else if(method.isAnnotationPresent(Aggregated.class)){
            System.out.println("Execute aggregated-result query...");
            Aggregated aggregate = method.getAnnotation(Aggregated.class);
            String sql = aggregate.sql();
            return SqlFactory.singleSelect(SqlFactory.handleQuery(sql,args),returnType);
        } else {
            System.out.println("No annotation found...");
        }
        try {
            return method.invoke(proxy, args);
        } catch (Exception e) {
            System.out.println("Exception found!");
            System.out.println(e.getMessage());
            return null;
        }
    }
}
```

+ §SqlFactory.java (Generic methods with functional programming that handle all kinds of query and execute automatically)

Construction like:

![](C:\Users\25874\Desktop\md\imgs\图片3.png)

+ §MethodInterFaces.java (Common methods defined in interface that using self-defined annotations to configure sql and
  args)

Codes like:

```java
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
    
    //Other methods...
}
```

\*Simple SQLs can be defined easily in MethodInterFaces.java using annotation.

\*Complex SQLs can be configured more details in SqlFactory.java.

See also in the Appendix attached to the end of this report.

+ Works like:

```java
// Using for configuring connection.
@SqlSupport(URL = "jdbc:postgresql://127.0.0.1:5432/sustc2", USERNAME = "postgres")
public class Main {
    public static void main(String[] args) {
        JdbcUtil.getConnection(Main.class);
        //TODO following operations...
    }
}
```

```java
// Using for complex sql.
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
```

```java
// Using proxy object {mapper} to execute simple sql.
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
```

## Part of API Running Results in Client-Server System

+ §Root User APIs:

![](C:\Users\25874\Desktop\md\imgs\图片4.png)

+ §Courier APIs: Add new item

![](C:\Users\25874\Desktop\md\imgs\图片5.png)

![](C:\Users\25874\Desktop\md\imgs\图片6.png)

+ §SUSTC manager APIs: Infos and counts

![](C:\Users\25874\Desktop\md\imgs\图片7.png)

+ §Seaport officer APIs: Items



+ ![](C:\Users\25874\Desktop\md\imgs\图片8.png)§Company manager APIs: Tax rate

![](C:\Users\25874\Desktop\md\imgs\图片9.png)


+ §Login

![](C:\Users\25874\Desktop\md\imgs\图片10.png)

+ §Back-end Structure: External library is not used.

The implementation of IDatabaseManipulation interface and related APIs are encapsulated into `CS307proj.jar` file.



Back-end server only parse and deal with the request that sent from the front-end. Three classes are included: `Spring
boot Application`, `REST API controller` and `JSON object wrapper`.


+ §Spring Boot Application:

```java
@SpringBootApplication
@SqlSupport(URL = "jdbc:postgresql://127.0.0.1:5432/sustc2?useUnicode=true&characterEncoding=utf8&rewriteBatchedStatement=true")
public class Cs307frontApplication {
    public static void main(String[] args) {
        JdbcUtil.getConnection(Cs307frontApplication.class);
        SpringApplication.run(Cs307frontApplication.class, args);
    }
}
```

+ §REST controller: Parse the HTTP request and invoke corresponding API.

```java
@RestController
@RequestMapping("/db")
public class APIController {
    DatabaseManipulation databaseManipulation = new DatabaseManipulation();
    
    //...
    @GetMapping("/api8")
    public Result<?> api8(@RequestParam("logInfo") String log, @RequestParam("name") String name) {
        return Result.ok(databaseManipulation.getShipInfo(JSON.parseObject(log, LogInfo.class), name));
    }

    @GetMapping("/api9")
    public Result<?> api9(@RequestParam("logInfo") String log, @RequestParam("code") String code) {
        return Result.ok(databaseManipulation.getContainerInfo(JSON.parseObject(log, LogInfo.class), code));
    }

    @GetMapping("/api10")
    public Result<?> api10(@RequestParam("logInfo") String log, @RequestParam("name") String name) {
        return Result.ok(databaseManipulation.getStaffInfo(JSON.parseObject(log, LogInfo.class), name));
    }
    //...
}
```

+ §Fore-end Callback Function: Send HTTP requests.

```vue
  Api6() {
      request.get("/db/api6", {
        params: {
          logInfo: JSON.stringify(this.sustcManager)
        }
      }).then(res => {
        if (res.code === 0) {
          this.$message({
            type: 'success',
            message: res.data
          })
          this.shipCount = res.data
        } else {
          this.$message({
            type: 'error',
            message: res.data
          })
        }
      })
    },
    Api7() {
      request.get("/db/api7", {
        params: {
          logInfo: JSON.stringify(this.sustcManager),
          name: this.api7.name
        }
      }).then(res => {
        if (res.code === 0) {
          this.$message({
            type: 'success',
            message: res.data
          })
        } else {
          this.$message({
            type: 'error',
            message: res.data
          })
        }
      })
    },
```

## Appendix

+ §SQL Used

\--SQL for API Required (Main Body)\--

\*Some duplicated codes are ignored.

+ §Constructor:

Please refer to resource/Insert with constraint.sql


+ §insert

```sql
insert into city(name) values (?);

insert into company(name) values (?);

insert into staff(name,gender,phone,birth_year,type,password,company_id,city_id) values (?,?,?,?,?,?,?,?);

insert into ship(name,company_id,state) values (?,?,?);

insert into container(code,type,state) values (?,?,?);

insert into record(item_name,item_class,item_price,state,company_id,container_id) values (?,?,?,?,?,?);

update ship set state = 1 where name = ?;

update container set state = 2 where code = ?;

update container set state = 1 where code = ?;

insert into undertake(type,record_id,city_id,staff_id) values(?,?,?,?);

insert into undertake(type,record_id,city_id,staff_id,tax) values(?,?,?,?,?);

```
+ §getCompanyCount/ getCityCount/ getShipCount

```sql
select count(id) from %s where trim(name) != ?;
```

+ §getCourierCount

```sql
select count(id) from staff where type = 4;
```

+ §getItemInfo

```sql
select * from record where item_name = ?;
```

+ §getShipInfo
```sql
select s.name,c.name ,s.state from ship s join company c on s.company_id = c.id where s.name = ?;
```

+ §getContainerInfo
```sql
select c.type,c.code,c.state from container c where c.code = ?;
```

+ §getStaffInfo
```sql
select s.type , s.name ,
(select com.name from company com where s.company_id = com.id),
(select ci.name from city ci where ci.id = s.city_id)
, s.gender, (2022 - s.birth_year), s.phone, s.password
from staff s
where s.name = ?;
```

+ §newItem
```sql
insert into record(item_name, item_class, item_price, state, company_id) values (?,?,?,?,?);

insert into undertake (record_id,staff_id,city_id,type) values(?,?,?,?);
```

+ §setItemState

```sql
select state from record where item_name = ?;

update undertake set
staff_id = (select id from staff where name = ?),
city_id = (select city_id from staff where name = ?)
where (type = 5 or type = 6) and record_id = (select id from record where item_name = ?);

update record set state = ? where item_name = ?;
```

+ §getImportTaxRate/ getExportTaxRate
```sql
select u.tax , r.item_price from undertake u
join record r on u.record_id = r.id
where u.type = ? and r.item_class = ? and u.city_id =
(select c.id from city c where c.name = ?);
```
+ §loadItemToContainer

```sql
select id from container where state = 0 and code = ?;

update record set state = 4,
container_id = ?
where item_name = ?;

update container set state = 1 where code = ?;
```
+ §loadContainerToShip

```sql
select state from ship where name = ?;

update container set ship_id =
(select s.id from ship s where s.name = ? and s.state = 0)
where code = ? and state = 1;

update record set state = 5 where container_id = (select id from container where code = ?) and state = 4;
```

+ §shipStartSailing
```sql
update ship set state = 1 where name = ?;

update record set state = 6
where state = 5 and container_id = (
select c.id from container c where c.ship_id = (
select s.id from ship s where s.name = ?
)
);
```
+ §unloadItem
```sql
select container_id from record where item_name = ?;

update container set state = 0 where id = ?;

update ship set state = 0 where id = (select c.ship_id from container
c where c.id = ?);

update record set state = ? where item_name = ?;
```

+ §itemWaitForChecking
```sql
select state from record where item_name = ?;

update record set state = ? where item_name = ?;
```

+ §getAllItemsAtPort
```sql
select item_name
from record
where id in
(select record_id from undertake
where (city_id = ? and (type = 3 or type = 4) and staff_id is null)
)
and (state = 3 or state = 8);
```

+ §setItemCheckState
```sql
update record set state = %d where id = ?;

update undertake set staff_id = ?,city_id = ? where record_id = ? and type = ?;
```

\--SQL for Supplemental and Advanced API\--

+ §getItemState
```sql
select state from record where item_name = ?;
```

+ §checkItemExist
```sql
select ((select count(id) from record where item_name = ?) > 0);
```

+ §getCityId
```sql
select city_id from staff where name = ?;
```

+ §getCompanyId
```sql
select s.company_id from staff s where s.name = ?
```

+ §getStaffId
```sql
select id from staff where name = ?
```

+ §getRecordId
```sql
select id from record where item_name = ?
```

+ §getContainerIdByRecord
```sql
select container_id from record where item_name = ?
```

+ §getStaffNameByItem
```sql
select (select name from staff where id = u.staff_id) from undertake u
where u.type = 6 and
u.record_id = (select id from record where item_name = ?);
```


+ §getCityName
```sql
select name from city where id = (select city_id from staff where name = ?);
```

+ §checkCourier
```sql
select
(
    (select city_id from undertake\
    where record_id = (select id from record where item_name = ?) and type = ?)
=
    (select city_id from staff where name = ?)
);
```

+ §getEmptyContainer
```sql
select id from container where state = 0 and code = ?;
```

+ §getShipState
```sql
select state from ship where name = ?;
```

+ §getContainerState
```sql
select state from container where code = ?;
```


+ §checkLoaded
```sql
select count(c.id) from container c where c.ship_id =\
(select s.id from ship s where s.name = ?);
```

+ §checkShipCompanyByStaffName
```sql
select (
    (select company_id from ship where name = ?)
=
    (select company_id from staff where name = ?)
);
```

+ §checkItemCompanyByStaffName
```sql
select (
    (select company_id from record where item_name = ?)
=
    (select company_id from staff where name = ?)
);
```

+ §shipFreed
```sql
select ((select count(c1.id) from container c1
where c1.state = 1 and c1.ship_id =
(select c2.ship_id from container c2 where c2.id = ?)) = 0);
```

+ §setShipFreeByContainerId
```sql
update ship set state = 0 where id =
(select c.ship_id from container c where c.id = ?);
```

+ §setContainerEmptyById
```sql
update container set state = 0 where id = ?;
```

+ §setItemStateSailing
```sql
update record set state = 6
where state = 5 and container_id =
(select c.id from container c where c.ship_id =
(select s.id from ship s where s.name = ?)
);
```

+ §setShipSailing
```sql
update ship set state = 1 where name = ?;
```

### Source Codes

Front-end and back-end source codes with `SpringMVC` and `Vue`:

<https://github.com/sustechwifi/CS307_front_back_end>

IDatabaseManipulation implement classes with only JDBC:

[<span class="underline">https://github.com/sustechwifi/CS307proj</span>](https://github.com/sustechwifi/CS307proj)

---

## Others

+ Helpful indices are added to speed up the SQL execution.

```sql
create index container_ship_index
    on container (ship_id);

create index undertake_record_id_index
    on undertake (record_id);
```

+ §Runtime Environment

**Hardware**

CPU Model:

> 11^th^ Gen Intel(R) Core(TM) i7-11800H @ 2.30GHz

Memory:

> RAM 16.0 GB

Hard Disk Type:

> Hard Disk Drive

Hard Disk Speed:

> Random 16.0 Read: 892.30 MB/s
>
> Sequential 64.0 Read: 2878.45 MB/s
>
> Sequential 64.0 Write: 2832.45 MB/s

**Software**

Data Base Managing System Version:

> SQL: Postgresql 14.02

Operating System Version:

> Windows 11

Programming Language:

> Java 17
>
> JavaScript

Development Environment:

> Idea 2021.3 Ultimate Edition

Libraries Used:

> Postgresql-42.5.0.jar

\*No other dependency used for implement.

**Flows are only used when presentation.**

Front-end Frameworks:

> Vue 3.2.13
>
> Element-plus 2.2.16

Back-end Frameworks:

> Springboot MVC 2.7.3
