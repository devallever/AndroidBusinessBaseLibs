package com.hd.calculator.app.function.db.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.hd.calculator.app.function.db.entity.AccountEntity;

import java.util.List;

@Dao
public interface AccountDao {

    //addAccount
    @Insert
    void addAccount(AccountEntity accountEntity);

    @Insert
    void addAccounts(List<AccountEntity> users);

    //updateAccount
    @Update
    void updateAccount(AccountEntity accountEntity);

    //getByUserId
    @Query("SELECT * FROM account WHERE userId = :userId")
    AccountEntity getByUserId(long userId);

    //getByPassword
    @Query("SELECT * FROM account WHERE password = :password")
    @Nullable AccountEntity getByPassword(String password);

    //getAll
    @Query("SELECT * FROM account")
    List<AccountEntity> getAccountList();

    //delete all
    @Query("DELETE FROM account")
    void deleteAll();

    //delete not boss
    @Query("DELETE FROM account WHERE boss != 1")
    void deleteWaiterAccount();

    //getBossAccount
    @Query("SELECT * FROM account WHERE boss = 1")
    AccountEntity getBossAccount();

    //update
    @Update
    void update(AccountEntity accountEntity);

    @Query("SELECT * FROM account WHERE userId = :userId")
    AccountEntity getAccountByUserId(long userId);

    @Query("DELETE FROM account WHERE userId = :userId")
    void deleteAccount(long userId);
}
