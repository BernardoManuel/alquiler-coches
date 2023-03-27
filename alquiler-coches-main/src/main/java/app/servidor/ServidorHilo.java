package app.servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class ServidorHilo implements Runnable {

    private Socket socketCliente;
    private Semaphore semaforoReservas;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String nombreCliente;
    private DateFormat dateFormat;
    private Integer idCoche;
    private Integer opcion;
    private Random random = new Random();

    private final Integer RAPIDO = 2000;
    private final Integer LENTO = 3000;

    // Constantes de estados del coche reservado.
    private final String RESERVADO = "RESERVADO";
    private final String REGISTRANDO = "REGISTRANDO";
    private final String LIMPIANDO = "LIMPIANDO";
    private final String ENVIANDO = "ENVIANDO";
    private final String ENTREGADO = "ENTREGADO";
    private final String DISPONIBLE = "DISPONIBLE";

    private final String ERROR_NO_RESERVA = "\n ERROR: Aun no ha reservado ningun vehiculo. \n";
    private final String ERROR_VER_ESTADO = "\n ERROR: Debe ver el estado de su reserva antes de reservar otro vehiculo. \n";

    private Integer tiempoUsoCoche;

    private String numeroReserva;

    private Boolean clienteConectado = true;

    /**
     * Constructor de la clase ServidorHilo
     *
     * @param socketCliente    socket del cliente conectado : Socket
     * @param semaforoReservas
     */
    public ServidorHilo(Socket socketCliente, Semaphore semaforoReservas) {
        this.socketCliente = socketCliente;
        this.semaforoReservas = semaforoReservas;

        try {
            this.dataInputStream = new DataInputStream(socketCliente.getInputStream());
            this.dataOutputStream = new DataOutputStream(socketCliente.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Metodo run() de ServidorHilo. Maneja la logica de la conexion para cada cliente.
     */
    public void run() {
        try {

            nombreCliente = dataInputStream.readUTF();
            Thread.sleep(100);// retardo corto

            System.out.println("SERVIDOR: " + nombreCliente + " se ha conectado... \n");
            SocketServidor.writeActividad(nombreCliente + " se ha conectado...", nombreCliente);

            Thread.sleep(100);// retardo corto
            dataOutputStream.writeUTF("Bienvenido " + nombreCliente + ". \n\n");

            do {
                SocketServidor.writeActividad("Imprimiendo menu de opciones...", nombreCliente);
                dataOutputStream.writeUTF("MENU DE OPCIONES");
                dataOutputStream.writeUTF("1: Reservar un coche.");
                dataOutputStream.writeUTF("2: Ver estado de reserva.");
                dataOutputStream.writeUTF("3: Salir..." + "\n");

                opcion = Integer.parseInt(dataInputStream.readUTF());

                switch (opcion) {

                    case 1:
                        if (numeroReserva == null) {
                            SocketServidor.menuVehiculos(socketCliente);
                            SocketServidor.writeActividad("Imprimiendo menu de coches disponibles para reservar",nombreCliente);

                            dataOutputStream.writeUTF("\n" + "¿Qué vehículo le gustaría reservar? (ID)");
                            idCoche = Integer.parseInt(dataInputStream.readUTF());

                            System.out.println("SERVIDOR: " + nombreCliente + " ha solicitado reservar el coche con id: "+ idCoche + ".");
                            numeroReserva = numReservaGenerator(); // Creamos el numero de reserva

                            SocketServidor.writeReserva("Ha solicitado reservar el coche ", numeroReserva,nombreCliente); // Escribimos en numero_reserva.log
                            SocketServidor.writeActividad("Cliente ha solicitado reservar el coche con id:" + idCoche,nombreCliente); // Escribimos en actividad_servidor.log

                            dataOutputStream.writeUTF("Ha solicitado reservar el coche con clave: " + idCoche + ". ");
                            dataOutputStream.writeUTF("Confirma la reserva del coche (S/N): "+ SocketServidor.getCoche(idCoche.toString()));
                            String respuesta = dataInputStream.readUTF();
                            Character respuestaConfirmacion = respuesta.charAt(0);

                            if (Character.toLowerCase(respuestaConfirmacion) == 's') {

                                dataOutputStream.writeUTF("Coche reservado!");

                                SocketServidor.actualizarEstado(idCoche.toString(), RESERVADO); // Actualizamos estado a RESERVADO
                                SocketServidor.writeReserva("Ha reservado el coche ", numeroReserva, nombreCliente); // Escribimos en numero_reserva.log
                                SocketServidor.writeActividad("Cliente ha reservado el coche con id:" + idCoche,nombreCliente); // Escribimos en actividad_servidor.log

                                dataOutputStream.writeUTF("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString())).concat("\n")); // Enviamos el estado del coche
                            } else if (Character.toLowerCase(respuestaConfirmacion) == 'n') {
                                numeroReserva = null;
                                break;
                            }
                        } else {
                            // Si el cliente ya ha reservado un coche pero no ha visto el estado de su reserva.
                            dataOutputStream.writeUTF(ERROR_VER_ESTADO); // Enviamos el mensaje de error al cliente.
                        }
                        break;

                    case 2:
                        if (numeroReserva != null) {
                            SocketServidor.writeReserva("Ha solicitado ver el estado del coche. ", numeroReserva,nombreCliente); // Escribimos en numero_reserva.log
                            SocketServidor.writeActividad("Ha solicitado ver el estado del coche. " + idCoche,nombreCliente); // Escribimos en actividad_servidor.log
                            SocketServidor.actualizarEstado(idCoche.toString(), REGISTRANDO); // Actualizamos estado a REGISTRANDO
                            dataOutputStream.writeUTF("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString()))); // Enviamos el estado del coche
                            SocketServidor.writeReserva("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString())),numeroReserva, nombreCliente); // Escribimos en numero_reserva.log

                            Thread.sleep(random.nextInt(LENTO) + RAPIDO);
                            SocketServidor.actualizarEstado(idCoche.toString(), LIMPIANDO); // Actualizamos estado a LIMPIANDO
                            dataOutputStream.writeUTF("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString()))); // Enviamos el estado del coche
                            SocketServidor.writeReserva("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString())),numeroReserva, nombreCliente); // Escribimos en numero_reserva.log
                            SocketServidor.writeActividad("Mostrando estado del coche..." + idCoche, nombreCliente); // Escribimos en actividad_servidor.log

                            Thread.sleep(random.nextInt(LENTO) + RAPIDO);
                            SocketServidor.actualizarEstado(idCoche.toString(), ENVIANDO); // Actualizamos estado a ENVIANDO
                            dataOutputStream.writeUTF("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString()))); // Enviamos el estado del coche
                            SocketServidor.writeReserva("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString())),numeroReserva, nombreCliente); // Escribimos en numero_reserva.log
                            SocketServidor.writeActividad("Mostrando estado del coche..." + idCoche, nombreCliente); // Escribimos en actividad_servidor.log

                            Thread.sleep(random.nextInt(LENTO) + RAPIDO);
                            SocketServidor.actualizarEstado(idCoche.toString(), ENTREGADO); // Actualizamos estado a ENTREGADO
                            dataOutputStream.writeUTF("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString()))); // Enviamos el estado del coche
                            SocketServidor.writeReserva("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString())),numeroReserva, nombreCliente); // Escribimos en numero_reserva.log
                            SocketServidor.writeActividad("Mostrando estado del coche..." + idCoche, nombreCliente); // Escribimos en actividad_servidor.log

                            Thread.sleep(tiempoUsoCoche = random.nextInt(LENTO) + RAPIDO);
                            SocketServidor.actualizarEstado(idCoche.toString(), DISPONIBLE); // Actualizamos estado a DISPONIBLE
                            dataOutputStream.writeUTF("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString()))); // Enviamos el estado del coche
                            SocketServidor.writeReserva("Estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString())),numeroReserva, nombreCliente); // Escribimos en numero_reserva.log
                            SocketServidor.writeActividad("Mostrando estado del coche: ".concat(SocketServidor.getEstado(idCoche.toString()))+ idCoche,nombreCliente); // Escribimos en actividad_servidor.log

                            tiempoUsoCoche = tiempoUsoCoche / 1000;
                            dataOutputStream.writeUTF("Tiempo de uso del coche: ".concat(tiempoUsoCoche.toString()).concat(" segundos. \n")); // Enviamos el estado del coche
                            SocketServidor.writeReserva("Tiempo de uso del coche: ".concat(tiempoUsoCoche.toString()).concat(" segundos. "),numeroReserva, nombreCliente); // Escribimos en numero_reserva.log
                            SocketServidor.writeActividad("Tiempo de uso del coche: ".concat(tiempoUsoCoche.toString()).concat("segundos. ")+ idCoche,nombreCliente); // Escribimos en actividad_servidor.log

                            Thread.sleep(random.nextInt(RAPIDO));

                            // Colocamos el numero de reserva a null para que el cliente pueda reservar otro coche.
                            numeroReserva = null;
                        } else {
                            // Si el cliente no ha reservado ningun coche aun
                            dataOutputStream.writeUTF(ERROR_NO_RESERVA); // Enviamos el mensaje de error al cliente.
                            break;
                        }
                        break;

                    case 3:
                        //Si el cliente tiene un coche reservado:
                        if(numeroReserva!=null){
                            dataOutputStream.writeUTF("Usted tiene un vehiculo reservado, desea cancelar la reserva? (S/N)");
                            String respuesta = dataInputStream.readUTF();
                            Character respuestaConfirmacion = respuesta.charAt(0);
                            if (Character.toLowerCase(respuestaConfirmacion) == 's') {
                                numeroReserva=null;
                                SocketServidor.actualizarEstado(idCoche.toString(), DISPONIBLE); // Actualizamos estado a DISPONIBLE
                                dataOutputStream.writeUTF("Reserva cancelada! ");
                                dataOutputStream.writeUTF("Hasta luego "+nombreCliente+"\n"); // Enviamos el mensaje de despedida al cliente
                                clienteConectado=false;// salimos del bucle while
                                semaforoReservas.release(1); //Liberamos el puesto del semaforo
                                semaforoReservas.release();// Liberamos el semaforo
                                socketCliente.close();//Cerramos la conexion
                            } else if (Character.toLowerCase(respuestaConfirmacion) == 'n') {
                                break;
                            }
                        } else if(numeroReserva==null){
                            //Si el cliente no tiene ningun coche reservado, le permitimos salir.
                            dataOutputStream.writeUTF("Hasta luego " + nombreCliente + "\n"); // Enviamos el mensaje de despedida al cliente
                            clienteConectado=false;// salimos del bucle while
                            semaforoReservas.release(1); //Liberamos el puesto del semaforo
                            semaforoReservas.release();//Liberamos el semaforo
                            socketCliente.close();//Cerramos la conexion
                        }
                }
            } while (clienteConectado);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método que genera un número de reserva tomando la fecha y hora actual, y la
     * inicial del nombre del cliente
     *
     * @return String que es el número de reserva formateado
     * "yyyyMMdd-HHmmss-inicial"
     */
    private String numReservaGenerator() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        String fechaHora = formattedDateTime.replaceAll("/", "").replaceAll(" ", "-").replaceAll(":", "");

        Character inicial = nombreCliente.charAt(0);

        return fechaHora.concat(inicial.toString());
    }

}
