package com.viaplaygroup.hackday.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.viaplaygroup.hackday.dto.MediaDto;
import com.viaplaygroup.hackday.entity.MediaEntity;
import com.viaplaygroup.hackday.mapper.MediaMapper;
import io.quarkus.panache.common.Sort;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@Path("api/media")
@ApplicationScoped
@Produces("application/json")
//@Consumes("application/json")
public class MediaResource {

    private static final Logger LOGGER = Logger.getLogger(MediaResource.class.getName());

    @GET
    public List<MediaDto> getAll() {
        return MediaEntity.listAll(Sort.by("id")).stream().map(x-> {return MediaMapper.INSTANCE.map((MediaEntity)x);}).collect(Collectors.toList());
    }

    @GET
    @Path("{id}")
    public MediaDto get(Long id) {
        MediaEntity entity = MediaEntity.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Media with id of " + id + " does not exist.", 404);
        }
        System.out.println(entity);
        return MediaMapper.INSTANCE.map(entity);
    }

    @POST
    @Transactional
    public Response create(MediaEntity Media) {
        if (Media.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        Media.persist();
        return Response.ok(Media).status(201).build();
    }

    @POST
    @Path("{id}/upload")
    @Consumes("*/*")
//    @Transactional
    public Response uploadFile(Long id, @QueryParam("filename") String fileName, InputStream attachmentInputStream) throws IOException {
        MediaDto media = get(id);
        String mediaFullPath = media.filePath + media.fileName;
        createDirectory(media.filePath);
        File destFile = new File(mediaFullPath);

        saveFile(attachmentInputStream, destFile);
        System.out.println("=========FILE UPLOAD========");
        return Response.ok().build();
    }

    private boolean createDirectory(String path) {
        File dir = new File(path);
        if (dir.isDirectory()) {
            System.out.println("========= Directory exists");
            return true;
        }
        System.out.println("========= Directory does not exist");
        return dir.mkdir();
    }

    private void saveFile(InputStream attachmentInputStream, File dest) throws IOException {
        InputStream is = attachmentInputStream;
        OutputStream os = null;
        try {
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            os.close();
        }
    }


    @PUT
    @Path("{id}")
    @Transactional
    public MediaEntity update(Long id, MediaDto MediaDto) {
        if (MediaDto.filePath == null) {
            throw new WebApplicationException("Media Name was not set on request.", 422);
        }

        MediaEntity entity = MediaEntity.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Media with id of " + id + " does not exist.", 404);
        }

//        entity.title = MediaDto.name;

        return entity;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(Long id) {
        MediaEntity entity = MediaEntity.findById(id);
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
