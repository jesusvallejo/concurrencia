package practica;
import es.upm.babel.cclib.Monitor;
import practica.Control.Color;

public class EnclavamientoMonitor implements Enclavamiento {

  Monitor mutex = new Monitor();
  boolean presencia;
  int [] tren= {0,0,0,0};
  Control.Color [] color = {Color.VERDE,Color.VERDE,Color.VERDE,Color.VERDE};
  Monitor.Cond cFreno = new EnclavamientoMonitor().mutex.newCond();
  Monitor.Cond cBarrera = new EnclavamientoMonitor().mutex.newCond();
  Monitor.Cond [] cSemaforos = {new EnclavamientoMonitor().mutex.newCond(),new EnclavamientoMonitor().mutex.newCond(),new EnclavamientoMonitor().mutex.newCond()};
  
  
  @Override
  public void avisarPresencia(boolean presencia) {
    mutex.enter();
    // chequeo de la PRE: no hay
    // chequeo de la CPRE y posible bloqueo: siempre cierto
    // implementacion de la POST
    this.presencia = presencia;
    //this.tren= tren; no es necesario porque no cambia
    ColoresCorrectos();	
    // codigo de desbloqueo
    mutex.leave();
  }

  @Override
  public boolean leerCambioBarrera(boolean actual) {
    mutex.enter();
    boolean esperado=actual;
    // chequeo de la PRE:no hay
    // chequeo de la CPRE y posible bloqueo
    if (actual != (tren[1]+tren[2]==0)) {
    // implementacion de la POST
    esperado = (tren[1]+tren[2]==0);
    }
    // codigo de desbloqueo
    mutex.leave();
    return esperado;
  }

  @Override
  public boolean leerCambioFreno(boolean actual) {
    mutex.enter();
    boolean esperado=actual;
    // chequeo de la PRE: no hay
    // chequeo de la CPRE y posible bloqueo
    if (actual != (tren[1]>1 || tren[2]>1 || (tren[2]==1 && presencia))) {
        // implementacion de la POST
        esperado = (tren[1]>1 || tren[2]>1 || (tren[2]==1 && presencia));
        }
    // codigo de desbloqueo
    mutex.leave();
    return esperado;
  }

  @Override
  public Control.Color leerCambioSemaforo(int i, Control.Color actual) {
    mutex.enter();
    Control.Color esperado=actual;
    // chequeo de la PRE
    if(i !=0) {
    // chequeo de la CPRE y posible bloqueo
    	if (actual != color[i]) {
    // implementacion de la POST
    		esperado=color[i];
    	}
    }
    // codigo de desbloqueo
    mutex.leave();
    return esperado;
  }

  @Override
  public void avisarPasoPorBaliza(int i) {
    mutex.enter();
    // chequeo de la PRE
    if(i!=0) {
    // chequeo de la CPRE y posible bloqueo: siempre cierto
    // implementacion de la POST
    	tren[i-1]=tren[i-1]-1;
    	tren[i]=tren[i]+1;
    	ColoresCorrectos();
    }
    // codigo de desbloqueo
    mutex.leave();
  }
  
  
  
  private void ColoresCorrectos() {
	  if (tren[1]>0) {
		  color[1]=Color.ROJO; 
	  }
	  if (tren[1] == 0 && (tren[2]>0) || presencia) {
		  color[1]=Color.AMARILLO; 
	  }
	  if (tren[1] == 0 && tren[2] == 0 && !presencia) {
		  color[1]= Color.VERDE;  
	  }
	  if (tren[2]>0 || presencia) {
		  color[2]= Color.ROJO;
	  }
	  if (tren[2]==0 && !presencia) {
		  color[2]= Color.VERDE;
	  }
	  color[3]= Color.VERDE;
	  }
 
}
