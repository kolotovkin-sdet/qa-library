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
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@SuppressWarnings({"rawtypes", "unchecked", "ResultOfMethodCallIgnored"})
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GrpcSteps<Req extends Message, Res extends Message> {

    private final GrpcRequest grpcRequest = new GrpcRequest();
    private final GrpcResponse grpcResponse = new GrpcResponse();

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

    public GrpcSteps<Req, Res> jsonBodyForRequestIs(String requestBodyPath) throws InvalidProtocolBufferException {
        grpcRequest.setRequestBody(
                GrpcQaUtil.toProto(
                        SessionManager.processTestData(
                                TestDataManager.getTestFileData(requestBodyPath)),
                        grpcRequest.getRequestClazz()));
        return this;
    }

    public GrpcSteps<Req, Res> jsonStringForRequestIs(String requestBody) throws InvalidProtocolBufferException {
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

    public GrpcSteps<Req, Res> sendGrpcServiceStreamingRequest(Function<Req, List<Res>> call) {
        try {
            if (grpcRequest.getRequestBody() != null || !grpcRequest.getRequestClazz().equals(Empty.class)) {
                grpcResponse.setResponseBodies(call.apply((Req) grpcRequest.getRequestBody()));
            } else {
                grpcResponse.setResponseBodies(call.apply(null));
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

    public GrpcSteps<Req, Res> sendGrpcClientStreamingRequest(Function<List<Req>, Res> call) {
        try {
            if (grpcRequest.getRequestBodies() != null || !grpcRequest.getRequestClazz().equals(Empty.class)) {
                grpcResponse.setResponseBody(call.apply(grpcRequest.getRequestBodies()));
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

    public GrpcSteps<Req, Res> sendGrpcBidirectionalStreamingRequest(Function<List<Req>, List<Res>> call) {
        try {
            if (grpcRequest.getRequestBodies() != null || !grpcRequest.getRequestClazz().equals(Empty.class)) {
                grpcResponse.setResponseBodies(call.apply(grpcRequest.getRequestBodies()));
            } else {
                grpcResponse.setResponseBodies(call.apply(null));
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
        assertThat(grpcResponse.getStatus()).isEqualTo(Status.OK);

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
        assertThat(grpcResponse.getResponseBodies()).hasSize(qty);
        return this;
    }

    public GrpcSteps<Req, Res> streamingResponseIsEmpty() {
        assertThat(grpcResponse.getResponseBodies()).isEmpty();
        return this;
    }

    public GrpcSteps<Req, Res> jsonResponsesAre(List<String> responseBodyPaths) {
        assertThat(grpcResponse.getResponseBodies()).hasSize(responseBodyPaths.size());

        var processedExpectedResponses = responseBodyPaths.stream()
                .map(body -> SessionManager.processTestData(TestDataManager.getTestFileData(body)))
                .sorted()
                .toList();

        var processedActualResponses = grpcResponse.getResponseBodies().stream()
                .map(body -> {
                    try {
                        return SessionManager.processTestData(TestDataManager.getTestFileData(GrpcQaUtil.toJson((Res) body)));
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
