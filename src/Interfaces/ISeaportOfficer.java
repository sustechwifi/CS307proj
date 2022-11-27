package Interfaces;

import POJO.LogInfo;

public interface ISeaportOfficer {
	String[] getAllItemsAtPort(LogInfo log);
	
	boolean setItemCheckState(LogInfo log, String itemName, boolean success);
}
