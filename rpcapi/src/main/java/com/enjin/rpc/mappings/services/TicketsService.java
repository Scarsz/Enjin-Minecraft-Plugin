package com.enjin.rpc.mappings.services;

import com.enjin.core.services.Service;
import com.enjin.rpc.mappings.deserializers.QuestionDeserializer;
import com.enjin.rpc.mappings.deserializers.TicketDeserializer;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.general.RPCResult;
import com.enjin.rpc.mappings.mappings.general.RPCSuccess;
import com.enjin.rpc.mappings.mappings.general.ResultType;
import com.enjin.rpc.mappings.mappings.tickets.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;
import com.enjin.rpc.mappings.deserializers.ExtraQuestionDeserializer;
import com.enjin.rpc.EnjinRPC;

import java.util.*;

public class TicketsService implements Service {
    public static final Gson GSON_TICKET = new GsonBuilder()
            .registerTypeAdapter(Ticket.class, new TicketDeserializer())
            .create();
    public static final Gson GSON_EXTRA_QUESTION = new GsonBuilder()
            .registerTypeAdapter(ExtraQuestion.class, new ExtraQuestionDeserializer())
            .create();
    public static final Gson GSON_QUESTION = new GsonBuilder()
            .registerTypeAdapter(Question.class, new QuestionDeserializer())
            .create();

    public List<Ticket> getPlayerTickets(final String authkey, final String player) {
        String method = "Tickets.getPlayerTickets";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("minecraft_player", player);
        }};
        int id = 1;

        try {
            JSONRPC2Session session = EnjinRPC.getSession();
            JSONRPC2Request request = new JSONRPC2Request(method, parameters, id);
            JSONRPC2Response response = session.send(request);
            RPCData<List<Ticket>> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<ArrayList<Ticket>>>(){}.getType());
            return data.getResult();
        } catch (JSONRPC2SessionException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public List<Ticket> getTickets(final String authkey, final int preset) {
        String method = "Tickets.getTickets";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("preset_id", preset);
        }};
        int id = 1;

        try {
            JSONRPC2Session session = EnjinRPC.getSession();
            JSONRPC2Request request = new JSONRPC2Request(method, parameters, id);
            JSONRPC2Response response = session.send(request);
            RPCData<List<Ticket>> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<ArrayList<Ticket>>>(){}.getType());
            return data.getResult();
        } catch (JSONRPC2SessionException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public Map<Integer, Module> getModules(final String authkey) {
        String method = "Tickets.getModules";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
        }};
        int id = 1;

        try {
            JSONRPC2Session session = EnjinRPC.getSession();
            JSONRPC2Request request = new JSONRPC2Request(method, parameters, id);
            JSONRPC2Response response = session.send(request);
            RPCData<Map<Integer, Module>> data = GSON_QUESTION.fromJson(response.toJSONString(), new TypeToken<RPCData<HashMap<Integer, Module>>>(){}.getType());
            return data.getResult();
        } catch (JSONRPC2SessionException e) {
            e.printStackTrace();
        }

        return Collections.emptyMap();
    }

    public boolean setStatus(final String authkey, final int preset, final String code, final TicketStatus status) {
        String method = "Tickets.setStatus";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("preset_id", preset);
            put("ticket_code", code);
            put("status", status.name());
        }};
        int id = 1;

        try {
            JSONRPC2Session session = EnjinRPC.getSession();
            JSONRPC2Request request = new JSONRPC2Request(method, parameters, id);
            JSONRPC2Response response = session.send(request);
            RPCData<Boolean> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<Boolean>>(){}.getType());
            return data.getResult();
        } catch (JSONRPC2SessionException e) {
            e.printStackTrace();
        }

        return false;
    }

    public PlayerAccess getPlayerAccess(final String authkey, final int preset, final String player) {
        String method = "Tickets.getPlayerAccess";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("preset_id", preset);
            put("minecraft_player", player);
        }};
        int id = 1;

        try {
            JSONRPC2Session session = EnjinRPC.getSession();
            JSONRPC2Request request = new JSONRPC2Request(method, parameters, id);
            JSONRPC2Response response = session.send(request);
            RPCData<PlayerAccess> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<PlayerAccess>>() {
            }.getType());
            return data.getResult();
        } catch (JSONRPC2SessionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Reply> getReplies(final String authkey, final int preset, final String code, final String player) {
        String method = "Tickets.getReplies";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("preset_id", preset);
            put("ticket_code", code);
            put("minecraft_player", player);
        }};
        int id = 1;

        try {
            JSONRPC2Session session = EnjinRPC.getSession();
            JSONRPC2Request request = new JSONRPC2Request(method, parameters, id);
            JSONRPC2Response response = session.send(request);
            RPCData<List<Reply>> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<List<Reply>>>() {
            }.getType());
            return data.getResult();
        } catch (JSONRPC2SessionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public RPCResult createTicket(final String authkey, final int preset, final String subject, final String description, final String player, final List<ExtraQuestion> extraQuestions) {
        String method = "Tickets.createTicket";
        final Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("preset_id", preset);
            put("subject", subject);
            put("description", description);
            put("minecraft_player", player);
            put("extra_questions", extraQuestions.toArray());
        }};
        int id = 1;

        try {
            JSONRPC2Session session = EnjinRPC.getSession();
            JSONRPC2Request request = new JSONRPC2Request(method, parameters, id);
            JSONRPC2Response response = session.send(request);

            JsonElement resp = EnjinRPC.gson.toJsonTree(response.getResult());
            if (resp.isJsonPrimitive()) {
                JsonPrimitive primitive = resp.getAsJsonPrimitive();
                if (primitive.isBoolean()) {
                    if (primitive.getAsBoolean()) {
                        return new RPCResult(ResultType.SUCCESS, "Your ticket was submitted successfully!");
                    } else {
                        return new RPCResult(ResultType.SUCCESS, "Your ticket was could not be submitted!");
                    }
                }
            } else if (resp.isJsonObject()) {
                JsonObject result = resp.getAsJsonObject();
                if (result.get("reason").getAsString().equals("ticket_delay")) {
                    String time = result.get("time_left").getAsString();
                    return new RPCResult(ResultType.TICKET_DELAY, "You can submit another ticket " + time);
                } else if (result.get("reason").getAsString().equals("invalid_params")) {
                    return new RPCResult(ResultType.INVALID_PARAMS, "Invalid parameters");
                }
            }

            System.out.println(resp.toString());
            return new RPCResult(ResultType.UNKNOWN_ERROR, "The error returned was not recognized.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new RPCResult(ResultType.UNKNOWN_ERROR, "There was an error while parsing a response from Enjin.");
    }

    public boolean sendReply(final String authkey, final int preset, final String code, final String text, final String mode, final TicketStatus status, final String player) {
        String method = "Tickets.sendReply";
        final Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("preset_id", preset);
            put("ticket_code", code);
            put("text", text);
            put("mode", mode);
            put("status", status.name());
            put("minecraft_player", player);
        }};
        int id = 1;

        try {
            JSONRPC2Session session = EnjinRPC.getSession();
            JSONRPC2Request request = new JSONRPC2Request(method, parameters, id);
            JSONRPC2Response response = session.send(request);
            RPCData<RPCSuccess> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<RPCSuccess>>(){}.getType());
            return data.getResult().isSuccess();
        } catch (JSONRPC2SessionException e) {
            e.printStackTrace();
        }

        return false;
    }
}
