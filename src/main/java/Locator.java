import org.jgroups.*;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Locator extends Frame implements WindowListener, ActionListener,
        MembershipListener, Serializable {
    private static final String channel_name = "Quotes";
    private RpcDispatcher disp;
    transient private JChannel channel;

    private final Button get = new Button("Get");
    private final Button set = new Button("Set");
    private final Button quit = new Button("Quit");
    private final Button get_all = new Button("All");
    private final Label record = new Label("Record");
    private final Label value = new Label("Value");
    private final Label err_msg = new Label("Error");
    private final TextField record_field = new TextField();
    private final TextField value_field = new TextField();
    private final java.awt.List listbox = new java.awt.List();
    private final Font default_font = new Font("Helvetica", Font.PLAIN, 12);

    private static final String props = null;


    public Locator() {
        super();
        try {
            channel = new JChannel(props);
            channel.setDiscardOwnMessages(true);
            disp = (RpcDispatcher) new RpcDispatcher(channel, this).setMembershipListener(this);
            channel.connect(channel_name);
        } catch (Exception e) {
            System.err.println("Locator(): " + e);
        }
        addWindowListener(this);
    }

    private void showMsg(String msg) {
        err_msg.setText(msg);
        err_msg.setVisible(true);
    }

    private void clearMsg() {
        err_msg.setVisible(false);
    }


    public void start() {
        setLayout(null);
        setSize(400, 300);
        setFont(default_font);

        record.setBounds(new Rectangle(10, 40, 60, 30));
        value.setBounds(new Rectangle(10, 70, 60, 30));
        record_field.setBounds(new Rectangle(100, 40, 100, 30));
        value_field.setBounds(new Rectangle(100, 70, 100, 30));
        listbox.setBounds(210, 45, 150, 160);
        err_msg.setBounds(new Rectangle(10, 200, 350, 30));
        err_msg.setFont(new Font("Helvetica", Font.ITALIC, 12));
        err_msg.setForeground(Color.red);
        err_msg.setVisible(false);
        get.setBounds(new Rectangle(10, 250, 80, 30));
        set.setBounds(new Rectangle(100, 250, 80, 30));
        quit.setBounds(new Rectangle(190, 250, 80, 30));
        get_all.setBounds(new Rectangle(280, 250, 80, 30));

        get.addActionListener(this);
        set.addActionListener(this);
        quit.addActionListener(this);
        get_all.addActionListener(this);

        add(record);
        add(value);
        add(record_field);
        add(value_field);
        add(err_msg);
        add(get);
        add(set);
        add(quit);
        add(get_all);
        add(listbox);
        record_field.requestFocus();
        setVisible(true);
    }


    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }


    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        try {
            switch (command) {
                case "Get": {
                    getData();
                    break;
                }
                case "Set":
                    setData();
                    break;
                case "All":
                    getAllData();
                    break;
                case "Quit":
                    setVisible(false);
                    channel.close();
                    System.exit(0);
                default:
                    System.out.println("Unknown action");
                    break;
            }
        } catch (Exception ex) {
            value_field.setText("");
            ex.printStackTrace();
            showMsg(ex.toString());
        }
    }

    private void getData() throws Exception {
        String record_name = record_field.getText();
        if (record_name == null || record_name.isEmpty()) {
            showMsg("Record name is empty !");
            return;
        }
        showMsg("Looking up value for " + record_name + ':');
        RspList<Object> quotes = disp.callRemoteMethods(null, "getQuote", new Object[]{record_name},
                new Class[]{String.class},
                new RequestOptions(ResponseMode.GET_ALL, 10000));

        String val = null;
        for (Rsp<Object> rsp : quotes.values()) {
            Object quote = rsp.getValue();
            if (quote == null || quote instanceof Throwable)
                continue;
            val = (String) quote;
            break;
        }

        if (val != null) {
            value_field.setText(val);
            clearMsg();
        } else {
            value_field.setText("");
            showMsg("Value for " + record_name + " not found");
        }
    }

    private void setData() throws Exception {
        Address destination = null;
        Object data;
        String record_name = record_field.getText();
        String record_val = value_field.getText();
        if (record_name == null || record_val == null || record_name.isEmpty() ||
                record_val.isEmpty()) {
            showMsg("Record name and value have to be present to enter a new value");
            return;
        }
        RspList<Object> keys = disp.callRemoteMethods(null, "searchKey", new Object[]{record_name},
                new Class[]{String.class},
                new RequestOptions(ResponseMode.GET_ALL, 10000));
        for (Rsp rsp : keys.values()) {
            Object adr = rsp.getValue();
            if (adr == null || adr instanceof Throwable)
                continue;
            destination = (Address) adr;
            break;
        }
        data = new Object[]{record_name, record_val};
        if (destination == null)
            destination = searchServerToSave(data);
        System.out.println(destination.toString());
        disp.callRemoteMethod(destination, "setQuote", new Object[]{record_name, record_val},
                new Class[]{String.class, String.class},
                new RequestOptions(ResponseMode.GET_FIRST, 0));

        showMsg("Record " + record_name + " set to " + record_val);
    }

    private void getAllData() throws Exception {
        listbox.removeAll();
        showMsg("Getting all records:");
        RspList<Object> rsp_list = disp.callRemoteMethods(null, "getAllRecords",
                null, null,
                new RequestOptions(ResponseMode.GET_ALL, 5000));

        System.out.println("rsp_list is " + rsp_list);

        Map<String, String> all_records = new HashMap<>();
        for (Rsp rsp : rsp_list.values()) {
            Object obj = (Map<String, String>) rsp.getValue();
            if (obj == null || obj instanceof Throwable)
                continue;
            all_records.putAll((Map<String, String>) obj);

        }

        if (all_records == null) {
            showMsg("No records found");
            return;
        }
        clearMsg();
        listbox.removeAll();
        all_records.entrySet().stream().filter(entry -> entry.getValue() != null)
                .forEach(entry -> listbox.add(entry.getKey() + ": " + entry.getValue()));
    }


    private ArrayList<Address> getAvailableServers() {
        ArrayList<Address> servers = new ArrayList<>();
        try {
            RspList<Address> rsp_list = disp.callRemoteMethods(null, "getAdress",
                    null, null,
                    new RequestOptions(ResponseMode.GET_ALL, 5000));
            for (Rsp rsp : rsp_list.values()) {
                Object adr = rsp.getValue();
                if (adr == null || adr instanceof Throwable)
                    continue;

                servers.add((Address) adr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return servers;
    }

    private Address searchServerToSave(Object ob) {
        ArrayList<Address> availableServer = getAvailableServers();
        if (availableServer.size() == 0) {
            return createExtraServer();
        }
        return availableServer.get(ob.hashCode() % (getAvailableServers().size()));
    }

    private Address createExtraServer() {
        Server sr = new Server();
        sr.start();
        return sr.getAdress();
    }

    @SuppressWarnings("UnusedParameters")
    public static void setQuote(String record_name, String value) {
    }

    public void printAllRecords() {
    }

    public void viewAccepted(View new_view) {
        setTitle("Members in " + channel_name + ": " + (new_view.size() - 1));
    }

    public void suspect(Address suspected_mbr) {
    }

    public void block() {
    }

    public void unblock() {
    }

    public static void main(String args[]) throws IOException {
        Locator client = new Locator();
        for (int i = 0; i < 4; i++) {
            new Server().start();
        }
        client.start();

    }

}