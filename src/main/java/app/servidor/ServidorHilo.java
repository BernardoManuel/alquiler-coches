package app.servidor;

import app.model.Coche;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.CharsetDecoder;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class ServidorHilo implements Runnable {

    private Socket socketCliente;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String nombreCliente;
    private DateFormat dateFormat;

    private Integer opcion;



    public ServidorHilo(Socket socketCliente, Map<Integer,Coche> listaCoches) {
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
                            Integer claveReservar = Integer.parseInt(dataInputStream.readUTF());

                            System.out.println("SERVIDOR: "+nombreCliente+" ha solicitado reservar el coche con id: "+claveReservar+".");
                            String numeroReserva = numReservaGenerator(); //Creamos el numero de reserva

                            SocketServidor.writeReserva("Ha solicitado reservar el coche ",numeroReserva,nombreCliente);
                            SocketServidor.writeActividad("Cliente ha solicitado reservar el coche con id:"+claveReservar,nombreCliente);

                            dataOutputStream.writeUTF("Ha solicitado reservar el coche con clave: "+claveReservar+". ");
                            dataOutputStream.writeUTF("Confirma la reserva del coche (S/N): "+ SocketServidor.getCoche(claveReservar.toString()));
                            String respuesta = dataInputStream.readUTF();
                            Character respuestaConfirmacion = respuesta.charAt(0);

                            if(Character.toLowerCase(respuestaConfirmacion)=='s'){
                                dataOutputStream.writeUTF("Coche reservado! \n");
                                //TODO reservar coche
                            } else if(Character.toLowerCase(respuestaConfirmacion)=='n'){
                                break;
                            }
                            break;
                        case 2:


                    }
                } while (opcion!=3);


            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
    }



    /**
     * Metodo que genera un numero de reserva tomando la fecha y hora actual, y un numero aleatorio
     * @return String que es el numero de reserva formatedo "yyyyMMdd-HHmmss-'numeroaleatorio'"
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
