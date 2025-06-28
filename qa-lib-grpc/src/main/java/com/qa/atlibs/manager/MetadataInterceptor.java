package com.qa.atlibs.manager;

import io.grpc.*;
import lombok.Getter;

@Getter
public class MetadataInterceptor implements ClientInterceptor {
    private Metadata metadata;
    private Status status;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                        metadata = headers;
                        super.onHeaders(headers);
                    }

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        MetadataInterceptor.this.status = status;
                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }

    public Metadata getMetadata() {
        if (metadata == null) {
            metadata = new Metadata();
        }
        return metadata;
    }
}
