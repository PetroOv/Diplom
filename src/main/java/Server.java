import org.jgroups.*;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.jgroups.util.Util;
import pojos.ChannelPOJO;
import pojos.ContentDetailPOJO;
import pojos.ProgramPOJO;


import java.io.*;
import java.util.*;



public class Server extends ReceiverAdapter implements Serializable {
    Map<String, String> records;
    Map<String, Object> settings = new HashMap<>();
    private ContentDetailPOJO contentDetailPOJO;
    private ProgramPOJO programPOJO;
    private ChannelPOJO channelPOJO;
    boolean hasFreeSpace = true;
    transient JChannel channel;
    transient RpcDispatcher disp;
    transient protected Log log = LogFactory.getLog(getClass());

    Server(){
        settings.put("channel",new ChannelPOJO("Quotes",null));
        settings.put("program", new ProgramPOJO(10));
        settings.put("content", new ContentDetailPOJO(new HashMap<>()));
    }



    public void viewAccepted(View new_view) {
        System.out.println("Accepted view (" + new_view.size() + new_view.getMembers() + ')');
    }


    public void start() {
        try {
            records = ((ContentDetailPOJO)settings.get("content")).getRecords();
            channel = new JChannel(((ChannelPOJO)settings.get("channel")).getProperties());
            disp = (RpcDispatcher) new RpcDispatcher(channel, this)
                    .setMembershipListener(this);
            channel.connect(((ChannelPOJO)settings.get("channel")).getName());
            System.out.println("\nQuote Server started at " + new Date());
            System.out.println("Joined channel '" + channel.getName() + "' (" + channel.getView().size() + " members)");
            channel.getState(null, 0);
            System.out.println("Ready to serve requests");
        } catch (Exception e) {
            log.error("Server.start() : " + e);
            System.exit(-1);
        }
    }



    public String getQuote(String record_name) throws Exception {
        System.out.print("Getting quote for " + record_name + ": ");
        String retval = records.get(record_name);
        if (retval == null) {
            System.out.println("not found");
            throw new Exception("Record " + record_name + " not found");
        }
        System.out.println(retval);
        return retval;
    }

    public void setQuote(String record_name, String value) throws IOException {
        System.out.println("Setting quote for " + record_name + ": " + value);
        records.put(record_name, value);
        if (memoryStatus()>0)
            System.out.println("Server has " + memoryStatus() + " free cells");
        else{
            System.out.println("Server" + channel.address() + "is full");
            hasFreeSpace = false;
        }
    }
    public Address searchKey(String key){
        if(records.containsKey(key))
            return channel.address();
        return null;
    }

    public Map<String, String> getAllRecords() {
        System.out.print("getAllrecords: ");
        printAllrecords();
        return records;
    }
    public int memoryStatus(){
        return ((ProgramPOJO)settings.get("program")).getMemorySize() - records.size();
    }
    public void printAllrecords() {
        System.out.println(records);
    }

    public boolean isHasFreeSpace() {
        return hasFreeSpace;
    }
    public Address getAdress(){
        if(hasFreeSpace)
            return channel.address();
        return null;
    }

    public void getState(OutputStream ostream) throws Exception {
        Util.objectToStream(records, new DataOutputStream(ostream));
    }
    //    private void integrate(HashMap<String, String> state) {
//        if (state != null)
//            state.keySet().forEach(key -> records.put(key, state.get(key)));
//    }

//    public void setState(InputStream istream) throws Exception {
//        integrate((HashMap<String, String>) Util.objectFromStream(new DataInputStream(istream)));
//    }


//    public static void main(String args[]) {
//        try {
//            Server server = new Server();
//            server.start();
//            while (true) {
//                Util.sleep(10000);
//            }
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }
//    }

}