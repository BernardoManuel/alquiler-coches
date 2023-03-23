package app.servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class ServidorHilo implements Runnable {

    private Socket socketCliente;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String nombreCliente;
    private DateFormat dateFormat;
    private Integer idCoche;
    private Integer opcion;
    private Random random = new Random();

    private final Integer RAPIDO = 2000;
    private final Integer LENTO = 3000;

    //Constantes de estados del coche reservado.
    private final String RESERVADO = "RESERVADO";
    private final String REGISTRANDO = "REGISTRANDO";
    private final String LIMPIANDO = "LIMPIANDO";
    private final String ENVIANDO = "ENVIANDO";
    private final String ENTREGADO = "ENTREGADO";
    private final String DISPONIBLE = "DISPONIBLE";

    private Integer tiempoUsoCoche;

    private String numeroReserva;



    public ServidorHilo(Socket socketCliente) {
        this.socketCliente=socketCliente;

        try {
            this.dataInputStream=new DataInputStream(socketCliente.getInputStream());
            this.dataOutputStream=new DataOutputStream(socketCliente.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void run() {
            try {

                nombreCliente = dataInputStream.readUTF();
                Thread.sleep(100);//retardo corto

                System.out.println("SERVIDOR: "+nombreCliente+" se ha conectado... \n");
                SocketServidor.writeActividad(nombreCliente+" se ha conectado...",nombreCliente);

                Thread.sleep(100);//retardo corto
                dataOutputStream.writeUTF("Bienvenido "+nombreCliente+". \n\n");

                do {
                    SocketServidor.writeActividad("Imprimiendo menu de opciones...",nombreCliente);
                    dataOutputStream.writeUTF("MENU DE OPCIONES");
                    dataOutputStream.writeUTF("1: Reservar un coche.");
                    dataOutputStream.writeUTF("2: Ver estado de reserva.");
                    dataOutputStream.writeUTF("3: Salir..."+"\n");

                    opcion = Integer.parseInt(dataInputStream.readUTF());

                    switch (opcion){

                        case 1:
                            SocketServidor.menuVehiculos(socketCliente);
                            SocketServidor.writeActividad("Imprimiendo menu de coches disponibles para reservar",nombreCliente);

                            dataOutputStream.writeUTF("\n"+"¿Qué vehículo le gustaría reservar? (ID)");
                            idCoche = Integer.parseInt(dataInputStream.readUTF());

                            System.out.println("SERVIDOR: "+nombreCliente+" ha solicitado reservar el coche con id: "+idCoche+".");
                            numeroReserva = numReservaGenerator(); //Creamos el numero de reserva

                            SocketServidor.writeReserva("Ha solicitado reservar el coche ",numeroReserva,nombreCliente); //Escribimos en numero_reserva.log
                            SocketServidor.writeActividad("Cliente ha solicitado reservar el coche con id:"+idCoche,nombreCliente); //Escribimos en actividad_servidor.log

                            dataOutputStream.writeUTF("Ha solicitado reservar el coche con clave: "+idCoche+". ");
                            dataOutputStream.writeUTF("Confirma la reserva del coche (S/N): "+ SocketServidor.getCoche(idCoche.toString()));
                            String respuesta = dataInputStream.readUTF();
                            Character respuestaConfirmacion = respuesta.charAt(0);

                            if(Character.toLowerCase(respuestaConfirmacion)=='s'){

                                dataOutputStream.writeUTF("Coche reservado!");

                                SocketServidor.actualizarEstado(idCoche.toString(), RESERVADO); // Actualizamos estado a RESERVADO
                                SocketServidor.writeReserva("Ha reservado el coche ",numeroReserva,nombreCliente); //Escribimos en numero_reserva.log
                                SocketServidor.writeActividad("Cliente ha reservado el coche con id:"+idCoche,nombreCliente); //Escribimos en actividad_servidor.log

                                dataOutputStream.writeUTF("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString())).concat("\n")); //Enviamos el estado del coche

                            } else if(Character.toLowerCase(respuestaConfirmacion)=='n'){
                                break;
                            }
                            break;

                        case 2:
                            SocketServidor.writeReserva("Ha solicitado ver el estado del coche. ",numeroReserva,nombreCliente); //Escribimos en numero_reserva.log
                            SocketServidor.writeActividad("Ha solicitado ver el estado del coche. "+idCoche,nombreCliente); //Escribimos en actividad_servidor.log
                            SocketServidor.actualizarEstado(idCoche.toString(), REGISTRANDO); // Actualizamos estado a REGISTRANDO
                            dataOutputStream.writeUTF("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString()))); //Enviamos el estado del coche
                            SocketServidor.writeReserva("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString())),numeroReserva,nombreCliente); //Escribimos en numero_reserva.log

                            Thread.sleep(random.nextInt(LENTO)+RAPIDO);
                            SocketServidor.actualizarEstado(idCoche.toString(),LIMPIANDO); // Actualizamos estado a LIMPIANDO
                            dataOutputStream.writeUTF("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString()))); //Enviamos el estado del coche
                            SocketServidor.writeReserva("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString())),numeroReserva,nombreCliente); //Escribimos en numero_reserva.log
                            SocketServidor.writeActividad("Mostrando estado del coche..."+idCoche,nombreCliente); //Escribimos en actividad_servidor.log

                            Thread.sleep(random.nextInt(LENTO)+RAPIDO);
                            SocketServidor.actualizarEstado(idCoche.toString(), ENVIANDO); // Actualizamos estado a ENVIANDO
                            dataOutputStream.writeUTF("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString()))); //Enviamos el estado del coche
                            SocketServidor.writeReserva("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString())),numeroReserva,nombreCliente); //Escribimos en numero_reserva.log
                            SocketServidor.writeActividad("Mostrando estado del coche..."+idCoche,nombreCliente); //Escribimos en actividad_servidor.log

                            Thread.sleep(random.nextInt(LENTO)+RAPIDO);
                            SocketServidor.actualizarEstado(idCoche.toString(), ENTREGADO); // Actualizamos estado a ENTREGADO
                            dataOutputStream.writeUTF("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString()))); //Enviamos el estado del coche
                            SocketServidor.writeReserva("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString())),numeroReserva,nombreCliente); //Escribimos en numero_reserva.log
                            SocketServidor.writeActividad("Mostrando estado del coche..."+idCoche,nombreCliente); //Escribimos en actividad_servidor.log

                            Thread.sleep(tiempoUsoCoche=random.nextInt(LENTO)+RAPIDO);
                            SocketServidor.actualizarEstado(idCoche.toString(), DISPONIBLE); // Actualizamos estado a DISPONIBLE
                            dataOutputStream.writeUTF("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString()))); //Enviamos el estado del coche
                            SocketServidor.writeReserva("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString())),numeroReserva,nombreCliente); //Escribimos en numero_reserva.log
                            SocketServidor.writeActividad("Mostrando estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString()))+idCoche,nombreCliente); //Escribimos en actividad_servidor.log

                            tiempoUsoCoche = tiempoUsoCoche / 1000;
                            dataOutputStream.writeUTF("Tiempo de uso del coche: ".concat(tiempoUsoCoche.toString()).concat(" segundos. \n")); //Enviamos el estado del coche
                            SocketServidor.writeReserva("Tiempo de uso del coche: ".concat(tiempoUsoCoche.toString()).concat(" segundos. "),numeroReserva,nombreCliente); //Escribimos en numero_reserva.log
                            SocketServidor.writeActividad("Tiempo de uso del coche: ".concat(tiempoUsoCoche.toString()).concat("segundos. ")+idCoche,nombreCliente); //Escribimos en actividad_servidor.log

                            Thread.sleep(random.nextInt(RAPIDO));

                            opcion=3;
                            break;
                        case 3:
                            socketCliente.close();
                    }
                } while (opcion!=3);


            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
    }



    /**
     * Método que genera un número de reserva tomando la fecha y hora actual, y la inicial del nombre del cliente
     * @return String que es el número de reserva formateado "yyyyMMdd-HHmmss-inicial"
     */
    private String numReservaGenerator(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        String fechaHora = formattedDateTime.replaceAll("/","").replaceAll(" ","-").replaceAll(":","");

        Character inicial = nombreCliente.charAt(0);

        return fechaHora.concat(inicial.toString());
    }


}
