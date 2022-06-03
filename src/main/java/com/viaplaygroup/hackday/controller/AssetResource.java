package com.viaplaygroup.hackday.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.viaplaygroup.hackday.dto.AssetDto;
import com.viaplaygroup.hackday.dto.MediaDto;
import com.viaplaygroup.hackday.entity.AssetEntity;
import com.viaplaygroup.hackday.mapper.AssetMapper;
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
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("api/assets")
@ApplicationScoped
@Produces("application/json")
//@Consumes("application/json")
public class AssetResource {

    private static final Logger LOGGER = Logger.getLogger(AssetResource.class.getName());

    @GET
    public List<AssetDto> getAll() {
        return AssetEntity.listAll(Sort.by("id")).stream().map(x-> {return AssetMapper.INSTANCE.map((AssetEntity)x);}).collect(Collectors.toList());
    }

    @GET
    @Path("/viewasset/{id}")
    @Produces(MediaType.TEXT_HTML)
    public String viewAsset(Long id) {
        AssetDto asset = get(id);
        Optional<MediaDto> mediaOptional = asset.media.stream().filter(x -> {
            if (x.fileName.endsWith(".mpd")) {
                return true;
            }
            return false;
        }).findAny();
        Optional<MediaDto> mediaMp4Optional = asset.media.stream().filter(x -> {
            if (x.fileName.endsWith(".mp4")) {
                return true;
            }
            return false;
        }).findAny();

        String fileParts[] =mediaMp4Optional.get().fileName.split("\\.(?=[^\\.]+$)");
        String fileNameBase = fileParts[0];


        String html = String.format("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<script src=\"http://cdn.dashjs.org/latest/dash.all.min.js\"></script>\n" +
                "<style>\n" +
                "    video {\n" +
                "       width: 100%%;\n" +
                "       height: 100%%;\n" +
                "\n" +
                "    }\n" +
                "    .top {\n" +
                "        width: 60%%;\n" +
                "        margin: auto;\n" +
                "       padding: 10px;\n" +
                "    }\n" +
                "</style>\n" +
                "        <script>\n" +
                "            function createTransfer() {\n" +
                "                // data to be sent to the POST request\n" +
                "                let _data = {\n" +
                "                    \"sourceMedia\":{\n" +
                "                        \"id\":%s\n" +
                "                    },\n" +
                "                    \"fileFullPath\":\"/Users/minhmdru/mediaroot/mp\",\n" +
                "                    \"status\":\"new\"\n" +
                "                }\n" +
                "\n" +
                "\n" +
                "                fetch('http://localhost:8080/api/transfer', {\n" +
                "                method: \"POST\",\n" +
                "                body: JSON.stringify(_data),\n" +
                "                headers: {\"Content-type\": \"application/json; charset=UTF-8\"}\n" +
                "                })\n" +
                "                .then(response => response.json()) \n" +
                "                .then(json => console.log(json))\n" +
                "                .catch(err => console.log(err));\n" +
                "\n" +
                "            }\n" +
                "            function createTranscode() {\n" +
                "                // data to be sent to the POST request\n" +
                "                let _data = {\n" +
                "                    \"sourceMedia\":{\n" +
                "                        \"id\":%s\n" +
                "                    },\n" +
                "                    \"fileFullPath\":\"/Users/minhmdru/mediaroot/mp\",\n" +
                "                    \"status\":\"new\"\n" +
                "                }\n" +
                "\n" +
                "\n" +
                "                fetch('http://localhost:8080/api/transcode', {\n" +
                "                method: \"POST\",\n" +
                "                body: JSON.stringify(_data),\n" +
                "                headers: {\"Content-type\": \"application/json; charset=UTF-8\"}\n" +
                "                })\n" +
                "                .then(response => response.json()) \n" +
                "                .then(json => console.log(json))\n" +
                "                .catch(err => console.log(err));\n" +
                "\n" +
                "            }\n" +
                "        </script>\n" +
                "        <style>\n" +
                "            .button {\n" +
                "                width: 150px;\n" +
                "                height: 40px;\n" +
                "            }\n" +
                "    .grid-container {\n" +
                "    display: grid;\n" +
                "    grid-template-columns: 1fr 1fr;\n" +
                "    grid-gap: 20px;\n" +
//                "    padding-left: 40px;\n" +
                "}\n"+
                "    .left {\n" +
                        "        padding-left: 40px;\n" +
                        "    }\n"+
                "body {\n" +
                "    background-color: darkgray;\n" +
                "}\n"+
                "        </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div><img src=\"https://mb.cision.com/Public/16563/3569090/96ce0201712e643c_800x800ar.png\" /></div>" +
                "<div class=\"grid-container\">" +
                "    <div class=\"grid-child purple left\">\n" +
                "       <h1>Title: %s</h1> \n" +
                "       <h2>Asset ID: %s</h2>\n" +
                "       <h2>Metadata:</h2>\n" +
                "       <h3>\n" +
                "           %s\n" +
                "       </h3>\n" +
                "       <button onclick=\"createTransfer()\" class=\"button\">Send to MINIO Storage</button><br>\n" +
                "       <button onclick=\"createTranscode()\" class=\"button\">Generate DASH</button>\n" +
                "    </div>\n" +
                "    <div class=\"grid-child green\">\n" +
                "       <video data-dashjs-player autoplay src=\"http://localhost:8080/api/assets/dash/%s/%s\" controls></video>\n" +
                "   </div>\n" +
                "</div>" +
                "</body>\n" +
                "</html>\n" +
                "\n",
                (mediaMp4Optional.isPresent()?mediaMp4Optional.get().id:""),
                (mediaMp4Optional.isPresent()?mediaMp4Optional.get().id:""),
                asset.title, asset.id, asset.metadata.toString(), asset.id , String.format("%s_MP4.mpd",fileNameBase));

//        String html =         String.format("<!DOCTYPE html>\n" +
//                "<html>\n" +
//                "<head>\n" +
//                "<script src=\"http://cdn.dashjs.org/latest/dash.all.min.js\"></script>\n" +
//                "<style>\n" +
//                "    video {\n" +
//                "       width: 640px;\n" +
//                "       height: 360px;\n" +
//                "    }\n" +
//                "</style>\n" +
//                "</head>\n" +
//                "<body>\n" +
//                "<div>\n" +
//                "       <video data-dashjs-player autoplay src=\"http://localhost:8080/api/assets/dash/%s/%s\" controls></video>\n" +
//                "   </div>\n" +
//                "</body>\n" +
//                "</html>\n" +
//                "\n",mediaOptional.get().assetId,mediaOptional.get().fileName);
        return html;
    }

    ;
    @GET
    @Path("/dash/{id}/{filename}")
    @Produces("*/*")
    public File getDash(Long id, String filename) {
        String path = String.format("/Users/minhmdru/mediaroot/%s/%s",id+"",filename);
        return new File(path);
    }

    @GET
    @Path("/view")
    @Produces(MediaType.TEXT_HTML)
    public String view() {
        String header = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "      \n" +
                "<head>\n" +
                "    <title>\n" +
                "        Set alternate row in table\n" +
                "    </title>\n" +
                "      \n" +
                "    <style>\n" +
                "        table {\n" +
                "            border-collapse: collapse;\n" +
                "            width: 100%;\n" +
                "        }\n" +
                "          \n" +
                "        th, td {\n" +
                "            text-align: left;\n" +
                "            padding: 8px;\n" +
                "        }\n" +
                "          \n" +
                "        tr:nth-child(odd) {\n" +
                "            background-color: gray;\n" +
                "        }\n" +
                "body {\n" +
                "    background-color: darkgray;\n" +
                "}\n"+
                "    </style>\n" +
                "</head>\n" +
                "  \n" +
                "<body>\n" +
                "<div><img src=\"https://mb.cision.com/Public/16563/3569090/96ce0201712e643c_800x800ar.png\" /></div>" +
                "    <table>\n" +
                "        <tr>\n" +
                "            <th>Asset ID</th>\n" +
                "            <th>Title</th>\n" +
                "            <th>Action</th>\n" +
                "        </tr>\n";
        String footer = "    </table>\n" +
                "</body>    \n" +
                "  \n" +
                "<html>\n";
        String body = "";
        String listAsset = AssetEntity.listAll(Sort.by("id")).stream().map(x -> {
            return AssetMapper.INSTANCE.map((AssetEntity) x);
        }).map(x-> {
            return String.format("<tr><td>%s</td><td>%s</td><td><a href=\"http://localhost:8080/api/assets/viewasset/%s\">View</a></td></tr>",x.id,x.title,x.id+"");
        }).collect(Collectors.joining());

        return header + listAsset + footer;
    }

    @GET
    @Path("{id}")
    public AssetDto get(Long id) {
        AssetEntity entity = AssetEntity.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Asset with id of " + id + " does not exist.", 404);
        }
        System.out.println(entity);
        return AssetMapper.INSTANCE.map(entity);
    }

    @POST
    @Transactional
    public Response create(AssetEntity asset) {
        if (asset.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        asset.persist();
        return Response.ok(asset).status(201).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public AssetEntity update(Long id, AssetDto assetDto) {
        if (assetDto.title == null) {
            throw new WebApplicationException("Asset Name was not set on request.", 422);
        }

        AssetEntity entity = AssetEntity.findById(id);

        if (entity == null) {
            throw new WebApplicationException("Asset with id of " + id + " does not exist.", 404);
        }

//        entity.title = assetDto.name;

        return entity;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(Long id) {
        AssetEntity entity = AssetEntity.findById(id);
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
