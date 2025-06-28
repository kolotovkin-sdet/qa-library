package com.qa.atlibs.model;

import com.google.protobuf.Message;
import io.grpc.Metadata;
import io.grpc.Status;
import lombok.Data;

import java.util.List;

@Data
public class GrpcResponse <Res extends Message> {
    private Class<Res> responseClazz;
    private Status status;
    private Status.Code statusCode;
    private String description;
    private Throwable cause;
    private Metadata responseMetadata;
    private Res responseBody;
    private List<Res> responseBodies;
}
