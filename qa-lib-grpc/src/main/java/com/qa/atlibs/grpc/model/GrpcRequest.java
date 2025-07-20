package com.qa.atlibs.grpc.model;

import com.google.protobuf.Message;
import io.grpc.Channel;
import io.grpc.Metadata;
import lombok.Data;

import java.util.List;

@Data
public class GrpcRequest <Req extends Message> {
    private Channel channel;
    private Metadata requestMetadata;
    private Class<Req> requestClazz;
    private Req requestBody;
    private List<Req> requestBodies;

    public Metadata getRequestMetadata() {
        if (requestMetadata == null) {
             return new Metadata();
        }
        return requestMetadata;
    }
}

