
# Lab ARSW3

## Daniel Ricardo Ruge Gomez

### Parte I

#### 1

![alt text](image.png)

El consumo de CPU que se puede ver en la ejecución del programa es por la clase Consumer, ya que su método run contiene un ciclo infinito (while(true)) que constantemente revisa si la cola tiene elementos (queue.size() > 0), y cuando está vacía, el hilo sigue ejecutándose sin detenerse, generando algo que se conoce como busy waiting. Esto provoca un uso innecesario de CPU, mientras que la clase Producer no causa este problema porque incluye pausas con Thread.sleep(1000) entre cada producción.


