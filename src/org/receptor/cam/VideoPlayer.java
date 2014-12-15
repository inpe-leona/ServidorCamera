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

import java.awt.Component;
import javax.media.Player;
import javax.media.rtp.ReceiveStream;

public class VideoPlayer {
    private Player player;
    private ReceiveStream receiveStream;
    private ReceptorStream receptor;
    private Component cam;

    public ReceiveStream getReceiveStream() {
        return receiveStream;
    }

    public void setReceiveStream(ReceiveStream receiveStream) {
        this.receiveStream = receiveStream;
    }

    public ReceptorStream getReceptor() {
        return receptor;
    }

    public void setReceptor(ReceptorStream receptor) {
        this.receptor = receptor;
    }

    public Component getCam() {
        return cam;
    }

    public void setCam(Component cam) {
        this.cam = cam;
    }
	

    VideoPlayer(Player player, ReceiveStream receiveStream, ReceptorStream recept) {
        this.player = player;
        this.receiveStream = receiveStream;
        this.receptor = recept;
    }

    public void initialize() {
        if (player.getVisualComponent() != null) {
            System.out.println("Inicio Video Frame"); 
            cam = player.getVisualComponent();
            receptor.getFormReceptor().add("Center", cam);
        }
        System.out.println("Não Iniciou Video Frame");
    }

    public void close() {
            player.close();
    }

    public Player getPlayer() {
            return player;
    }

    public void setPlayer(Player player) {
            this.player = player;
    }
}
