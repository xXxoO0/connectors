/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.camunda.connector.http.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.http.base.auth.Authentication;
import io.camunda.connector.http.base.auth.BasicAuthentication;
import io.camunda.connector.http.base.auth.BearerAuthentication;
import io.camunda.connector.http.base.auth.NoAuthentication;
import io.camunda.connector.http.base.auth.OAuthAuthentication;
import io.camunda.connector.http.rest.model.HttpJsonRequest;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class HttpJsonFunctionSecretsTest extends BaseTest {
  private static final String SUCCESS_REPLACE_SECRETS_CASES_PATH =
      "src/test/resources/requests/success-cases-replace-secrets.json";

  private OutboundConnectorContext context;

  protected static Stream<String> successReplaceSecretsCases() throws IOException {
    return loadTestCasesFromResourceFile(SUCCESS_REPLACE_SECRETS_CASES_PATH);
  }

  @ParameterizedTest(name = "Should replace request secrets")
  @MethodSource("successReplaceSecretsCases")
  void replaceSecrets_shouldReplaceRequestSecrets(String input) {
    // Given request with secrets
    context = getContextBuilderWithSecrets().variables(input).build();
    HttpJsonRequest httpJsonRequest = context.bindVariables(HttpJsonRequest.class);
    // Then should replace secrets
    assertThat(httpJsonRequest.getUrl()).isIn(ActualValue.URL, ActualValue.URL_WITH_PATH);
    assertThat(httpJsonRequest.getMethod().name()).isEqualTo(ActualValue.METHOD);
    assertThat(httpJsonRequest.getConnectionTimeoutInSeconds())
        .isEqualTo(ActualValue.CONNECT_TIMEOUT);
  }

  @ParameterizedTest(name = "Should replace auth secrets")
  @MethodSource("successReplaceSecretsCases")
  void replaceSecrets_shouldReplaceAuthSecrets(String input) {
    // Given request with secrets
    context = getContextBuilderWithSecrets().variables(input).build();
    HttpJsonRequest httpJsonRequest = context.bindVariables(HttpJsonRequest.class);
    // Then should replace secrets
    Authentication authentication = httpJsonRequest.getAuthentication();
    if (authentication instanceof NoAuthentication) {
      // nothing check in this case
    } else if (authentication instanceof BearerAuthentication bearerAuth) {
      assertThat(bearerAuth.getToken()).isEqualTo(ActualValue.Authentication.TOKEN);
    } else if (authentication instanceof BasicAuthentication basicAuth) {
      assertThat(basicAuth.getPassword()).isEqualTo(ActualValue.Authentication.PASSWORD);
      assertThat(basicAuth.getUsername()).isEqualTo(ActualValue.Authentication.USERNAME);
    } else if (authentication instanceof OAuthAuthentication oAuthAuthentication) {
      assertThat(oAuthAuthentication.getOauthTokenEndpoint())
          .isEqualTo(ActualValue.Authentication.OAUTH_TOKEN_ENDPOINT);
      assertThat(oAuthAuthentication.getClientId()).isEqualTo(ActualValue.Authentication.CLIENT_ID);
      assertThat(oAuthAuthentication.getClientSecret())
          .isEqualTo(ActualValue.Authentication.CLIENT_SECRET);
      assertThat(oAuthAuthentication.getAudience()).isEqualTo(ActualValue.Authentication.AUDIENCE);
    } else {
      fail("unknown authentication type");
    }
  }

  @ParameterizedTest(name = "Should replace QueryParameters secrets")
  @MethodSource("successReplaceSecretsCases")
  void replaceSecrets_shouldReplaceQueryParametersSecrets(String input) {
    // Given request with secrets
    context = getContextBuilderWithSecrets().variables(input).build();
    HttpJsonRequest httpJsonRequest = context.bindVariables(HttpJsonRequest.class);
    // Then should replace secrets
    JsonNode queryParams = objectMapper.valueToTree(httpJsonRequest.getQueryParameters());

    assertThat(queryParams.get(JsonKeys.QUERY).asText())
        .isEqualTo(ActualValue.QueryParameters.QUEUE);
    assertThat(queryParams.get(JsonKeys.PRIORITY).asText())
        .isEqualTo(ActualValue.QueryParameters.PRIORITY);
  }

  @ParameterizedTest(name = "Should replace headers secrets")
  @MethodSource("successReplaceSecretsCases")
  void replaceSecrets_shouldReplaceHeadersSecrets(String input) {
    // Given request with secrets
    context = getContextBuilderWithSecrets().variables(input).build();
    HttpJsonRequest httpJsonRequest = context.bindVariables(HttpJsonRequest.class);
    // Then should replace secrets
    JsonNode headers = objectMapper.valueToTree(httpJsonRequest.getHeaders());

    assertThat(headers.get(JsonKeys.CLUSTER_ID).asText()).isEqualTo(ActualValue.Headers.CLUSTER_ID);
    assertThat(headers.get(JsonKeys.USER_AGENT).asText()).isEqualTo(ActualValue.Headers.USER_AGENT);
  }

  @ParameterizedTest(name = "Should replace body secrets")
  @MethodSource("successReplaceSecretsCases")
  void replaceSecrets_shouldReplaceBodySecrets(String input) {
    // Given request with secrets
    context = getContextBuilderWithSecrets().variables(input).build();
    HttpJsonRequest httpJsonRequest = context.bindVariables(HttpJsonRequest.class);
    // Then should replace secrets
    JsonNode body = objectMapper.valueToTree(httpJsonRequest.getBody());
    JsonNode customer = body.get(JsonKeys.CUSTOMER);

    assertThat(customer.get(JsonKeys.ID).asText()).isEqualTo(ActualValue.Body.CUSTOMER_ID_REAL);
    assertThat(customer.get(JsonKeys.NAME).asText()).isEqualTo(ActualValue.Body.CUSTOMER_NAME_REAL);
    assertThat(customer.get(JsonKeys.EMAIL).asText())
        .isEqualTo(ActualValue.Body.CUSTOMER_EMAIL_REAL);

    assertThat(body.get(JsonKeys.TEXT).asText()).isEqualTo(ActualValue.Body.TEXT);
  }
}
