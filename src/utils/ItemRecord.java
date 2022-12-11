package utils;
public class ItemRecord {
    public String itemName;
    public String itemClass;
    public String itemPrice;
    public String retrievalCity;
    public String retrievalCourier;
    public String deliveryCity;
    public String deliveryCourier;
    public String exportCity;
    public String importCity;
    public String exportTax;
    public String importTax;
    public String exportOfficer;
    public String importOfficer;
    public String containerCode;
    public String containerType;
    public String shipName;
    public String companyName;
    public String itemState;

    public ItemRecord(String record){
        String[] col = record.split(",");
        itemName = col[0];
        itemClass = col[1];
        itemPrice = col[2];
        retrievalCity = col[3];
        retrievalCourier = col[4];
        deliveryCity = col[5];
        deliveryCourier = col[6];
        exportCity = col[7];
        importCity = col[8];
        exportTax = col[9];
        importTax = col[10];
        exportOfficer = col[11];
        importOfficer = col[12];
        containerCode = col[13];
        containerType = col[14];
        shipName = col[15];
        companyName = col[16];
        itemState = col[17];
    }
}
