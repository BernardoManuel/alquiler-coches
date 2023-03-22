package app.servidor;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SocketServidor {
    private Integer puerto;
    private String host;
    private Boolean servidorRunning;
    private ServidorHilo servidorHilo;
    private ServerSocket serverSocket;

    private static File actividadServidorLog;

    private static File numeroReservaLog;
    private static File vehiculosLog;

    private static BufferedReader bufferedReaderVehiculos;
    private static BufferedWriter bufferedWriterReserva;
    private static BufferedWriter bufferedWriterActividad;




    public SocketServidor(Integer puerto, String host) throws IOException {
        this.puerto = puerto;
        this.host = host;
        this.servidorRunning=true;

        InetSocketAddress address = new InetSocketAddress(host,puerto);
        serverSocket = new ServerSocket();
        serverSocket.bind(address);

        //Inicializamos los archivos log
        actividadServidorLog = new File("src/main/resources/logs/actividad_servidor.log");
        numeroReservaLog = new File("src/main/resources/logs/numero_reserva.log");
        //Inicializamos los lectores y escritores de archivos log
        bufferedWriterReserva = new BufferedWriter(new FileWriter(numeroReservaLog));
        bufferedWriterActividad = new BufferedWriter(new FileWriter(actividadServidorLog));
    }

    public void start() throws IOException {
        while(servidorRunning){
            System.out.println("SERVIDOR: esperando conexiones...");

            Socket cliente = serverSocket.accept();
            System.out.println("SERVIDOR: conexion establecida...");
            servidorHilo = new ServidorHilo(cliente);

            Thread hilo = new Thread(servidorHilo);
            hilo.start();
        }
    }


    /**
     * Metodo estatico sincronizado que envia al cliente el menu de vehiculos disponibles
     * es decir: Estado = DISPONIBLE
     * @param socketCliente
     * @throws IOException
     */
    public static synchronized void menuVehiculos(Socket socketCliente) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(socketCliente.getOutputStream());
        vehiculosLog = new File("src/main/resources/logs/vehiculos.log");
        bufferedReaderVehiculos = new BufferedReader(new FileReader(vehiculosLog));

        dataOutputStream.writeUTF("------ MENU DE VEHÍCULOS ------");

        String line;
        while((line=bufferedReaderVehiculos.readLine())!=null){
            //Envia unicamente los coches que tienen estado = "DISPONIBLE"
            if(line.contains("DISPONIBLE")){
                dataOutputStream.writeUTF(line);
            }
        }
        bufferedReaderVehiculos.close();
    }


    public static synchronized void writeReserva(String mensaje, String numeroReserva, String nombreCliente) throws IOException {
        numeroReservaLog = new File("src/main/resources/logs/numero_reserva.log");
        bufferedWriterReserva = new BufferedWriter(new FileWriter(numeroReservaLog));

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        bufferedWriterReserva.write(formattedDateTime+ " RESERVA: '"+numeroReserva+"'. CLIENTE: "+nombreCliente+" "+mensaje+". \n");
        bufferedWriterReserva.close();
    }

    public static synchronized void writeActividad(String mensaje, String nombreCliente) throws IOException {
        numeroReservaLog = new File("src/main/resources/logs/actividad_servidor.log");
        bufferedWriterReserva = new BufferedWriter(new FileWriter(numeroReservaLog));

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        bufferedWriterReserva.write(formattedDateTime+ " SERVIDOR: "+mensaje+". CLIENTE: "+nombreCliente+"\n");
        bufferedWriterReserva.close();
    }

    /**
     * Metodo estatico sincronizado que devuelve la linea completa en la que se encuentra el coche.
     * @param idCoche id del coche del que se devuelve la linea
     * @return
     * @throws IOException
     */
    public static synchronized String getCoche(String idCoche) throws IOException {
        vehiculosLog = new File("src/main/resources/logs/vehiculos.log");
        bufferedReaderVehiculos = new BufferedReader(new FileReader(vehiculosLog));
        String coche = null;

        String line;
        while((line=bufferedReaderVehiculos.readLine())!=null){
            if(line.contains("\"id\":".concat(idCoche).concat(","))){
                coche = line;
                break;
            }
        }
        bufferedReaderVehiculos.close();

        return coche;
    }

    /**
     * Metodo estatico sincronizado que actualiza el estado del vehiculo, al pasado por parametro
     * @param idCoche id del vehiculo a actualizar
     * @param estado String que especifica el estado en el que esta el vehiculo.
     * @throws IOException
     */
    public static synchronized void actualizarEstado(String idCoche, String estado) throws IOException {
        vehiculosLog = new File("src/main/resources/logs/vehiculos.log");
        BufferedReader bufferedReaderVehiculos = new BufferedReader(new FileReader(vehiculosLog));

        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = bufferedReaderVehiculos.readLine()) != null) {
            if (line.contains("\"id\":".concat(idCoche).concat(","))) {
                int indexInicio = line.indexOf("Estado") + 10;
                int indexFin = line.indexOf("}") - 1;
                line = line.substring(0, indexInicio) + estado + line.substring(indexFin);
            }
            stringBuilder.append(line).append("\n");
        }
        bufferedReaderVehiculos.close();

        // Actualizar el archivo con los cambios
        FileWriter fileWriter = new FileWriter(vehiculosLog);
        fileWriter.write(stringBuilder.toString());
        fileWriter.close();
    }


    /**
     * Metodo estatico sincronizado que devuelve el estado del coche.
     * @param idCoche
     * @return
     * @throws IOException
     */
    public static synchronized String getEstado(String idCoche) throws IOException {
        vehiculosLog = new File("src/main/resources/logs/vehiculos.log");
        BufferedReader bufferedReaderVehiculos = new BufferedReader(new FileReader(vehiculosLog));
        String estado = "";

        String line;
        while ((line = bufferedReaderVehiculos.readLine()) != null) {
            if (line.contains("\"id\":".concat(idCoche).concat(","))) {
                //Inicio de la cadena estado
                int indexInicio = line.indexOf("Estado") + 10;
                //Fin de la cadena estado
                int indexFin = line.indexOf("}") - 1;

                estado = line.substring(indexInicio,indexFin);
            }
        }
        bufferedReaderVehiculos.close();

        return estado;
    }


    public static void main(String[] args) throws IOException {

        int puerto = 23233;
        String host = "localhost";

        SocketServidor socketServidor = null;
        try {

            socketServidor = new SocketServidor(puerto,host);

            socketServidor.start();

        } catch (IOException e) {
            throw new RuntimeException(e);

        }


    }

}
