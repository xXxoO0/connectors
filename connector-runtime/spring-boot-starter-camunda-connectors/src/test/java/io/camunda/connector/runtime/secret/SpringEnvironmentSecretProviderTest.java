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
package io.camunda.connector.runtime.secret;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.connector.runtime.app.TestConnectorRuntimeApplication;
import io.camunda.connector.runtime.core.secret.SecretProviderAggregator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {TestConnectorRuntimeApplication.class},
    properties = {"camunda.connector.secretprovider.discovery.enabled=false"})
public class SpringEnvironmentSecretProviderTest {

  @Autowired SecretProviderAggregator secretProviderAggregator;

  @Value("${test.secret}")
  String expectedSecretValue;

  @Test
  void springEnvironmentSecretProviderShouldBePresent() {
    // Spring environment based secret provider should look up values from properties
    assertThat(secretProviderAggregator.getSecretProviders().size()).isEqualTo(1);
    var actualSecretValue = secretProviderAggregator.getSecret("test.secret");
    assertThat(actualSecretValue).isNotNull();
    assertThat(actualSecretValue).isEqualTo(expectedSecretValue);
  }
}
