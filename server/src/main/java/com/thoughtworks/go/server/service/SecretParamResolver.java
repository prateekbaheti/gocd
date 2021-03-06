/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.thoughtworks.go.server.service;

import com.thoughtworks.go.config.SecretConfig;
import com.thoughtworks.go.config.SecretParam;
import com.thoughtworks.go.config.SecretParams;
import com.thoughtworks.go.plugin.access.secrets.SecretsExtension;
import com.thoughtworks.go.plugin.domain.secrets.Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Component
public class SecretParamResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecretParamResolver.class);
    private SecretsExtension secretsExtension;
    private GoConfigService goConfigService;

    @Autowired
    public SecretParamResolver(SecretsExtension secretsExtension, GoConfigService goConfigService) {
        this.secretsExtension = secretsExtension;
        this.goConfigService = goConfigService;
    }

    public void resolve(SecretParams secretParams) {
        if (secretParams == null || secretParams.isEmpty()) {
            LOGGER.debug("No secret params to resolve.");
            return;
        }

        secretParams.groupBySecretConfigId().forEach(lookupAndUpdateSecretParamsValue());
    }

    private BiConsumer<String, SecretParams> lookupAndUpdateSecretParamsValue() {
        return (secretConfigId, secretParamsToResolve) -> {
            Map<String, List<SecretParam>> secretParamMap = secretParamsToResolve.stream().collect(groupingBy(SecretParam::getKey, Collectors.toList()));
            final SecretConfig secretConfig = goConfigService.cruiseConfig().getSecretConfigs().find(secretConfigId);

            LOGGER.debug("Resolving secret params '{}' using secret config '{}'", secretParamMap.keySet(), secretConfig.getId());
            List<Secret> resolvedSecrets = secretsExtension.lookupSecrets(secretConfig.getPluginId(), secretConfig, secretParamMap.keySet());
            LOGGER.debug("Resolved secret size '{}'", resolvedSecrets.size());


            LOGGER.debug("Updating secret params '{}' with values.", secretParamMap.keySet());
            resolvedSecrets.forEach(assignValue(secretParamMap));
            LOGGER.debug("Secret params '{}' updated with values.", secretParamMap.keySet());
        };
    }

    private Consumer<Secret> assignValue(Map<String, List<SecretParam>> secretParamMap) {
        return secret -> secretParamMap.get(secret.getKey()).forEach(secretParam -> secretParam.setValue(secret.getValue()));
    }
}
