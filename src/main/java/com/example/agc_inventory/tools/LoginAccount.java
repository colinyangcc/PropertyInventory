package com.example.agc_inventory.tools;

import android.app.Application;


public class LoginAccount extends Application {
    private String Account;
    private String DBHostIP,DBName,DBAccount,DBPassword;

    public String getAccount(){
        return this.Account;
    }

    public void setAccount(String str){
        this.Account= str;
    }

    public String getDBHostIP(){
        return this.DBHostIP;
    }
    public String getDBName(){
        return this.DBName;
    }
    public String getDBAccount(){
        return this.DBAccount;
    }
    public String getDBPassword(){
        return this.DBPassword;
    }

    public void setDBHostIP(String str){
        this.DBHostIP= str;
    }
    public void setDBName(String str){
        this.DBName= str;
    }
    public void setDBAccount(String str){
        this.DBAccount= str;
    }
    public void setDBPassword(String str){
        this.DBPassword= str;
    }


}
