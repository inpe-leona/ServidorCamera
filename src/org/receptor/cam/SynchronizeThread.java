package org.receptor.cam;
/**
 *
 * @author Leona
 */

public class SynchronizeThread extends Thread {

    private boolean dataReceived;
    private boolean connected;
    private ReceptorStream receptorStream;

    public SynchronizeThread(boolean connected, boolean dataReceived,
                    Object dataSync, ReceptorStream receptorStream) {
        this.connected = connected;
        this.dataReceived = dataReceived;
        this.receptorStream = receptorStream;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void run() {
        try {
            System.out.println("antes do while");
            while (connected) {
                if (!dataReceived) {
                    System.err.println("Esperando dados chegarem via RTP...");
                }
            }
            System.err.println("Nenhum dado via RTP foi recebido.");
            receptorStream.close();
        } catch (Exception e) {
        }
    }
}
