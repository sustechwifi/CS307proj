
import main.interfaces.ItemInfo;
import main.interfaces.LogInfo;
import service.DatabaseManipulation;
import utils.JdbcUtil;
import utils.MethodFactory;
import utils.annotations.SqlSupport;

import java.util.*;


@SqlSupport(
        DRIVER = "org.postgresql.Driver",
        USERNAME = "postgres",
        PASSWORD = "20030118",
        URL = "jdbc:postgresql://127.0.0.1:5432/sustc2?useUnicode=true&characterEncoding=utf8&rewriteBatchedStatement=true"
)
public class Main {
    private static final DatabaseManipulation db = new DatabaseManipulation();

    private static void testSustcManager(){
        var sustcManager = new LogInfo("xxx",LogInfo.StaffType.SustcManager,"123");
        System.out.println(db.getCourierCount(sustcManager));
        System.out.println(db.getCompanyCount(sustcManager));
        System.out.println(db.getCityCount(sustcManager));
        System.out.println(db.getShipCount(sustcManager));
        System.out.println(db.getItemInfo(sustcManager,"cherry-3a393"));
        System.out.println(db.getStaffInfo(sustcManager,"Fang Xiaoxue"));
        System.out.println(db.getShipInfo(sustcManager,"eeab6019"));
        System.out.println(db.getContainerInfo(sustcManager,"d13b8d54"));
    }

    private static void testSeaportOfficer() {
        var seaportOfficer = new LogInfo("Dai Yushu", LogInfo.StaffType.SeaportOfficer,"123");
        System.out.println(Arrays.toString(db.getAllItemsAtPort(seaportOfficer)));
    }

    private static void testCourier() {
        var courier = new LogInfo("Fang Xiaoxue",LogInfo.StaffType.Courier,"123");
        System.out.println(db.newItem(courier,new ItemInfo(
                "cherry2022",
                "cherry",
                1919810,
                null,
                null,
                null,
                null,
                null
                )));
    }

    private static void testCompanyManager() {
        var companyManager = new LogInfo("xxx", LogInfo.StaffType.CompanyManager,"123");
        System.out.println(db.getExportTaxRate(companyManager,"Dalian","cherry"));
        System.out.println(db.getImportTaxRate(companyManager,"New York","cherry"));
    }

    public static void main(String[] args) {
        JdbcUtil.getConnection(Main.class);


        Scanner input = new Scanner(System.in);
        String s1 = input.nextLine();
        String[] ss1 = s1.split(", ");
        String s2 = input.nextLine();
        String[] ss2 = s2.split(", ");
        Set<String> flag = new HashSet<String>();
        Collections.addAll(flag, ss1);
        for (String s : ss2) {
            if (!flag.contains(s)) {
                System.out.println(s);
            }
        }

    }
}
