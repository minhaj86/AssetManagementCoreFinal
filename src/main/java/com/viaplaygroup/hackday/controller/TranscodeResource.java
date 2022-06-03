package com.viaplaygroup.hackday.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.viaplaygroup.hackday.dto.TranscodeDto;
import com.viaplaygroup.hackday.entity.MediaEntity;
import com.viaplaygroup.hackday.entity.TranscodeEntity;
import com.viaplaygroup.hackday.mapper.TranscodeMapper;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
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

@Path("api/transcode")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class TranscodeResource {


    private static final Logger LOGGER = Logger.getLogger(TranscodeResource.class.getName());

    @GET
    public List<TranscodeDto> getAll() {
        return TranscodeEntity.listAll(Sort.by("id")).stream().map(x-> {return TranscodeMapper.INSTANCE.map((TranscodeEntity)x);}).collect(Collectors.toList());
    }

    @GET
    @Path("{id}")
    public TranscodeDto get(Long id) {
        TranscodeEntity entity = TranscodeEntity.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Transcode with id of " + id + " does not exist.", 404);
        }
        System.out.println(entity);
        return TranscodeMapper.INSTANCE.map(entity);
    }

    @POST
    @Transactional
    public Response create(TranscodeEntity transcodeEntity) {
        if (transcodeEntity.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        transcodeEntity.persist();
        TranscodeDto transcodeDto = TranscodeMapper.INSTANCE.map(transcodeEntity);
        MediaEntity sourceMedia = MediaEntity.findById(transcodeEntity.sourceMedia.id);
        transcodeDto.sourceDirectory = sourceMedia.filePath;
        transcodeDto.sourceFileFullPath = sourceMedia.filePath + sourceMedia.fileName;
        publishEvent(transcodeDto);

        return Response.ok(transcodeEntity).status(201).build();
    }

//    @GET
//    @Path("/events")
//    @Produces(MediaType.SERVER_SENT_EVENTS)
//    public Multi<TranscodeEvent> stream() {
//        return transcodes;
//    }

    private void publishEvent(TranscodeDto transcodeDto) {

        String mqHost = System.getenv("mqhost");
        String transcodeJobQueue = System.getenv("transcodejobqueue");
        ConnectionFactory mqConnectionFactory = new ConnectionFactory();
        mqConnectionFactory.setHost(mqHost);
        mqConnectionFactory.setPort(5672);
        Connection mqConnection = null;
        try {
            mqConnection = mqConnectionFactory.newConnection();
            Channel channel = mqConnection.createChannel();
            channel.exchangeDeclare("exc", "direct", true);
            channel.queueBind(transcodeJobQueue, "exc", "black");
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writeValueAsString(transcodeDto);
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
    public TranscodeEntity update(Long id, TranscodeDto transcodeDto) {
//        if (transcodeDto.filePath == null) {
//            throw new WebApplicationException("Transcode Name was not set on request.", 422);
//        }

        TranscodeEntity entity = TranscodeEntity.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Transcode with id of " + id + " does not exist.", 404);
        }

//        entity.title = TranscodeDto.name;

        return entity;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(Long id) {
        TranscodeEntity entity = TranscodeEntity.findById(id);
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
