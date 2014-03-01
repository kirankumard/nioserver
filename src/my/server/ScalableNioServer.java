/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package my.server;

import java.io.IOException;

/**
 *
 * @author kdevaras
 */
public class ScalableNioServer {

    public static void main(String[] args) throws IOException {
        new Thread(new ConnectionAcceptor()).start();
    }
}
