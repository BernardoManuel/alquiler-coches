package app.cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class SocketCliente {

    private Integer puerto;
    private String host;

    private Boolean clienteRunning;

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private Scanner teclado;

    private String nombre;

    /**
     * Constructor de la clase SocketCliente
     * @param puerto numero de puerto
     * @param host direccion del host.
     */
    public SocketCliente(Integer puerto, String host) {
        this.puerto = puerto;
        this.host = host;
        this.clienteRunning = true;
        teclado = new Scanner(System.in);
    }

    /**
     * Método start del cliente que solicita conexion al servidor.
     * Una vez establecida la conexion, inicializa los Input y Output Streams,
     * envia el nombre del cliente (introducido por teclado) al servidor y
     * llama a los metodos recibirDatos() y enviarDatos()
     *
     * @throws IOException
     */
    public void start() throws IOException {

            //SOLICITAMOS LA CONEXION PARA REALIZAR UNA RESERVA
            Socket socket = new Socket(host,puerto);
            System.out.println("CLIENTE: conectado al servidor...");

            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            System.out.println("Indique su nombre:");
            nombre = teclado.nextLine();
            dataOutputStream.writeUTF(nombre);
            //lanzar hilo cliente
            recibirDatos();
            enviarDatos();
    }

    /**
     * Metodo void que lanza un hilo mientras el cliente este conectado,
     * que se encarga de imprimir los datos recibidos del servidor.
     */
    public void recibirDatos(){
        new Thread(() -> {
            while (clienteRunning){
                try {
                    String msj = dataInputStream.readUTF();
                    System.out.println(msj);
                } catch (IOException e) {
                    stop();
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    /**
     * Metodo void que lanza un hilo mientras el cliente este conectado,
     * que se encarga de enviar constantemente los datos introducidos por
     * teclado al socket servidor.
     */
    public void enviarDatos(){
        new Thread(() -> {
            while (clienteRunning){
                try {
                    String msj = teclado.nextLine();
                    dataOutputStream.writeUTF(msj);
                } catch (IOException e) {
                    stop();
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    /**
     * Metodo void que cierra los flujos de datos y el Scanner de teclado.
     */
    public void stop(){
        try{
            dataInputStream.close();
            dataOutputStream.close();
            teclado.close();
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        int puerto = 23222;
        String host = "localhost";

        SocketCliente socketCliente = new SocketCliente(puerto,host);

        try {

            socketCliente.start();


        } catch (Exception e){
            socketCliente.stop();
            e.printStackTrace();
        }


    }
}
