package fr.chantapp.db;

import android.content.Context;

import org.greenrobot.greendao.database.Database;

public class DbSession {
    public static DaoSession getDaoSession(Context context){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "chantDb.db");
        Database db = helper.getWritableDb();
        return new DaoMaster(db).newSession();
    }

}
