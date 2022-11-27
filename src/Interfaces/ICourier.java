package Interfaces;

import POJO.ItemInfo;
import POJO.ItemState;
import POJO.LogInfo;

public interface ICourier {
	boolean newItem(LogInfo log, ItemInfo item);

	boolean setItemState(LogInfo log, String name, ItemState s);
}
