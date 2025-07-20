package com.qa.atlibs.grpc.steps;

import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.qa.atlibs.grpc.exception.GrpcTestException;
import com.qa.atlibs.grpc.manager.GrpcManager;
import com.qa.atlibs.core.manager.SessionManager;
import com.qa.atlibs.core.manager.TestDataManager;
import com.qa.atlibs.grpc.model.GrpcRequest;
import com.qa.atlibs.grpc.model.GrpcResponse;
import com.qa.atlibs.grpc.util.GrpcQaUtil;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings({"ResultOfMethodCallIgnored"})
@Accessors(fluent = true)
public class GrpcSteps<Req extends Message, Res extends Message> {

    private final GrpcRequest<Req> grpcRequest = new GrpcRequest<>();
    private final GrpcResponse<Res> grpcResponse = new GrpcResponse<>();

    public static <Req extends Message, Res extends Message> GrpcSteps<Req, Res> prepareRequest(Class<Req> clazz) {
        GrpcSteps<Req, Res> grpcSteps = new GrpcSteps<>();
        grpcSteps.grpcRequest.setRequestClazz(clazz);
        return grpcSteps;
    }

    public GrpcSteps<Req, Res> metadataForRequestAre(Map<String, String> metadataHeaders) {
        for (Map.Entry<String, String> entry : metadataHeaders.entrySet()) {
            grpcRequest.getRequestMetadata().put(Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER),
                    SessionManager.processTestData(entry.getValue()));

        }
        MetadataUtils.newAttachHeadersInterceptor(grpcRequest.getRequestMetadata());
        return this;
    }

    @SneakyThrows
    public GrpcSteps<Req, Res> jsonBodyForRequestIs(String requestBodyPath) {
        grpcRequest.setRequestBody(
                GrpcQaUtil.toProto(
                        SessionManager.processTestData(
                                TestDataManager.getTestFileData(requestBodyPath)),
                        grpcRequest.getRequestClazz()));
        return this;
    }

    @SneakyThrows
    public GrpcSteps<Req, Res> jsonStringForRequestIs(String requestBody) {
        grpcRequest.setRequestBody(GrpcQaUtil.toProto(SessionManager.processTestData(requestBody), grpcRequest.getRequestClazz()));
        return this;
    }

    public GrpcSteps<Req, Res> jsonBodiesForRequestAre(List<String> requestBodyPaths) {
        var processedBodies = requestBodyPaths.stream()
                .map(body -> {
                    try {
                        return GrpcQaUtil.toProto(
                                SessionManager.processTestData(
                                        TestDataManager.getTestFileData(body)),
                                grpcRequest.getRequestClazz());
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        grpcRequest.setRequestBodies(processedBodies);
        return this;
    }

    public GrpcSteps<Req, Res> jsonStringsForRequestAre(List<String> requestBodies) {
        var processedBodies = requestBodies.stream()
                .map(body -> {
                    try {
                        return GrpcQaUtil.toProto(SessionManager.processTestData(body), grpcRequest.getRequestClazz());
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        grpcRequest.setRequestBodies(processedBodies);
        return this;
    }

    public GrpcSteps<Req, Res> sendGrpcUnaryRequest(Function<Req, Res> call) {
        try {
            if (grpcRequest.getRequestBody() != null || !grpcRequest.getRequestClazz().equals(Empty.class)) {
                grpcResponse.setResponseBody(call.apply((Req) grpcRequest.getRequestBody()));
            } else {
                grpcResponse.setResponseBody(call.apply(null));
            }
            grpcResponse.setStatus(GrpcManager.getMetadataInterceptor().getStatus());
            grpcResponse.setStatusCode(GrpcManager.getMetadataInterceptor().getStatus().getCode());
            grpcResponse.setResponseMetadata(GrpcManager.getMetadataInterceptor().getMetadata());
        } catch (StatusRuntimeException e) {
            grpcResponse.setDescription(e.getStatus().getDescription());
            grpcResponse.setCause(e.getStatus().getCause());
        }
        return this;
    }

    public GrpcSteps<Req, Res> sendGrpcServiceStreamingRequest(Function<Req, Iterator<Res>> call) {
        try {
            Iterator<Res> iterator;
            if (grpcRequest.getRequestBody() != null || !grpcRequest.getRequestClazz().equals(Empty.class))
                iterator = call.apply(grpcRequest.getRequestBody());
            else
                iterator = call.apply(null);

            List<Res> responses = new ArrayList<>();
            iterator.forEachRemaining(responses::add);

            grpcResponse.setStatus(GrpcManager.getMetadataInterceptor().getStatus());
            grpcResponse.setStatusCode(GrpcManager.getMetadataInterceptor().getStatus().getCode());
            grpcResponse.setResponseMetadata(GrpcManager.getMetadataInterceptor().getMetadata());
            grpcResponse.setResponseBodies(responses);
        } catch (StatusRuntimeException e) {
            grpcResponse.setDescription(e.getStatus().getDescription());
            grpcResponse.setCause(e.getStatus().getCause());
        }
        return this;
    }

    public GrpcSteps<Req, Res> sendGrpcClientStreamingRequest(
            Function<StreamObserver<Res>, StreamObserver<Req>> call) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Res> respRef = new AtomicReference<>();

            StreamObserver<Res> responseObserver = new StreamObserver<>() {
                @Override public void onNext(Res r) { respRef.set(r); }
                @Override public void onError(Throwable t) {
                    if (t instanceof StatusRuntimeException sre) {
                        grpcResponse.setDescription(sre.getStatus().getDescription());
                        grpcResponse.setCause(sre.getStatus().getCause());
                    }
                    latch.countDown();
                }
                @Override public void onCompleted() {
                    grpcResponse.setResponseBody(respRef.get());
                    grpcResponse.setStatus(GrpcManager.getMetadataInterceptor().getStatus());
                    grpcResponse.setStatusCode(GrpcManager.getMetadataInterceptor().getStatus().getCode());
                    grpcResponse.setResponseMetadata(GrpcManager.getMetadataInterceptor().getMetadata());
                    latch.countDown();
                }
            };

            StreamObserver<Req> requestObserver = call.apply(responseObserver);

            var bodies = grpcRequest.getRequestBodies();
            if (bodies != null) bodies.forEach(requestObserver::onNext);
            requestObserver.onCompleted();

            latch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return this;
    }

    public GrpcSteps<Req, Res> sendGrpcBidirectionalStreamingRequest(
            Function<StreamObserver<Res>, StreamObserver<Req>> call) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            List<Res> responses = new ArrayList<>();

            StreamObserver<Res> responseObserver = new StreamObserver<>() {
                @Override public void onNext(Res r) { responses.add(r); }
                @Override public void onError(Throwable t) {
                    if (t instanceof StatusRuntimeException sre) {
                        grpcResponse.setDescription(sre.getStatus().getDescription());
                        grpcResponse.setCause(sre.getStatus().getCause());
                    }
                    latch.countDown();
                }
                @Override public void onCompleted() {
                    grpcResponse.setResponseBodies(responses);
                    grpcResponse.setStatus(GrpcManager.getMetadataInterceptor().getStatus());
                    grpcResponse.setStatusCode(GrpcManager.getMetadataInterceptor().getStatus().getCode());
                    grpcResponse.setResponseMetadata(GrpcManager.getMetadataInterceptor().getMetadata());
                    latch.countDown();
                }
            };

            StreamObserver<Req> requestObserver = call.apply(responseObserver);

            var bodies = grpcRequest.getRequestBodies();
            if (bodies != null) {
                bodies.forEach(requestObserver::onNext);
            }
            requestObserver.onCompleted();

            latch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return this;
    }

    public GrpcSteps<Req, Res> failedResponseStatusIs(Status.Code statusCode) {
        assertThat(grpcResponse.getStatusCode()).isEqualTo(statusCode);
        return this;
    }

    public GrpcSteps<Req, Res> failedResponseStatusIs(Status status) {
        assertThat(grpcResponse.getStatus().getCode().toStatus()).isEqualTo(status);
        return this;
    }

    public GrpcSteps<Req, Res> failedResponseStatusIs(String description) {
        assertThat(grpcResponse.getDescription()).isEqualTo(SessionManager.processTestData(description));
        return this;
    }

    public GrpcSteps<Req, Res> failedResponseStatusIs(Throwable cause) {
        assertThat(grpcResponse.getCause()).isEqualTo(cause);
        return this;
    }

    public GrpcSteps<Req, Res> jsonResponseIs(String responseBodyPath) {
        assertThat(grpcResponse.getStatus().getCode()).isEqualTo(Status.Code.OK);

        try {
            String expectedResult = SessionManager.processTestData(TestDataManager.getTestFileData(responseBodyPath));
            String actualResult = GrpcQaUtil.toJson(grpcResponse.getResponseBody());

            assertThatJson(actualResult).isEqualTo(expectedResult);
        } catch (IOException e) {
            throw new GrpcTestException(e);
        }
        return this;
    }

    public GrpcSteps<Req, Res> responseMetadataIs(Map<String, String> metadataHeaders) {

        Map<String, String> actualMetadata
                = GrpcQaUtil.metadataToMap(Objects.requireNonNull(grpcResponse.getResponseMetadata()));

        assertThat(actualMetadata.keySet()).containsExactlyElementsOf(metadataHeaders.keySet());

        metadataHeaders.forEach((key, expectedValue) -> {
            String processedExpectedValue = SessionManager.processTestData(expectedValue);
            String actualValue = actualMetadata.get(key);
            assertThat(actualValue).isEqualTo(processedExpectedValue);
        });
        return this;
    }

    public GrpcSteps<Req, Res> streamingResponseQtyIs(int qty) {
        assertThat(grpcResponse.getStatus().getCode()).isEqualTo(Status.Code.OK);
        assertThat(grpcResponse.getResponseBodies()).hasSize(qty);
        return this;
    }

    public GrpcSteps<Req, Res> streamingResponseIsEmpty() {
        assertThat(grpcResponse.getStatus().getCode()).isEqualTo(Status.Code.OK);
        assertThat(grpcResponse.getResponseBodies()).isEmpty();
        return this;
    }

    public GrpcSteps<Req, Res> jsonResponsesAre(List<String> responseBodyPaths) {
        assertThat(grpcResponse.getStatus().getCode()).isEqualTo(Status.Code.OK);
        assertThat(grpcResponse.getResponseBodies()).hasSize(responseBodyPaths.size());

        var processedExpectedResponses = responseBodyPaths.stream()
                .map(body -> SessionManager.processTestData(TestDataManager.getTestFileData(body)))
                .sorted()
                .toList();

        var processedActualResponses = grpcResponse.getResponseBodies().stream()
                .map(body -> {
                    try {
                        return SessionManager.processTestData(GrpcQaUtil.toJson(body));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .sorted()
                .toList();

        for (int i = 0; i < processedActualResponses.size() && i < processedExpectedResponses.size(); i++) {
            assertThatJson(processedActualResponses.get(i)).isEqualTo(processedExpectedResponses.get(i));
        }
        return this;
    }
}
