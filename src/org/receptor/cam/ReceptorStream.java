/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.receptor.cam;

/**
 *
 * @author Leona
 */
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.control.BufferControl;
import javax.media.protocol.DataSource;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.NewParticipantEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.event.StreamMappedEvent;
import org.receptor.form.AppletReceptor;

public class ReceptorStream implements ReceiveStreamListener, 
                                       SessionListener, 
                                       ControllerListener {
    private RTPManager[] managerRTP = null;
    private Object dataSync = new Object();
    private VideoPlayer videoFrame = null;
    private AppletReceptor formReceptor;
    private SynchronizeThread sincroniaThread;
    private Socket clientSocket = null;
    private DataOutputStream outputStream = null;

    public SynchronizeThread getSincroniaThread() {
        return sincroniaThread;
    }

    public ReceptorStream(VideoPlayer videoFrame, AppletReceptor formRecept) 
    {
        this.videoFrame = videoFrame;
        this.formReceptor = formRecept;
    }

    public boolean initialize(AppletReceptor formReceptApplet) {
         try {
            clientSocket = new Socket(formReceptor.getNameServer(), Integer.parseInt(formReceptor.getNamePort())); //aqui estamos pegando da tela a porta para conexao com o servidor
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            outputStream.writeInt(1);

            InetAddress inetAddress;
            SessionAddress localAddress = new SessionAddress();
            SessionAddress destAddress;
            managerRTP = new RTPManager[1];
            Session sessionLabel;
            try {
                sessionLabel = new Session(formReceptor.getNameServer() + "/"
                                + Integer.parseInt(formReceptor.getNamePort()));
            //essa porta especifica em qual porta no servidor, o cliente ira obter os dados
            //deixamos no codigo arramado a porta 1235 para a conexao RTP, nada impede de mudar
            //essa porta, mas tem que fazer isso no servidor e no cliente
            } catch (IllegalArgumentException e) {
                System.err.println("Falha ao ler endereço do stream: "
                                + formReceptor.getNameServer() 
                        + ":" + Integer.parseInt(formReceptor.getNamePort()));
                return false;
            }
            System.err.println("Sessão de RTP aberta para:" + sessionLabel.address
                            + " porta: " + sessionLabel.port + " ttl: "
                            + sessionLabel.ttl);
            managerRTP[0] = (RTPManager) RTPManager.newInstance();
            managerRTP[0].addSessionListener(this);
            managerRTP[0].addReceiveStreamListener(this);
            inetAddress = InetAddress.getByName(sessionLabel.address);
            if (inetAddress.isMulticastAddress()) {
                    localAddress = new SessionAddress(inetAddress, sessionLabel.port,
                                    sessionLabel.ttl);
                    destAddress = new SessionAddress(inetAddress, sessionLabel.port,
                                    sessionLabel.ttl);
            } else {
                    localAddress = new SessionAddress(InetAddress.getLocalHost(),
                                    sessionLabel.port);
                    destAddress = new SessionAddress(inetAddress, sessionLabel.port);
            }
            managerRTP[0].initialize(localAddress);
            BufferControl bc = (BufferControl) managerRTP[0]
                            .getControl("javax.media.control.BufferControl");
            if (bc != null)
                    bc.setBufferLength(350);
            managerRTP[0].addTarget(destAddress);
        } catch (Exception e) {
            System.err.println("Não foi possivel criar a conexão RTP: "
                            + e.getMessage());
            return false;
        }
        sincroniaThread = new SynchronizeThread(true, true, dataSync, this);
        return true;
    }

    public boolean close() {
        boolean closed =  false;
        for (int i = 0; i < managerRTP.length; i++) {
            if (managerRTP[i] != null) {
                managerRTP[i].removeTargets("Fechando sessão com o servidor.");
                managerRTP[i].dispose();
                managerRTP[i] = null;
                closed = true;
            }
        }
        return closed;
    }

    public synchronized void update(SessionEvent evt) {
        System.out.println("1o update");
        if (evt instanceof NewParticipantEvent) {
            Participant p = ((NewParticipantEvent) evt).getParticipant();
            System.err.println("Um novo participante foi detectado.: "
                            + p.getCNAME());
        }
    }

    public synchronized void update(ReceiveStreamEvent evt) {
        System.out.println("2o update");
        Participant participant = evt.getParticipant();
        ReceiveStream stream = evt.getReceiveStream();
        System.err.println("Um novo participante foi detectado.: ");
        if (evt instanceof RemotePayloadChangeEvent) {
            System.err.println("  - Recebido um RTP PayloadChangeEvent.");
            System.err.println("Não foi possível carregar mudanças.");
            System.exit(0);
        } else if (evt instanceof NewReceiveStreamEvent) {
            try {
                stream = ((NewReceiveStreamEvent) evt).getReceiveStream();
                DataSource ds = stream.getDataSource();
                RTPControl ctl = (RTPControl) ds.getControl("javax.media.rtp.RTPControl");
                if (ctl != null) {
                    System.err.println("Recebido um novo RTP stream: "+ ctl.getFormat());
                } else
                    System.err.println("Recebido um novo RTP stream");
                if (participant == null)
                    System.err.println("O servido desse stream ainda n�o foi identificado.");
                else {
                    System.err.println("O stream vem de:"+ participant.getCNAME());
                }
                Player p = javax.media.Manager.createPlayer(ds);
                if (p == null)
                    return;
                p.addControllerListener(this);
                p.realize();
                videoFrame = new VideoPlayer(p, stream, this);
                synchronized (dataSync) {
                    dataSync.notifyAll();
                }
            } catch (Exception e) {
                    System.err.println("NewReceiveStreamEvent exception "
                                        + e.getMessage());
                    return;
            }
        } else if (evt instanceof StreamMappedEvent) {
            if (stream != null && stream.getDataSource() != null) {
                DataSource ds = stream.getDataSource();
                RTPControl ctl = (RTPControl) ds.getControl("javax.media.rtp.RTPControl");
                System.err.println("  - The previously unidentified stream ");
                if (ctl != null)
                        System.err.println("      " + ctl.getFormat());
                System.err.println("      had now been identified as sent by: "
                                + participant.getCNAME());
            }
        } else if (evt instanceof ByeEvent) {
            System.err.println("Conexao terminada por: "
                            + participant.getCNAME());
            videoFrame.close();
        }
    }

    public synchronized void controllerUpdate(ControllerEvent ce) {
        System.out.println("primeiro controllerUpdate");
        videoFrame.setPlayer((Player) ce.getSourceController());
        if (videoFrame.getPlayer() == null)
            return;
        if (ce instanceof RealizeCompleteEvent) {
            System.err.println("tentando inciar player");
            videoFrame.initialize();
            videoFrame.getPlayer().start();
        }
        if (ce instanceof ControllerErrorEvent) {
            videoFrame.getPlayer().removeControllerListener(this);
            videoFrame.close();
            System.err.println("Erro interno ao abrir player: " + ce);
        }
    }

    public void sendCommand(String string) {
        if (clientSocket != null && outputStream != null) {
            try {
                outputStream.writeBytes(string);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    AppletReceptor getFormReceptor() {
        return formReceptor;
    }
   
}
