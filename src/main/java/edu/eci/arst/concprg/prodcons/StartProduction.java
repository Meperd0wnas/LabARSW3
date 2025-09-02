/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class StartProduction {
    
    
    public static void main(String[] args) {
        
        // Definimos el límite de stock
        int stockLimit = 1;


        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(stockLimit);

        
        new Producer(queue).start();
        new Consumer(queue).start();
    }
    

}