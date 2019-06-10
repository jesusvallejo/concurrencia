// Grupo: Jesus Vallejo Collados(x150319), David Martin Matamala(z170141)

import es.upm.babel.cclib.Monitor;

public class EnclavamientoMonitor implements Enclavamiento {

	Monitor mutex = new Monitor();
	//recursos necesarios marcados por el ctad ,e inicializados acorde 
	boolean presencia;// inicializacion por defecto(false)
	int [] via= {0,0,0,0};// inicializacion a 0 todos los tramos de via(tren en el ctad, nombre cambiado ya en mi opinión representa mejor la variable)
	Control.Color [] color = {Control.Color.VERDE,Control.Color.VERDE,Control.Color.VERDE,Control.Color.VERDE};// inicializacion a verde 
	//creacion e inicializacion de las condiciones de monitor
	Monitor.Cond cFreno = mutex.newCond();// condicion para parar/seguir ejecucion de freno
	Monitor.Cond cBarrera = mutex.newCond();// condicion para parar/seguir ejecucion de barrera
	Monitor.Cond [] cSemaforos = {mutex.newCond(),mutex.newCond(),mutex.newCond()};// condicion para parar/seguir ejecucion de cada uno de los tres semaforos realmente utilizados

	Boolean estadoFreno = false;// guarda el estado anterior del freno, ejecucion comienza en false(freno desacticado)
	Boolean estadoBarrera = true;// guarda el estado anterior de la barrera, ejecucion comienza en true(barrera abierta)
	Control.Color [] prevColor = {Control.Color.VERDE,Control.Color.VERDE,Control.Color.VERDE,Control.Color.VERDE};// guarda el estado anterior de los semaforos,comienza como el recurso

	@Override
	public void avisarPresencia(boolean presencia) {
		mutex.enter();
		// chequeo de la PRE: NO HAY
		// chequeo de la CPRE y posible bloqueo: TRUE ALWAYS
		// implementacion de la POST
		this.presencia = presencia;//actualiazmos presencia
		ColoresCorrectos();
		// codigo de desbloqueo
		interdesbloqueo();
		mutex.leave();
	}

	@Override
	public boolean leerCambioBarrera(boolean actual) {
		mutex.enter();
		boolean esperado = (via[1]+via[2]==0);// calculamos el estado esperado
		estadoBarrera=esperado;//actualizamos el estado anterior
		// chequeo de la PRE:NO HAY
		// chequeo de la CPRE y posible bloqueo
		if (!(actual != esperado)) {// se bloquea si el estado no ha cambiado
			cBarrera.await();	
		}
		//codigo de desbloqueo
		interdesbloqueo();
		mutex.leave();
		return esperado;
	}

	@Override
	public boolean leerCambioFreno(boolean actual) {
		mutex.enter();
		boolean esperado = (via[1]>1 || via[2]>1 || (via[2]==1 && presencia));// calculamos esperado
		estadoFreno=esperado;// actualizamos el estado anterior
		// chequeo de la PRE: NO HAY
		// chequeo de la CPRE y posible bloqueo
		if (!(actual != esperado)) {
			cFreno.await();
		}
		// implementacion de la POST
		
		interdesbloqueo();
		mutex.leave();
		return esperado;
	}

	@Override
	public Control.Color leerCambioSemaforo(int i, Control.Color actual) {
		Control.Color esperado=actual;
		// chequeo de la PRE
		if(i !=0) {
			mutex.enter();
			prevColor[i]=color[i];
			// chequeo de la CPRE y posible bloqueo
			if (!(actual != color[i])) {
				cSemaforos[i-1].await();
			}
			// implementacion de la POST
			esperado=color[i];
			// codigo de desbloqueo
			interdesbloqueo();
			mutex.leave();
		}
		else {
			throw new PreconditionFailedException("excepcion");// lanzar excepcion fuera del mutex
		}
		return esperado;
	}

	@Override
	public void avisarPasoPorBaliza(int i) throws PreconditionFailedException{
		
		if(i!=0) {
			mutex.enter();
			via[i-1]=via[i-1]-1;// movemos el tren hacia adelante , quitando del estado anterior , añádiendo al actual
			via[i]=via[i]+1;
			ColoresCorrectos();
			interdesbloqueo();
			mutex.leave();
		}
		else {
			throw new PreconditionFailedException("excepcion");// lanzar excepcion fuera del mutex
		}
	}



	private void ColoresCorrectos() { // calcula cual deberia ser el color en funcion de los trenes y/o coches que hay
		mutex.enter();
		if (via[1]>0) {   // if anidados para que se comprueben todos los casos
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
		color[3]= Control.Color.VERDE;// siempre pasa
		mutex.leave();
	}

	private void interdesbloqueo() {
		// desbloquea las condiciones (ha habido un cambio), solo se puede hacer un desbloqueo por cada llamada a desbloqueo, sino excepcion
		//comprueba si hay condicion pendiente y si ha habio cambio, previo vs actual
		if (cSemaforos[2].waiting()>0 &&  this.prevColor[3]!=color[3]) {
			cSemaforos[2].signal();
		}
		else if (cSemaforos[1].waiting()>0 &&  this.prevColor[2]!=color[2]) {
			cSemaforos[1].signal();
		}
		else if (cSemaforos[0].waiting()>0 &&  this.prevColor[1]!=color[1]) {
			cSemaforos[0].signal();
		}
		else if(cBarrera.waiting()>0 && estadoBarrera != (via[1]+via[2]==0)) {
			cBarrera.signal();
		}
		else if (cFreno.waiting()>0 && (((this.presencia && this.via[2]==1)||this.via[2]>1||this.via[1]>1)!= estadoFreno )) {
			cFreno.signal();
		}
	}



}
