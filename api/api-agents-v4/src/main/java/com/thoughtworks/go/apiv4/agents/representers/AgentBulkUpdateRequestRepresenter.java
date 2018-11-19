/*
 * Copyright 2018 ThoughtWorks, Inc.
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

package com.thoughtworks.go.apiv4.agents.representers;

import com.thoughtworks.go.api.representers.JsonReader;
import com.thoughtworks.go.api.util.GsonTransformer;
import com.thoughtworks.go.api.util.HaltApiResponses;
import com.thoughtworks.go.apiv4.agents.model.AgentBulkUpdateRequest;
import com.thoughtworks.go.apiv4.agents.model.AgentBulkUpdateRequest.Operation;

import java.util.List;

public class AgentBulkUpdateRequestRepresenter extends UpdateRequestRepresenter {
    public static AgentBulkUpdateRequest fromJSON(String requestBody) {
        final JsonReader reader = GsonTransformer.getInstance().jsonReaderFrom(requestBody);
        final List<String> uuids = extractToList(reader.optJsonArray("uuids"));
        final String agentConfigState = reader.getString("agent_config_state");

        if (uuids.isEmpty()) {
            HaltApiResponses.haltBecauseOfReason("Must specify agent 'uuids' for bulk update.");
        }

        final AgentBulkUpdateRequest.Operations operations = toOperationsFromJSON(reader.readJsonObject("operations"));

        return new AgentBulkUpdateRequest(uuids, operations, toTriState(agentConfigState));
    }

    public static AgentBulkUpdateRequest.Operations toOperationsFromJSON(JsonReader reader) {
        return new AgentBulkUpdateRequest.Operations(
                toOperationFromJSON(reader.readJsonObject("environments")),
                toOperationFromJSON(reader.readJsonObject("resources"))
        );
    }

    public static Operation toOperationFromJSON(JsonReader reader) {
        return new Operation(extractToList(reader.optJsonArray("add")), extractToList(reader.optJsonArray("remove")));
    }
}
