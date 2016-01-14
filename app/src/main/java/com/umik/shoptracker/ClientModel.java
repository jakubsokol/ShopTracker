package com.umik.shoptracker;


import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Author Rafał Ciupa
 */

public class ClientModel implements Runnable {

    /** zmienna reprezentująca połączenie pomiędzy kientem a serwerem */
    private Socket gniazdko;
    /** zmienna reprezentująca adres IP serwera */
    private InetAddress adres_ip;
    /** zmienna reprezentująca port na którym nasłuchuje serwer */
    private int port;
    /** zmienna reprezentująca strumien wejsciowy gniazdka */
    private InputStream sturmien_wejsciowy;
    /** zmienna reprezentująca strumien wyjściowy gniazdka */
    private OutputStream strumien_wyjsciowy;
    /** zmienna reprezentująca strumien obiektowy wejsciowy, obiekty przesyłane tym strumieniem musza byc serializowane */
    private ObjectInputStream obiektowy_wejsciowy;
    /** zmienna reprezentująca strumien obiektowy wyjsciowy, obiekty przysylane tym strumieniem musza byc serializowane */
    private ObjectOutputStream obiektowy_wyjsciowy;

    private File plik_do_wyslania = null;

    private byte[] tablica_bajtow = null;

    private FileInputStream strumien_odczytu_do_pliku = null;

    private BufferedInputStream bajtowy_strumien_in = null;

    private DataOutputStream data_out = null;

    private String nazwa_pliku = null;

    public ClientModel(){

        try {
            adres_ip = InetAddress.getByName("192.168.0.11");
            port = 7005;
        }
        catch(UnknownHostException e){
            e.printStackTrace();
        }
    }


    public void utworzPolaczenie(){

        try {
            Log.i("WY", "wejscie do utworz polaczenie");
            gniazdko = new Socket(adres_ip,port);
            Log.i("WY", "po socket");
            strumien_wyjsciowy = gniazdko.getOutputStream();
            data_out = new DataOutputStream(strumien_wyjsciowy);
            obiektowy_wyjsciowy = new ObjectOutputStream(strumien_wyjsciowy);
            Log.i("WY", "zakonczenie");
        } catch (IOException e) {
            Log.i("GNIAZDKO", "nie udało się utworzyć gnaizdka");
            e.printStackTrace();
        }
    }

    public void utworzPlik(String sciezka_dostepu) throws IOException {

        plik_do_wyslania = new File(sciezka_dostepu);
        tablica_bajtow = new byte[(int) plik_do_wyslania.length()];
        strumien_odczytu_do_pliku = new FileInputStream(plik_do_wyslania);
        bajtowy_strumien_in = new BufferedInputStream(strumien_odczytu_do_pliku);
        bajtowy_strumien_in.read(tablica_bajtow, 0, tablica_bajtow.length);
        System.out.println("plik o nazwie " + plik_do_wyslania.getName() + " został poprawnie wczytany");
    }

    public void wyslijPlik(String sciezka_do_pliku) {

        try {
            utworzPlik(sciezka_do_pliku);

            int liczba_bajtow =0;	// formuowanie wielkosci segmentu dla TCP
            int rozmiar =0;	// ilosc obecnie przeslanych bajtow do serwera,
            // poczatkowo inicjowwane na zmienna
            // liczba_bajtow
            if(tablica_bajtow.length<10000){
                liczba_bajtow = tablica_bajtow.length;
                rozmiar = tablica_bajtow.length;
            }
            else{
                liczba_bajtow = 10000;
                rozmiar = 10000;
            }

            int liczba_bajtow_przeslanych = 0; // ilosc jednorazowego transferu
            // danych do serwera

            System.out.println("po danowych");
            data_out.writeLong(tablica_bajtow.length);
            System.out.println(tablica_bajtow.length);

            nazwa_pliku = plik_do_wyslania.getName();

            data_out.writeUTF(plik_do_wyslania.getName());
            data_out.write(tablica_bajtow, 0, liczba_bajtow);
            System.out.println("po przeslaniu danych");

            while (rozmiar < tablica_bajtow.length) {
                liczba_bajtow_przeslanych = Math.min(liczba_bajtow, tablica_bajtow.length - rozmiar);
                data_out.write(tablica_bajtow, rozmiar, liczba_bajtow_przeslanych);
                data_out.flush();
                rozmiar += liczba_bajtow_przeslanych;
            }
            data_out.flush();

        } catch (IOException e) {
            System.err.println("NASTĄPIŁ BŁAD PODCZAS METODY wyslijPlik");
            e.printStackTrace();
        }
    }

    public void wyslijWiadomosc(User u) throws IOException {
        obiektowy_wyjsciowy.writeObject(u);
    }


    public Socket getGniazdko() {
        return gniazdko;
    }

    public void setGniazdko(Socket gniazdko) {
        this.gniazdko = gniazdko;
    }

    @Override
    public void run() {
        utworzPolaczenie();
    }
}