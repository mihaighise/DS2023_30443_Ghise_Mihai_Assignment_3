package com.example.demo;

import com.example.demo.grpc.*;
import com.example.demo.grpc.GreeterGrpc;
import com.example.demo.grpc.ChatServiceGrpc;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class HelloWorldServer {
    private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
//                .addService(new GreeterImpl())
                .addService(new ChatAppImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    HelloWorldServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final HelloWorldServer server = new HelloWorldServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    static class ChatAppImpl extends ChatServiceGrpc.ChatServiceImplBase {
        private StreamObserver<ChatMessage> observer;

        private HashMap<String, StreamObserver<ChatMessage>> observers = new HashMap<>();


        @Override
        public void sendMsg(ChatMessage request, StreamObserver<Empty> responseObserver) {
            logger.info(request.getFrom() + ": " + request.getMsg());
            if(observers.get(request.getTo()) != null)
                observers.get(request.getTo()).onNext(request);
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public void receiveMsg(CustomUser request, StreamObserver<ChatMessage> responseObserver) {
//            observer = responseObserver;
            logger.info(request.getUsername());
            observers.put(request.getUsername(), responseObserver);
            responseObserver.onNext(ChatMessage.newBuilder().build());
        }
    }
}