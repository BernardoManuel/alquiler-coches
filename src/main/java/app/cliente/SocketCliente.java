package app.cliente;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;

public class SocketCliente {

    private Integer puerto;
    private String host;

    private Boolean clienteRunning;

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private Scanner teclado;

    private String nombre;

    public SocketCliente(Integer puerto, String host) {
        this.puerto = puerto;
        this.host = host;
        this.clienteRunning = true;
        teclado = new Scanner(System.in);
    }

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

    public void recibirDatos(){
        new Thread(new Runnable() {
            public void run() {
                while (clienteRunning){
                    try {
                        String msj = dataInputStream.readUTF();
                        System.out.println(msj);
                    } catch (IOException e) {
                        stop();
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    public void enviarDatos(){
        new Thread(new Runnable() {
            public void run() {
                while (clienteRunning){
                    try {
                        String msj = teclado.nextLine();
                        dataOutputStream.writeUTF(msj);
                    } catch (IOException e) {
                        stop();
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    //Metodo que cierra los flujos de datos y el Scanner de teclado.
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

        int puerto = 23233;
        String host = "localhost";

        SocketCliente socketCliente = new SocketCliente(puerto,host);

        try {

            socketCliente.start();


        } catch (Exception e){
            e.printStackTrace();
        }


    }
}
