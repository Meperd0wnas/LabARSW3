
# Lab ARSW3

## Daniel Ricardo Ruge Gomez

### Parte I

#### 1

![alt text](image.png)

El consumo de CPU que se puede ver en la ejecución del programa es por la clase Consumer, ya que su método run contiene un ciclo infinito (while(true)) que constantemente revisa si la cola tiene elementos (queue.size() > 0), y cuando está vacía, el hilo sigue ejecutándose sin detenerse, generando algo que se conoce como busy waiting. Esto provoca un uso innecesario de CPU, mientras que la clase Producer no causa este problema porque incluye pausas con Thread.sleep(1000) entre cada producción.


### 2 

![alt text](image-1.png)

cambios: 

Cambié el tipo de Queue<Integer> a BlockingQueue<Integer> (tambien toco cambiarlo en StartProduction), porque esta interfaz tiene métodos bloqueantes, tambien reemplacé if (queue.size() > 0) { poll() } por queue.take(), que espera hasta que haya un elemento disponible y evita el consumo excesivo de CPU y finalmente puse un InterruptedException para permitir salir del bucle limpiamente si se interrumpe el hilo.


JVMachine ahora: 


![alt text](image-2.png)