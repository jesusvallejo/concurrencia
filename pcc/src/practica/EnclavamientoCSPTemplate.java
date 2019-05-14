package practica;
import java.util.ArrayList;
import java.util.List;

import org.jcsp.lang.Alternative;
import org.jcsp.lang.Any2OneChannel;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;

/** 
 * Enclavamiento implementation using CSP. 
 *
 */ 
public class EnclavamientoCSP implements CSProcess, Enclavamiento {

  /** WRAPPER IMPLEMENTATION */
  /**
   *  Channel for receiving external request for each method
   */
  private static final Any2OneChannel channel = Channel.any2one();

  @Override
  public void avisarPresencia(boolean presencia) {
    channel.out().write(presencia);
  }

  @Override
  public boolean leerCambioBarrera(boolean actual) {
    return ...;

  }

  @Override
  public boolean leerCambioFreno(boolean actual) {
    return ...;

  }

  /** notice that the exception can be thrown outside the server */
  @Override
  public Control.Color leerCambioSemaforo(int i, Control.Color color) {
    if (i == 0 )
      throw new PreViolationSharedResourceException("Semaforo 0 no existe");

    channel.out().write(...);

    return ...;
  }

  @Override
  public void avisarPasoPorBaliza(int i) {

  }


  /** SERVER IMPLEMENTATION */
  /**
   * Constants representing the method presented in the API
   */

  public EnclavamientoCSPTemplate() {
  }

  private static final int AVISAR_PRESENCIA = 0;
  private static final int LEER_CAMBIO_BARRERA = 1;
  private static final int LEER_CAMBIO_FRENO  = 2;
  private static final int LEER_CAMBIO_SEMAFORO  = 3;
  private static final int AVISAR_PASO_POR_BALIZA = 4;

  private static final int QUEUE_HEAD = 0;

  @Override
  public void run() {

    /** One entry for each method */
    Guard[] guards = {

    };

    Alternative services = new Alternative(guards);
    int chosenService = 0;

    One2OneChannel innerCh;

    while (true){
      chosenService = services.fairSelect();
      switch(chosenService){

      case AVISAR_PRESENCIA:

        break;

      case LEER_CAMBIO_BARRERA:
        //@ assume pre && cpre operation;

        break;

      case LEER_CAMBIO_FRENO:
        //@ assume pre && cpre operation;

        break;

      case LEER_CAMBIO_SEMAFORO:
        //@ assume pre && cpre operation;

        break;

      case AVISAR_PASO_POR_BALIZA:
        //@ assume pre && cpre operation;

        break;
      }


      /*
       * Unblocking code
       * Must always process all request which is associated CPRE holds
       */
      boolean anyResumed;
      do{
        anyResumed = false;

        //@ assert todas las peticiones han sido procesadas
      } while (anyResumed);

    } // end while
  } // end run


}