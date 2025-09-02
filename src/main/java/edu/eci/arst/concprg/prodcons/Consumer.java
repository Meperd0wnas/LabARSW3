/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author hcadavid
 */
public class Consumer extends Thread {
    
    private BlockingQueue<Integer> queue;
    
    public Consumer(BlockingQueue<Integer> queue){
        this.queue = queue;        
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                // el hilo queda bloqueado hasta que haya un elemento (Si la cola está vacía)
                int elem = queue.take();
                System.out.println("Consumer consumes " + elem);

                // Consumidor lento
                Thread.sleep(2000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break; // salir del ciclo si el hilo es interrumpido
            }
        }
    }
}
