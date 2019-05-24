// Grupo: Jesus Vallejo Collados(x150319), David Martin Matamala(z170141)

import es.upm.babel.cclib.Monitor;

public class EnclavamientoMonitor implements Enclavamiento {

	Monitor mutex = new Monitor();
	boolean presencia;
	int [] via= {0,0,0,0};
	Control.Color [] color = {Control.Color.VERDE,Control.Color.VERDE,Control.Color.VERDE,Control.Color.VERDE};
	Monitor.Cond cFreno = mutex.newCond();
	Monitor.Cond cBarrera = mutex.newCond();
	Monitor.Cond [] cSemaforos = {mutex.newCond(),mutex.newCond(),mutex.newCond()};

	Boolean estadoFreno = false;
	Boolean estadoBarrera = true;
	Control.Color [] prevColor = {Control.Color.VERDE,Control.Color.VERDE,Control.Color.VERDE,Control.Color.VERDE};

	@Override
	public void avisarPresencia(boolean presencia) {
		mutex.enter();
		// chequeo de la PRE: no hay
		// chequeo de la CPRE y posible bloqueo: siempre cierto
		// implementacion de la POST
		this.presencia = presencia;
		//this.via= via; no es necesario porque no cambia
		ColoresCorrectos();	
		// codigo de desbloqueo
		interdesbloqueo();
		mutex.leave();
	}

	@Override
	public boolean leerCambioBarrera(boolean actual) {
		mutex.enter();
		boolean esperado=actual;
		// chequeo de la PRE:no hay
		// chequeo de la CPRE y posible bloqueo
		if (!(actual != (via[1]+via[2]==0))) {
			cBarrera.await();	
		}
		// implementacion de la POST
		esperado = (via[1]+via[2]==0);
		estadoBarrera=esperado;
		interdesbloqueo();
		mutex.leave();
		return esperado;
	}

	@Override
	public boolean leerCambioFreno(boolean actual) {
		mutex.enter();
		boolean esperado=actual;
		// chequeo de la PRE: no hay
		// chequeo de la CPRE y posible bloqueo
		if (!(actual != (via[1]>1 || via[2]>1 || (via[2]==1 && presencia)))) {
			cFreno.await();
		}
		// implementacion de la POST
		esperado = (via[1]>1 || via[2]>1 || (via[2]==1 && presencia));
		// codigo de desbloqueo
		estadoFreno=esperado;
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
			throw new PreconditionFailedException("excepcion");
		}
		return esperado;
	}

	@Override
	public void avisarPasoPorBaliza(int i) throws PreconditionFailedException{
		
		if(i!=0) {
			mutex.enter();
			via[i-1]=via[i-1]-1;
			via[i]=via[i]+1;
			ColoresCorrectos();
			interdesbloqueo();
			mutex.leave();
		}
		else {
			throw new PreconditionFailedException("excepcion");
		}
	}



	private void ColoresCorrectos() {
		mutex.enter();
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
		mutex.leave();
	}

	private void interdesbloqueo() {

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
		else if (cFreno.waiting()>0 && (((this.presencia && this.via[2]>0)||this.via[2]>1||this.via[1]>1)!= estadoFreno )) {
			
			cFreno.signal();
		}
	}



}
