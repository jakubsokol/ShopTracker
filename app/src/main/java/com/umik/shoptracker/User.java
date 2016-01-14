package com.umik.shoptracker;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    /** zmienna reprezentujaca identyfiakctor telefonu z ktorego zostanie przesłany obiekt uzytkownika */
    private int id_telefonu;
    /** zmienna reprezentująca o której uzytknik wszedl/wyszedl z dzialu*/
    private Date znacznik_czasowy;
    /** zmienna  reprezentujca 5 zdjec uzytkownika, któe zostaną przesłane na serwer */
    private File[] zdjecia;
    /** zmienna reprezentujaca flage wyjscia (true) lub wejscia(false) */
    private boolean flag;

    public User(int id_telefonu,Date znacznik_czasowy){
        this.id_telefonu=id_telefonu;
        this.znacznik_czasowy=znacznik_czasowy;
        zdjecia = new File[5];
    }

    public User(int id_telefonu,Date znacznik_czasowy, boolean flag){
        this.id_telefonu = id_telefonu;
        this.znacznik_czasowy = znacznik_czasowy;
        this.flag = flag;
    }

    public void copy(File[] f){
        zdjecia = f;
    }

    public int getId_telefonu() {
        return id_telefonu;
    }

    public void setId_telefonu(int id_telefonu) {
        this.id_telefonu = id_telefonu;
    }

    public Date getZnacznik_czasowy() {
        return znacznik_czasowy;
    }

    public void setZnacznik_czasowy(Date znacznik_czasowy) {
        this.znacznik_czasowy = znacznik_czasowy;
    }

    public File[] getZdjecia() {
        return zdjecia;
    }

    public void setZdjecia(File[] zdjecia) {
        this.zdjecia = zdjecia;
    }

    public String stworzFolder() {

        String sciezka = "D:\\foto";
        File dir = new File(sciezka+"\\"+getId_telefonu()+"_"+getZnacznik_czasowy().getHours()+"-"+getZnacznik_czasowy().getMinutes()+"-"+getZnacznik_czasowy().getSeconds());


        if(dir.mkdirs()){

            return dir.getAbsolutePath()+"\\";
        }
        else{
            return null;
        }
    }

    public String userToString() {
        return "ID: " + Integer.toString(getId_telefonu()) + "\n";
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public static void main(String[] args) {

    }
}