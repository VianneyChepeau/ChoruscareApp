package fr.chantapp.db;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import fr.chantapp.db.TypeParole;
import fr.chantapp.db.Chant;
import fr.chantapp.db.Parole;

import fr.chantapp.db.TypeParoleDao;
import fr.chantapp.db.ChantDao;
import fr.chantapp.db.ParoleDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig typeParoleDaoConfig;
    private final DaoConfig chantDaoConfig;
    private final DaoConfig paroleDaoConfig;

    private final TypeParoleDao typeParoleDao;
    private final ChantDao chantDao;
    private final ParoleDao paroleDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        typeParoleDaoConfig = daoConfigMap.get(TypeParoleDao.class).clone();
        typeParoleDaoConfig.initIdentityScope(type);

        chantDaoConfig = daoConfigMap.get(ChantDao.class).clone();
        chantDaoConfig.initIdentityScope(type);

        paroleDaoConfig = daoConfigMap.get(ParoleDao.class).clone();
        paroleDaoConfig.initIdentityScope(type);

        typeParoleDao = new TypeParoleDao(typeParoleDaoConfig, this);
        chantDao = new ChantDao(chantDaoConfig, this);
        paroleDao = new ParoleDao(paroleDaoConfig, this);

        registerDao(TypeParole.class, typeParoleDao);
        registerDao(Chant.class, chantDao);
        registerDao(Parole.class, paroleDao);
    }
    
    public void clear() {
        typeParoleDaoConfig.clearIdentityScope();
        chantDaoConfig.clearIdentityScope();
        paroleDaoConfig.clearIdentityScope();
    }

    public TypeParoleDao getTypeParoleDao() {
        return typeParoleDao;
    }

    public ChantDao getChantDao() {
        return chantDao;
    }

    public ParoleDao getParoleDao() {
        return paroleDao;
    }

}
