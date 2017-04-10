package com.example.ypham.all2clouds;

import java.util.Date;

/**
 * Created by Silver Wolf on 09/11/2014.
 */
public class CEvent {
    String CAL_ID = null;
    String Event_ID = null;
    String Event_TITLE = null;
    String Event_DESC = null;
    String Event_TimeZone = null;
    Date Event_START = null;
    Date Event_END = null;
    String Event_LOC = null;
    String displayName = null;
    String accountName = null;
    String ownerName = null;

    public CEvent(){

    }

    public CEvent(String _CAL_ID, String _Event_ID, String _Event_TITLE, String _Event_DESC, String _Event_TimeZone, Date _Event_START, Date _Event_END,
                  String _Event_LOC, String _displayName, String _accountName, String _ownerName){
        CAL_ID = _CAL_ID;
        Event_ID = _Event_ID;
        Event_TITLE = _Event_TITLE;
        Event_DESC = _Event_DESC;
        Event_TimeZone = _Event_TimeZone;
        Event_START = _Event_START;
        Event_END = _Event_END;
        Event_LOC = _Event_LOC;
        displayName = _displayName;
        accountName = _accountName;
        ownerName = _ownerName;
    }
}
