package com.viaplaygroup.hackday.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.viaplaygroup.hackday.dto.TransferDto;
import com.viaplaygroup.hackday.entity.MediaEntity;
import com.viaplaygroup.hackday.entity.TransferEntity;
import com.viaplaygroup.hackday.mapper.TransferMapper;
import io.quarkus.panache.common.Sort;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Path("api/transfer")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class TransferResource {


    private static final Logger LOGGER = Logger.getLogger(TransferResource.class.getName());

    @GET
    public List<TransferDto> getAll() {
        return TransferEntity.listAll(Sort.by("id")).stream().map(x-> {return TransferMapper.INSTANCE.map((TransferEntity)x);}).collect(Collectors.toList());
    }

    @GET
    @Path("{id}")
    public TransferDto get(Long id) {
        TransferEntity entity = TransferEntity.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Transfer with id of " + id + " does not exist.", 404);
        }
        System.out.println(entity);
        return TransferMapper.INSTANCE.map(entity);
    }

    @POST
    @Transactional
    public Response create(TransferEntity transferEntity) {
        if (transferEntity.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        transferEntity.persist();
        TransferDto transferDto = TransferMapper.INSTANCE.map(transferEntity);
        MediaEntity sourceMedia = MediaEntity.findById(transferEntity.sourceMedia.id);
        transferDto.sourceDirectory = sourceMedia.filePath;
        transferDto.sourceFileFullPath = sourceMedia.filePath + sourceMedia.fileName;
        publishEvent(transferDto);

        return Response.ok(transferEntity).status(201).build();
    }

//    @GET
//    @Path("/events")
//    @Produces(MediaType.SERVER_SENT_EVENTS)
//    public Multi<TransferEvent> stream() {
//        return Transfers;
//    }

    private void publishEvent(TransferDto transferDto) {

        String mqHost = System.getenv("mqhost");
        String transferjobqueue = "transfer";
        ConnectionFactory mqConnectionFactory = new ConnectionFactory();
        mqConnectionFactory.setHost(mqHost);
        mqConnectionFactory.setPort(5672);
        Connection mqConnection = null;
        try {
            mqConnection = mqConnectionFactory.newConnection();
            Channel channel = mqConnection.createChannel();
            channel.exchangeDeclare("exc", "direct", true);
            channel.queueBind(transferjobqueue, "exc", "black");
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writeValueAsString(transferDto);
            System.out.println(jsonStr);
            byte[] messageBodyBytes = jsonStr.getBytes();

            AMQP.BasicProperties amqproperties = new AMQP.BasicProperties();
            channel.basicPublish("exc", "black", amqproperties, messageBodyBytes);
            channel.close();
            mqConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (TimeoutException e) {
//            throw new RuntimeException(e);
        }

    }
    @PUT
    @Path("{id}")
    @Transactional
    public TransferEntity update(Long id, TransferDto transferDto) {
//        if (transferDto.filePath == null) {
//            throw new WebApplicationException("Transfer Name was not set on request.", 422);
//        }

        TransferEntity entity = TransferEntity.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Transfer with id of " + id + " does not exist.", 404);
        }

//        entity.title = transferDto.name;

        return entity;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(Long id) {
        TransferEntity entity = TransferEntity.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return Response.status(204).build();
    }

    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Inject
        ObjectMapper objectMapper;

        @Override
        public Response toResponse(Exception exception) {
            LOGGER.error("Failed to handle request", exception);

            int code = 500;
            if (exception instanceof WebApplicationException) {
                code = ((WebApplicationException) exception).getResponse().getStatus();
            }

            ObjectNode exceptionJson = objectMapper.createObjectNode();
            exceptionJson.put("exceptionType", exception.getClass().getName());
            exceptionJson.put("code", code);

            if (exception.getMessage() != null) {
                exceptionJson.put("error", exception.getMessage());
            }

            return Response.status(code)
                    .entity(exceptionJson)
                    .build();
        }

    }
}
