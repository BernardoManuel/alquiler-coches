package app.servidor;

import app.model.Coche;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class SocketServidor {
    private Integer puerto;
    private String host;
    private Boolean servidorRunning;
    private ServidorHilo servidorHilo;
    private static Map<Integer,Coche> listaCoches;
    private static Map<Integer,Coche> listaCochesReservados;
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

        // crear una lista de coches.
        listaCoches = new HashMap<Integer,Coche>();
        // insertamos coches en la lista
        listaCoches.put(1,new Coche("Honda", "Civic", 2019, 4));
        listaCoches.put(2,new Coche("Ford", "Mustang", 2015, 2));
        listaCoches.put(3,new Coche("Chevrolet", "Camaro", 2021, 2));
        listaCoches.put(4,new Coche("Toyota", "Corolla", 2022, 4));
        listaCoches.put(5,new Coche("BMW", "M5", 2022, 4));
        listaCoches.put(6,new Coche("Mercedes-Benz", "S-Class", 2020, 4));
        listaCoches.put(7,new Coche("Audi", "A4", 2018, 4));
        listaCoches.put(8,new Coche("Tesla", "Model S", 2021, 4));
        listaCoches.put(9,new Coche("Lamborghini", "Aventador", 2019, 2));
        listaCoches.put(10,new Coche("Ferrari", "488 GTB", 2022, 2));
        listaCoches.put(11,new Coche("Porsche", "911", 2021, 2));
        listaCoches.put(12,new Coche("Jaguar", "F-Type", 2022, 2));
        listaCoches.put(13,new Coche("Maserati", "Ghibli", 2021, 4));
        listaCoches.put(14,new Coche("McLaren", "720S", 2022, 2));
        listaCoches.put(15,new Coche("Bugatti", "Chiron", 2021, 2));

        listaCochesReservados = new HashMap<Integer,Coche>();
    }

    public void start() throws IOException {
        while(servidorRunning){
            System.out.println("SERVIDOR: esperando conexiones...");

            Socket cliente = serverSocket.accept();
            System.out.println("SERVIDOR: conexion establecida...");
            servidorHilo = new ServidorHilo(cliente, listaCoches);

            Thread hilo = new Thread(servidorHilo);
            hilo.start();
        }
    }


    /**
     * Metodo sinconizado que realiza la reserva del coche
     *
     * @param claveCoche    hace referencia al key del HashMap correspondiente al coche
     *                      Añade el coche a una lista de coches reservados y lo remueve de la lista de coches disponibles
     * @param nombreCliente
     */
    public static synchronized void reservarCoche(Integer claveCoche, String nombreCliente){
        //Colocamos el coche en la lista de coches reservados.
        Coche coche = listaCoches.get(claveCoche);
        listaCochesReservados.put(coche.hashCode(),coche);

        listaCoches.remove(claveCoche);
        System.out.println("SERVIDOR: "+nombreCliente+" ha reservado el coche: "+claveCoche+" "+coche.toString());
    }

//    /**
//     * Metodo sinconizado que realiza la reserva del coche
//     *
//     * @param claveCoche    hace referencia al key del HashMap correspondiente al coche
//     *                      Añade el coche a una lista de coches reservados y lo remueve de la lista de coches disponibles
//     * @param nombreCliente
//     */
//    public static synchronized void reservarCoche(Integer claveCoche, String nombreCliente){
//        //Colocamos el coche en la lista de coches reservados.
//        Coche coche = listaCoches.get(claveCoche);
//        listaCochesReservados.put(coche.hashCode(),coche);
//
//        listaCoches.remove(claveCoche);
//        System.out.println("SERVIDOR: "+nombreCliente+" ha reservado el coche: "+claveCoche+" "+coche.toString());
//    }


    public static synchronized void menuVehiculos(Socket socketCliente) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(socketCliente.getOutputStream());
        vehiculosLog = new File("src/main/resources/logs/vehiculos.log");
        bufferedReaderVehiculos = new BufferedReader(new FileReader(vehiculosLog));

        dataOutputStream.writeUTF("------ MENU DE VEHÍCULOS ------");

        String line;
        while((line=bufferedReaderVehiculos.readLine())!=null){
            dataOutputStream.writeUTF(line);
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

    public static synchronized void writeActividad(String mensaje, String numeroReserva, String nombreCliente) throws IOException {
        numeroReservaLog = new File("src/main/resources/logs/vehiculos.log");
        bufferedWriterReserva = new BufferedWriter(new FileWriter(numeroReservaLog));

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        bufferedWriterReserva.write(formattedDateTime+ " RESERVA: '"+numeroReserva+"'. CLIENTE: "+nombreCliente+" "+mensaje+". \n");
        bufferedWriterReserva.close();
    }

    public static synchronized String getCoche(String idCoche) throws IOException {
        vehiculosLog = new File("src/main/resources/logs/vehiculos.log");
        bufferedReaderVehiculos = new BufferedReader(new FileReader(vehiculosLog));
        String coche = null;

        String line;
        while((line=bufferedReaderVehiculos.readLine())!=null){
            if(line.contains(idCoche)){
                coche = line;
                break;
            }
        }
        bufferedReaderVehiculos.close();

        return coche;
    }

    public static void main(String[] args) throws IOException {

        int puerto = 1234;
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
