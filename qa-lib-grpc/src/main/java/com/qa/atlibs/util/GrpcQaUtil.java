package com.qa.atlibs.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.qa.atlibs.exception.GrpcTestException;
import io.grpc.Metadata;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class GrpcQaUtil {

    @SuppressWarnings("unchecked")
    public static <T extends Message> T toProto(String json, Class<T> clazz) throws RuntimeException, InvalidProtocolBufferException {
        Message.Builder builder = null;
        try {
            builder = (Message.Builder) clazz.getMethod("newBuilder").invoke(null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | NoSuchMethodException | SecurityException e) {
            throw new GrpcTestException(e);
        }
        JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        return (T) builder.build();
    }

    public static <T extends Message> String toJson(T obj) throws IOException {
        return JsonFormat.printer()
                .includingDefaultValueFields()
                .preservingProtoFieldNames()
                .print(obj);
    }

    public static Map<String, String> metadataToMap(Metadata metadata) {
        Map<String, String> metadataMap = new HashMap<>();
        metadata.keys().forEach(key -> {
            metadataMap.put(key, metadata.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)));
        });
        return metadataMap;
    }

}
