package com.example.demo;

import com.example.demo.grpc.ChatMessage;
import com.example.demo.grpc.ChatServiceGrpc;
import com.example.demo.grpc.CustomUser;
import com.example.demo.grpc.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.HashMap;
import java.util.logging.Logger;

@GrpcService
public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {
    private static final Logger logger = Logger.getLogger(ChatServiceImpl.class.getName());

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
