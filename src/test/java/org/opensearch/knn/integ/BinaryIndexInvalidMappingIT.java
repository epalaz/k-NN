/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.knn.integ;

import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.opensearch.knn.KNNJsonIndexMappingsBuilder;
import org.opensearch.knn.KNNRestTestCase;
import org.opensearch.knn.index.VectorDataType;
import org.opensearch.knn.index.engine.KNNEngine;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static com.carrotsearch.randomizedtesting.RandomizedTest.$;
import static com.carrotsearch.randomizedtesting.RandomizedTest.$$;
import static org.opensearch.knn.common.KNNConstants.ENCODER_SQ;
import static org.opensearch.knn.common.KNNConstants.METHOD_HNSW;

/**
 * This class contains integration tests for binary index with invalid mapping
 */
@Log4j2
@AllArgsConstructor
public class BinaryIndexInvalidMappingIT extends KNNRestTestCase {
    private String description;
    private String indexMapping;
    private String expectedExceptionMessage;

    @ParametersFactory(argumentFormatting = "description:%1$s; indexMapping:%2$s, expectedExceptionMessage:%3$s")
    public static Collection<Object[]> parameters() throws IOException {
        return Arrays.asList(
            $$(
                $(
                    "Creation of binary index with nmslib engine should fail",
                    createKnnHnswBinaryIndexMapping(KNNEngine.NMSLIB, FIELD_NAME, 16, null),
                    "Validation Failed"
                ),
                $(
                    "Creation of binary index with encoder should fail",
                    createKnnHnswBinaryIndexMapping(KNNEngine.FAISS, FIELD_NAME, 16, ENCODER_SQ),
                    "Validation Failed"
                )
            )
        );
    }

    private static String createKnnHnswBinaryIndexMapping(
        final KNNEngine knnEngine,
        final String fieldName,
        final int dimension,
        final String encoderName
    ) throws IOException {
        KNNJsonIndexMappingsBuilder.Method.Parameters.Encoder encoder;
        KNNJsonIndexMappingsBuilder.Method.Parameters parameters = null;
        if (encoderName != null) {
            encoder = KNNJsonIndexMappingsBuilder.Method.Parameters.Encoder.builder().name(encoderName).build();
            parameters = KNNJsonIndexMappingsBuilder.Method.Parameters.builder().encoder(encoder).build();
        }

        KNNJsonIndexMappingsBuilder.Method method = KNNJsonIndexMappingsBuilder.Method.builder()
            .methodName(METHOD_HNSW)
            .engine(knnEngine.getName())
            .parameters(parameters)
            .build();

        return KNNJsonIndexMappingsBuilder.builder()
            .fieldName(fieldName)
            .dimension(dimension)
            .vectorDataType(VectorDataType.BINARY.getValue())
            .method(method)
            .build()
            .getIndexMapping();
    }
}
