/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.receptor.form;

import java.applet.Applet;
import java.awt.Component;
import org.receptor.cam.ReceptorStream;
import org.receptor.cam.VideoPlayer;

public class AppletReceptor extends Applet 
{
    private static String session;  
    private static VideoPlayer videoPlayer;
    private static ReceptorStream receptorStream = null;
    private String server;
    private String port;
  
    public AppletReceptor(){
        setServer("150.163.46.138");
        setPort("1235");
        setVisible(true);
    }
    
    public AppletReceptor(String server, String port){
        this.server = server;
        this.port = port;
        setVisible(true);
    }
    
    public static String getSession() {
        return session;
    }

    public static void setSession(String session) {
        AppletReceptor.session = session;
    }

    public static VideoPlayer getVideoFrame() {
        return videoPlayer;
    }

    public static void setVideoFrame(VideoPlayer videoPlayer) {
        AppletReceptor.videoPlayer = videoPlayer;
    }

    public static ReceptorStream getReceptorStream() {
        return receptorStream;
    }

    public static void setReceptorStream(ReceptorStream receptorStream) {
        AppletReceptor.receptorStream = receptorStream;
    }
    
    public void init() {
        setName("Receptor");
        receptorStream = new ReceptorStream(videoPlayer, this);
        if (receptorStream.initialize(this)) {
            receptorStream.getSincroniaThread().setConnected(true);
            receptorStream.getSincroniaThread().start();
            setSize(400, 250);
        } 
        else  System.out.println("Não Conectou!");               
     }
   
    public String getNameServer() {
            return server;
    }

    public String getNamePort() {
            return port;
    }
     
    public void setServer(String server){
        this.server = server;
    }
    
    public void setPort(String port){
        this.port = port;
    }

    public Component getCam(){
        return videoPlayer.getCam();
    }
}
