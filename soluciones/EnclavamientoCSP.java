// Grupo: Jesus Vallejo Collados(x150319), David Martin Matamala(z170141)
import org.jcsp.lang.Alternative;
import org.jcsp.lang.AltingChannelInput;
import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.Guard;
import org.jcsp.lang.One2OneChannel;
import org.jcsp.lang.ProcessManager;

/**
 * Implementation using channel replication
 */
public class EnclavamientoCSP implements CSProcess, Enclavamiento {

	/** WRAPPER IMPLEMENTATION */
	/**
	 * Channels for receiving external requests
	 * just one channel for nonblocking requests
	 */

	private final One2OneChannel chAvisarPresencia     = Channel.one2one();
	private final One2OneChannel chAvisarPasoPorBaliza = Channel.one2one();
	// leerCambioBarrera blocks depending on a boolean parameter
	private final One2OneChannel chLeerCambioBarreraT  = Channel.one2one();
	private final One2OneChannel chLeerCambioBarreraF  = Channel.one2one();
	// leerCambioFreno blocks depending on a boolean parameter
	private final One2OneChannel chLeerCambioFrenoT    = Channel.one2one();
	private final One2OneChannel chLeerCambioFrenoF    = Channel.one2one();
	// leerCambioSemaforo blocks depending on a semaphore id and a colour
	private final One2OneChannel[][] chLeerCambioSemaforo =
			new One2OneChannel[3][3];


	public EnclavamientoCSP () {
		// pending initializations
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				chLeerCambioSemaforo[i][j] = Channel.one2one();
			}
		}
		new ProcessManager(this).start();
	} 

	@Override
	public void avisarPresencia(boolean presencia) {
		chAvisarPresencia.out().write(presencia);
	}

	@Override
	public void avisarPasoPorBaliza(int i) {
		if (i == 0 )
			throw new PreconditionFailedException("Baliza 0 no existe");

		chAvisarPasoPorBaliza.out().write(i);
	}

	@Override
	public boolean leerCambioBarrera(boolean abierta) {
		One2OneChannel chreply = Channel.one2one();
		if (abierta) {
			chLeerCambioBarreraT.out().write(chreply);
		} else {
			chLeerCambioBarreraF.out().write(chreply);
		}
		return (Boolean) chreply.in().read();
	}

	@Override
	public boolean leerCambioFreno(boolean accionado) {
		One2OneChannel chreply = Channel.one2one();
		if (accionado) {
			chLeerCambioFrenoT.out().write(chreply);
		} else {
			chLeerCambioFrenoF.out().write(chreply);
		}
		return (Boolean) chreply.in().read();
	}

	/** notice that the exception must be thrown outside the server */
	@Override
	public Control.Color leerCambioSemaforo (int i, Control.Color color) {
		if (i == 0 || i > 3)
			throw new PreconditionFailedException("Semaforo 0 no existe");

		One2OneChannel chreply = Channel.one2one();

		chLeerCambioSemaforo[i-1][color.ordinal()].out().write(chreply);

		return (Control.Color) chreply.in().read();
	}

	/** SERVER IMPLEMENTATION */
	@Override
	public void run() {
		// resource state is kept in the server
		// + TODO : Declarar el estado del recurso
		// + TODO : presencia &&  state initialization
		boolean presencia= false;
		// + TODO : tren &&  state initialization
		Integer[] via = {0,0,0,0};
		// + TODO : color &&  state initialization
		Control.Color [] color = {Control.Color.VERDE,Control.Color.VERDE,Control.Color.VERDE,Control.Color.VERDE} ;
		// mapping request numbers to channels and vice versa
		// 0 <--> chAvisarPresencia
		// 1 <--> chAvisarPasoPorBaliza
		// 2 <--> chLeerCambioBarreraT
		// 3 <--> chLeerCambioBarreraF
		// 4 <--> chLeerCambioFrenoT
		// 5 <--> chLeerCambioFrenoF
		// 6 + (3*(i-1)) + j <--> chLeerCambioSemaforo[i][j]
		Guard[] inputs = new AltingChannelInput[15];
		inputs[0] = chAvisarPresencia.in();
		inputs[1] = chAvisarPasoPorBaliza.in();
		inputs[2] = chLeerCambioBarreraT.in();
		inputs[3] = chLeerCambioBarreraF.in();
		inputs[4] = chLeerCambioFrenoT.in();
		inputs[5] = chLeerCambioFrenoF.in();
		for (int i = 6; i < 15; i++) {
			inputs[i] = chLeerCambioSemaforo[(i-6) / 3][(i-6) % 3].in();
		};

		Alternative services = new Alternative(inputs);
		int chosenService = 0;

		// conditional sincronization
		boolean[] sincCond = new boolean[15];
		// algunas condiciones de recepción no varían durante
		// la ejecución del programa
		// sincCond[0] = ...;
		// sincCond[1] = ...;
		sincCond[0]=true;// estas condiciones siempre se cumplen
		sincCond[1]=true;
		




		while (true){
			// actualizar sincronización condicional
			// TODO : actualizar los demás elementos de sincCond(cambian con caja ejecucion)
			sincCond[2]=(via[1]+via[2]!=0);// cpre leer cambio barrera true(barrera abierta = true)
			sincCond[3]=(via[1]+via[2]==0);// cpre leer cambio barrera false
			
			sincCond[4]=!(via[1] > 1|| via[2]>1||(via[2]==1 && presencia));// cpre leer cambio freno true(freno activado = true)
			sincCond[5]=  (via[1] > 1|| via[2]>1||(via[2]==1 && presencia));// cpre leer cambio freno false(freno activado = false)
			
			sincCond[6]=color[1]!=Control.Color.ROJO; // rojo de 1
			sincCond[7]=color[1]!=Control.Color.AMARILLO; // amarillo 1
			sincCond[8]=color[1]!=Control.Color.VERDE;// verde 1
			
			
			sincCond[9]=color[2]!=Control.Color.ROJO;// rojo de 2
			sincCond[10]=color[2]!=Control.Color.AMARILLO; // amarillo 2// aunque no se usa  el amarillo es necesario para el fairselect
			sincCond[11]=color[2]!=Control.Color.VERDE;// verde 2
			
			sincCond[12]=color[3]!=Control.Color.ROJO;// rojo de 3
			sincCond[13]=color[3]!=Control.Color.AMARILLO; // amarillo 3// aunque no se usa ni el rojo ni el amarillo es necesario para el fairselect
			sincCond[14]=color[3]!=Control.Color.VERDE;// verde 3

			// esperar petición
			chosenService = services.fairSelect(sincCond);
			One2OneChannel chreply; // lo usamos para contestar a los clientes

			switch(chosenService){
			case 0: // avisarPresencia
				//@ assume inv & pre && cpre of operation;
				// TODO : leer mensaje del canal
				// TODO : actualizar estado del recurso
				
				presencia = (Boolean) chAvisarPresencia.in().read();
				ColoresCorrectos(color,via,presencia);
				break;
			case 1: // avisarPasoPorBaliza
				//@ assume inv & pre && cpre of operation;
				// TODO : leer mensaje del canal
				int p = (Integer) chAvisarPasoPorBaliza.in().read();
				// TODO : actualizar estado del recurso
				via[p-1]=via[p-1]-1;
				via[p]=via[p]+1;
				ColoresCorrectos(color,via,presencia);		
				break;
			case 2: // leerCambioBarrera(true)
				//@ assume inv & pre && cpre of operation;
				// TODO : leer mensaje del canal y procesar peticion
				chreply= (One2OneChannel) chLeerCambioBarreraT.in().read();
				// TODO : calcular valor a devolver al cliente
				
				// TODO : contestar al cliente
				chreply.out().write(false);
				break;
			case 3: // leerCambioBarrera(false)
				//@ assume inv & pre && cpre of operation;
				// TODO : leer mensaje del canal y procesar peticion
				chreply= (One2OneChannel) chLeerCambioBarreraF.in().read();
				// TODO : calcular valor a devolver al cliente
				
				// TODO : contestar al cliente
				chreply.out().write(true);
				break;
			case 4: // leerCambioFreno(true)
				//@ assume inv & pre && cpre of operation;
				// TODO : leer mensaje del canal y procesar peticion
				chreply= (One2OneChannel) chLeerCambioFrenoT.in().read();
				// TODO : calcular valor a devolver al cliente
				//
				
				// TODO : contestar al cliente
				chreply.out().write(false);
				
				break;
			case 5: // leerCambioFreno(false)
				//@ assume inv & pre && cpre of operation;
				// TODO : leer mensaje del canal y procesar peticion
				chreply= (One2OneChannel) chLeerCambioFrenoF.in().read();
				// TODO : calcular valor a devolver al cliente
				
				// TODO : contestar al cliente
				chreply.out().write(true);
				break;
			default: // leerCambioSemaforo(queSemaforo,queColor)
				// TODO : decodificar número de semáforo y color a partir del
				//        valor de chosenService
				int k = chosenService-6;// "poner a 0"
				int i = k /3;// calcula fila
				int j = k % 3;// calcula columna
				// TODO : leer mensaje del canal
				chreply = (One2OneChannel) chLeerCambioSemaforo[i][j].in().read();
				// TODO : contestar al cliente
				chreply.out().write(color[i+1]);// coincide la fila con el semaforo +1 , 4 semaforos , 3 filas por solo usar los sem 1,2,3
				break;
			} // SWITCH
		} // SERVER LOOP
	} // run()

	// métodos auxiliares varios
	// TODO : coloca aquí aquellos métodos que hayais
	//        usado en la otra práctica para ajustar
	//        luces de semaforos, evaluar CPREs, etc.
	//
	private Control.Color [] ColoresCorrectos(Control.Color [] color, Integer [] via, boolean presencia) {
		// en esta implementacion es necesario el paso de parametros
		if (via[1]>0) {
			color[1]=Control.Color.ROJO;
		}
		if (via[1] == 0 && (via[2]>0 || presencia)) {
			color[1]=Control.Color.AMARILLO;
		}
		if (via[1] == 0 && via[2] == 0 && !presencia) {
			color[1]= Control.Color.VERDE;
		}
		if (via[2]>0 || presencia) {
			color[2]= Control.Color.ROJO;
		}
		if (via[2]==0 && !presencia) {
			color[2]= Control.Color.VERDE;
		}
		color[3]= Control.Color.VERDE;
		return color;
	}


} // end CLASS
