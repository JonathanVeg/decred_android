package altcoin.br.decred.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import altcoin.br.decred.data.DBTools;
import altcoin.br.decred.utils.Utils;

class Wallet {
    private String address;
    private String balance;

    public Wallet() {
        this.address = "";
        this.balance = "0";
    }

    private Wallet(String address) {
        this.address = address;
        this.balance = "0";
    }

    public boolean save(Context c) {
        DBTools db = new DBTools(c);

        try {

            String sql = "delete from wallets where address = 'ADDRESS'".replaceAll("ADDRESS", getAddress());

            Utils.log(sql + " ::: " + db.exec(sql));

            sql = "insert into wallets(address, balance) values('ADDRESS', 'BALANCE')".replaceAll("ADDRESS", getAddress()).replaceAll("BALANCE", getBalance());

            Utils.log(sql);

            return db.exec(sql);

        } catch (Exception e) {
            e.printStackTrace();

            return false;
        } finally {
            db.close();
        }
    }

    public boolean delete(Context c) {
        DBTools db = new DBTools(c);

        try {

            return db.exec("delete from wallets where address = 'ADDRESS'".replaceAll("ADDRESS", getAddress()));

        } catch (Exception e) {
            e.printStackTrace();

            return false;
        } finally {
            db.close();
        }
    }

    public static List<Wallet> loadAll(Context c) {
        DBTools db = new DBTools(c);

        List<Wallet> wallets = new ArrayList<>();

        try {

            for (int i = 0; i < db.search("select address from wallets"); i++)
                wallets.add(new Wallet(db.getData(i, 0)));

            return wallets;

        } catch (Exception e) {
            e.printStackTrace();

            return wallets;
        } finally {
            db.close();
        }
    }


    private String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}
