/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */
package io.camunda.connector.http.graphql.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.http.graphql.model.GraphQLRequest;
import io.camunda.connector.http.graphql.model.GraphQLRequestWrapper;
import java.util.HashMap;
import java.util.Map;

public final class JsonSerializeHelper {
  public static GraphQLRequest serializeRequest(ObjectMapper objectMapper, String input)
      throws JsonProcessingException {
    GraphQLRequestWrapper graphQLRequestWrapper =
        objectMapper.readValue(input, GraphQLRequestWrapper.class);
    GraphQLRequest graphQLRequest = graphQLRequestWrapper.getGraphql();
    graphQLRequest.setAuthentication(graphQLRequestWrapper.getAuthentication());
    return graphQLRequest;
  }

  public static Map<String, Object> queryAndVariablesToMap(GraphQLRequest graphQLRequest) {
    final Map<String, Object> map = new HashMap<>();
    map.put("query", getEscapedQuery(graphQLRequest));
    if (graphQLRequest.getVariables() != null) {
      map.put("variables", graphQLRequest.getVariables());
    }
    return map;
  }

  public static String getEscapedQuery(GraphQLRequest graphQLRequest) {
    return graphQLRequest.getQuery().replace("\\n", "").replace("\\\"", "\"");
  }
}
