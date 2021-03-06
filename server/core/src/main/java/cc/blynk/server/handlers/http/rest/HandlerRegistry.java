package cc.blynk.server.handlers.http.rest;

import cc.blynk.utils.UriTemplate;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpMethod;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Set;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 09.12.15.
 */
public class HandlerRegistry {

    private final static Set<HandlerHolder> processors = new HashSet<>();

    public static void register(String rootPath, Object o) {
        registerHandler(rootPath, o);
    }

    public static void register(Object o) {
        registerHandler("", o);
    }

    private static void registerHandler(String rootPath, Object handler) {
        Class<?> handlerClass = handler.getClass();
        Annotation pathAnnotation = handlerClass.getAnnotation(Path.class);
        String handlerMainPath = ((Path) pathAnnotation).value();

        for (Method method : handlerClass.getMethods()) {
            Annotation consumes = method.getAnnotation(Consumes.class);
            String contentType = MediaType.APPLICATION_JSON;
            if (consumes != null) {
                contentType = ((Consumes) consumes).value()[0];
            }

            Annotation path = method.getAnnotation(Path.class);
            if (path != null) {
                String fullPath = rootPath + handlerMainPath + ((Path) path).value();
                UriTemplate uriTemplate = new UriTemplate(fullPath);

                HandlerHolder handlerHolder = new HandlerHolder(uriTemplate, method, handler, method.getParameterCount());

                for (int i = 0; i < method.getParameterCount(); i++) {
                    Parameter parameter = method.getParameters()[i];

                    Annotation queryParamAnnotation = parameter.getAnnotation(QueryParam.class);
                    if (queryParamAnnotation != null) {
                        handlerHolder.params[i] = new QueyMethodParam(((QueryParam) queryParamAnnotation).value(), parameter.getType());
                    }

                    Annotation pathParamAnnotation = parameter.getAnnotation(PathParam.class);
                    if (pathParamAnnotation != null) {
                        handlerHolder.params[i] = new PathMethodParam(((PathParam) pathParamAnnotation).value(), parameter.getType());
                    }

                    Annotation formParamAnnotation = parameter.getAnnotation(FormParam.class);
                    if (formParamAnnotation != null) {
                        handlerHolder.params[i] = new FormMethodParam(((FormParam) formParamAnnotation).value(), parameter.getType());
                    }

                    if (pathParamAnnotation == null && queryParamAnnotation == null && formParamAnnotation == null) {
                        handlerHolder.params[i] = new BodyMethodParam(parameter.getName(), parameter.getType(), contentType);
                    }
                }

                processors.remove(handlerHolder);
                processors.add(handlerHolder);
            }
        }
    }

    public static void populateBody(HttpRequest req, URIDecoder uriDecoder) {
        if (req.getMethod() == HttpMethod.PUT || req.getMethod() == HttpMethod.POST) {
            if (req instanceof HttpContent) {
                uriDecoder.bodyData = ((HttpContent) req).content();
                uriDecoder.contentType = req.headers().get(HttpHeaders.Names.CONTENT_TYPE);
            }
        }
    }

    public static HandlerHolder findHandler(HttpMethod httpMethod, String path) {
        for (HandlerHolder handlerHolder : processors) {
            if (handlerHolder.httpMethod == httpMethod && handlerHolder.uriTemplate.matches(path)) {
                return handlerHolder;
            }
        }

        return null;
    }

    public static FullHttpResponse invoke(HandlerHolder handlerHolder, Object[] params) {
        try {
            return (FullHttpResponse) handlerHolder.method.invoke(handlerHolder.handler, params);
        } catch (Exception e) {
            return Response.serverError(e.getMessage());
        }
    }

    public static String path(String uri) {
        int pathEndPos = uri.indexOf('?');
        if (pathEndPos < 0) {
            return uri;
        } else {
            return uri.substring(0, pathEndPos);
        }
    }

}
