package org.crucial.executor.k8s;

import org.crucial.executor.IterativeRunnable;
import org.crucial.executor.ServerlessExecutorService;
import org.testng.annotations.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AdvancedTest {

    @Test
    public void testSubmit() throws ExecutionException, InterruptedException {

        final String ret = "test";
        int port = 8080;
        String serviceName = "my-executor";



        long start = System.currentTimeMillis();
        /*
             job-b
             cmd: grep data
        */
        ServerlessExecutorService esK8s1 = new KubernetesExecutorService();
        esK8s1.setLocal(false);
        Future<String> future1 = esK8s1.submitListener(serviceName, port, (Serializable  & Callable<String>) () -> {
            try {
                int serverPort = port;
                ServerSocket serverSocket = new ServerSocket(serverPort);
                System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");

                List<String> data = new ArrayList<>();
                String grep = "Honda";

                Socket clientSocket = serverSocket.accept();
                Scanner in = new Scanner(clientSocket.getInputStream());
                while (in.hasNextLine()) {
                    String input = in.nextLine();
                    boolean val = input.contains(grep);
                    if (val) data.add(input);
                    if (input.equalsIgnoreCase("EOF")) {
                        break;
                    }
                }
                System.out.println(data.toString());
            } catch(UnknownHostException ex) {
                ex.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
            return ret;
        });

        // Get IP and port
        Dictionary<String, String> serviceSpec = esK8s1.getServiceSpecs(serviceName);
        System.out.println(serviceSpec);

        /*
             job-a
             cmd: curl URL
        */
        ServerlessExecutorService esK8s2 = new KubernetesExecutorService();
        esK8s2.setLocal(false);
        Future<String> future2 = esK8s2.submit((Serializable & Callable<String>) () -> {
            String URL = "https://perso.telecom-paristech.fr/eagan/class/igr204/data/cars.csv";
            try {
                int serverPort = Integer.parseInt(serviceSpec.get("port"));
                String host = serviceSpec.get("IP");

                Socket socket = new Socket(host, serverPort);
                System.out.println("Just connected to " + socket.getRemoteSocketAddress());

                PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);

                URL url = new URL(URL);
                Scanner sc = new Scanner(url.openStream());
                while(sc.hasNextLine()) {
                    String line = sc.nextLine();
                    toServer.println(line);
                }
                toServer.println("EOF");
                toServer.println("Data successfully sent to server");
                toServer.close();
                socket.close();
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        });

        System.out.println(future2.get());
        System.out.println(future1.get());

        long end = System.currentTimeMillis();
        long elapsedTime = end - start;

        System.out.println("Elapsed time: " + elapsedTime/1000.0);

        esK8s2.deleteAllJobs();

    }
}
